package com.propellerads.sdk.bannerAd.ui.base

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.repository.BannerGravity
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlin.coroutines.CoroutineContext

internal abstract class BaseBannerDialog :
    DialogFragment(), IBanner, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    protected abstract val dismissFlow: Flow<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        // remove title space
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog)
    }

    private fun getConfig() =
        arguments?.getSerializable(IBannerConfig.CONFIG) as? IBannerConfig

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val config = getConfig()
        if (config == null) {
            dismissSafely()
            return null
        }

        return configureBanner(inflater, config)
    }

    protected abstract fun configureBanner(
        inflater: LayoutInflater,
        config: IBannerConfig,
    ): View?

    protected fun configureFullScreen(
        isFullWidth: Boolean,
        isFullHeight: Boolean,
    ) {
        dialog?.window?.apply {

            decorView.apply {

                val displayMetrics = Resources.getSystem().displayMetrics
                if (isFullWidth) {
                    minimumWidth = displayMetrics.widthPixels
                }
                if (isFullHeight) {
                    minimumHeight = displayMetrics.heightPixels
                }
            }

            fun getSize(isFull: Boolean) =
                if (isFull) FrameLayout.LayoutParams.MATCH_PARENT
                else FrameLayout.LayoutParams.WRAP_CONTENT

            setLayout(
                getSize(isFullWidth),
                getSize(isFullHeight)
            )
        }
    }

    protected fun configureGravity(
        vertical: BannerGravity,
    ) {
        dialog?.window?.apply {
            when (vertical) {
                BannerGravity.TOP -> Gravity.TOP
                BannerGravity.BOTTOM -> Gravity.BOTTOM
                BannerGravity.CENTER -> null
            }?.let { verticalGravity ->
                setGravity(Gravity.CENTER_HORIZONTAL or verticalGravity)
            }
        }
    }

    protected fun configureBackground(
        color: String,
        hasRoundedCorners: Boolean,
    ) {
        dialog?.window?.decorView?.apply {
            val backgroundColor = Colors.from(color)
            if (hasRoundedCorners) {
                val background = GradientDrawable().apply {
                    setColor(backgroundColor)
                    cornerRadius = 16.dp
                }
                setBackground(background)
            } else {
                setBackgroundColor(backgroundColor)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Don't move this method to onCreate
        // Coroutines stops on fragment pause to prevent update UI from background
        subscribeOnDismiss()
    }

    private fun subscribeOnDismiss() {
        launch {
            dismissFlow
                .filter { it }
                .collect {
                    dismissSafely()
                }
        }
    }

    override fun show(fragmentManager: FragmentManager) {
        val bannerId = getConfig()?.id
        show(fragmentManager, bannerId)
    }

    protected fun dismissSafely() {
        if (!isStateSaved) {
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancelChildren()
    }
}