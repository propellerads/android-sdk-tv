package com.propellerads.sdk.bannerAd.ui.qr

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.bannerAd.ui.base.BaseBannerDialog
import com.propellerads.sdk.bannerAd.ui.base.IBannerBuilder
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.repository.BannerGravity
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.util.*

internal class QRBannerDialog
private constructor() : BaseBannerDialog() {

    companion object : IBannerBuilder {
        override fun build(
            requestUUID: UUID,
            config: IBannerConfig
        ) = QRBannerDialog().apply {
            arguments = Bundle().apply {
                putSerializable(IBannerConfig.REQUEST_UUID, requestUUID)
                putSerializable(IBannerConfig.CONFIG, config)
            }
        }
    }

    private val viewModel: QRBannerDialogViewModel by viewModels()

    override val dismissFlow: Flow<Boolean>
        get() = viewModel.dismissFlow

    override fun configureBanner(
        inflater: LayoutInflater,
        bannerConfig: IBannerConfig
    ): View? {

        if (bannerConfig !is IQRBannerConfig) {
            dismissSafely()
            return null
        }

        viewModel.setConfig(bannerConfig)

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
}