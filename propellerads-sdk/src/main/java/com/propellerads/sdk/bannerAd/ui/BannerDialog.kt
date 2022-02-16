package com.propellerads.sdk.bannerAd.ui

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
import androidx.fragment.app.viewModels
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.repository.BannerGravity
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class BannerDialog private constructor() :
    DialogFragment(), IBanner, CoroutineScope {

    companion object {
        fun build(
            requestUUID: UUID,
            config: IBannerConfig
        ) = BannerDialog().apply {
            arguments = Bundle().apply {
                putSerializable(IBannerConfig.REQUEST_UUID, requestUUID)
                putSerializable(IBannerConfig.CONFIG, config)
            }
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val viewModel: BannerDialogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        // remove title space
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bannerConfig = arguments?.getSerializable(IBannerConfig.CONFIG) as? IBannerConfig
        if (bannerConfig == null) {
            dismissSafely()
            return null
        }

        return configureBanner(inflater, bannerConfig)
    }

    private fun configureBanner(
        inflater: LayoutInflater,
        bannerConfig: IBannerConfig,
    ): View {
        viewModel.setBannerConfig(bannerConfig)

        configureDialogParams(bannerConfig.appearance)

        val binding = when (bannerConfig.appearance.layoutTemplate) {
            "qr_code_3_1" -> PropellerBannerQrBinding.inflate(inflater)
                .apply {
                    applyStyle(bannerConfig)
                    launch {
                        viewModel.qrCodeImageFlow
                            .collect(this@apply::setQRImage)
                    }
                }
            else -> null
        }

        return binding?.root ?: View(context).also { dismissSafely() }
    }

    private fun configureDialogParams(appearance: BannerAppearance) {

        dialog?.window?.apply {

            // Configure vertical gravity
            when (appearance.gravity) {
                BannerGravity.TOP -> Gravity.TOP
                BannerGravity.BOTTOM -> Gravity.BOTTOM
                BannerGravity.CENTER -> null
            }?.let { verticalGravity ->
                setGravity(Gravity.CENTER_HORIZONTAL or verticalGravity)
            }

            decorView.apply {

                val backgroundColor = Colors.from(appearance.backgroundColor)
                if (appearance.hasRoundedCorners) {
                    val background = GradientDrawable().apply {
                        setColor(backgroundColor)
                        cornerRadius = 16.dp
                    }
                    setBackground(background)
                } else {
                    setBackgroundColor(backgroundColor)
                }

                val displayMetrics = Resources.getSystem().displayMetrics
                if (appearance.isFullWidth) {
                    minimumWidth = displayMetrics.widthPixels
                }
                if (appearance.isFullHeight) {
                    minimumHeight = displayMetrics.heightPixels
                }
            }

            fun getSize(isFull: Boolean) =
                if (isFull) FrameLayout.LayoutParams.MATCH_PARENT
                else FrameLayout.LayoutParams.WRAP_CONTENT

            setLayout(
                getSize(appearance.isFullWidth),
                getSize(appearance.isFullHeight)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        subscribeOnDismiss()
    }

    private fun subscribeOnDismiss() {
        launch {
            viewModel.dismissFlow
                .filter { it }
                .collect {
                    dismissSafely()
                }
        }
    }

    override fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, null)
    }

    private fun dismissSafely() {
        if (!isStateSaved) {
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        job.cancelChildren()
    }
}