package com.propellerads.sdk.widget

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import com.propellerads.sdk.R
import com.propellerads.sdk.configurator.AdConfigState
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.WidgetConfig
import com.propellerads.sdk.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlin.coroutines.CoroutineContext


class PropellerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val job = Job()
    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = job + Dispatchers.Main

    }

    private val adConfigurator = DI.adConfigurator

    private val button: AppCompatButton
    private val progress: ProgressBar
    private val errorView: TextView

    private val widgetIdState = MutableStateFlow("")

    init {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.propeller_button_layout, this)
        button = layout.findViewById(R.id.propeller_button)
        progress = layout.findViewById(R.id.propeller_progress)
        errorView = layout.findViewById(R.id.propeller_error)

        obtainAttrs(attrs)
    }

    private fun obtainAttrs(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PropellerButton, 0, 0
        ).apply {
            try {
                getString(R.styleable.PropellerButton_widget_id)?.let {
                    setWidgetId(it)
                }
            } finally {
                recycle()
            }
        }
    }

    fun setWidgetId(id: String) {
        coroutineScope.launch {
            widgetIdState.emit(id)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        coroutineScope.launch {
            adConfigurator.state
                .combine(widgetIdState, ::Pair)
                .collect(::handleConfiguration)
        }
    }

    private fun handleConfiguration(pair: Pair<AdConfigState, String>) {
        val (config, widgetId) = pair
        when (config) {
            is AdConfigState.Loading -> {
                setContent(hasProgress = true)
            }
            is AdConfigState.Success -> {
                val widgetConfig = config.widgets
                    .firstOrNull { it.id == widgetId }
                if (widgetConfig != null) {
                    configureWidget(widgetConfig)
                    setContent(hasButton = true)
                } else {
                    val error = if (widgetId.isNotEmpty())
                        "Config for widget \"$widgetId\" was not found"
                    else "Widget id was not set"
                    Logger.d(error)
                    errorView.text = error
                    setContent(hasErrorView = true)
                }
            }
            is AdConfigState.Error -> {
                errorView.text = config.exception?.message ?: "API exception"
                setContent(hasErrorView = true)
            }
        }
    }

    private fun configureWidget(widgetConfig: WidgetConfig) {
        val appearance = widgetConfig.appearance
        button.text = appearance.buttonLabel
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, appearance.buttonLabelSize.toFloat())
        button.setTextColor(Colors.from(appearance.buttonLabelColor))
        val typeface = when {
            appearance.isButtonLabelBold && appearance.isButtonLabelItalic -> Typeface.BOLD_ITALIC
            appearance.isButtonLabelBold -> Typeface.BOLD
            appearance.isButtonLabelItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        button.setTypeface(button.typeface, typeface)
        if (appearance.buttonLabelShadowColor.isNotEmpty()) {
            button.setShadowLayer(3.dp, 1.dp, 1.dp, Colors.from(appearance.buttonLabelShadowColor))
        }
        button.isAllCaps = appearance.buttonLabelAllCaps

        val gradientDrawable = if (appearance.buttonColors.size > 1) {
            val gradientColors = appearance.buttonColors.map(Colors::from).toIntArray()
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors)
        } else {
            GradientDrawable().apply {
                val color = Colors.from(appearance.buttonColors.firstOrNull())
                setColor(color)
            }
        }

        gradientDrawable.cornerRadius = appearance.buttonRadius.dp
        button.background = gradientDrawable

        val hPaddingDp = appearance.horizontalPadding.dp.toInt()
        val vPaddingDp = appearance.verticalPadding.dp.toInt()
        val horizontalPadding = if (hPaddingDp > 0) hPaddingDp else button.paddingLeft
        val verticalPadding = if (vPaddingDp > 0) vPaddingDp else button.paddingTop
        if (verticalPadding > 0) {
            button.minHeight = 0
            button.minimumHeight = 0
        }
        button.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        button.setOnClickListener {
            handleClick(widgetConfig)
        }
    }

    private fun handleClick(widgetConfig: WidgetConfig) {
        if (widgetConfig.browserUrl.isNotEmpty()) {
            if (context.hasCustomTabsBrowser()) {
                adConfigurator.impressionCallback(widgetConfig.impressionUrl)
                openBrowser(widgetConfig.browserUrl)
            } else {
                Logger.d("Android device does not support Web browsing")
            }
        }
    }

    private fun openBrowser(url: String) {
        Logger.d("Proceed to browser URL: $url")
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(context, Uri.parse(url))
    }

    private fun setContent(
        hasButton: Boolean = false,
        hasProgress: Boolean = false,
        hasErrorView: Boolean = false,
    ) {
        button.isVisible = hasButton
        progress.isVisible = hasProgress
        errorView.isVisible = hasErrorView
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancelChildren()
    }
}