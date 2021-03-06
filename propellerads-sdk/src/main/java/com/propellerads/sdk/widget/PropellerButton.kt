package com.propellerads.sdk.widget

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import com.propellerads.sdk.R
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.repository.WidgetConfig
import com.propellerads.sdk.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlin.coroutines.CoroutineContext

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

    private val adConfigurator by lazy { DI.configLoader }

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
        obtainConfiguration()
    }

    private fun obtainConfiguration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            coroutineScope.launch {
                adConfigurator.widgetsStatus
                    .combine(widgetIdState, ::Pair)
                    .collect(::handleConfigurationRes)
            }
        }
    }

    private fun handleConfigurationRes(
        pair: Pair<Resource<Map<String, WidgetConfig>>, String>
    ) {
        val (resource, widgetId) = pair
        when (resource) {
            is Resource.Loading -> {
                setContent(hasProgress = true)
            }
            is Resource.Success -> {
                val widgetConfig = resource.data[widgetId]
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
            is Resource.Fail -> {
                errorView.text = resource.exception?.message ?: "API exception"
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (widgetConfig.browserUrl.isNotEmpty()) {
                if (context.hasCustomTabsBrowser()) {
                    adConfigurator.callbackImpression(widgetConfig.impressionUrl)
                    openBrowser(widgetConfig.browserUrl)
                } else {
                    Logger.d("Android device does not support Web browsing")
                }
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