package com.propellerads.sdk.bannerAd.ui.qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.propellerads.sdk.bannerAd.ui.base.BaseBannerDialog
import com.propellerads.sdk.bannerAd.ui.base.IBannerBuilder
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerAppearance
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

    private val viewModel by lazy {
        ViewModelProvider(viewModelStore, defaultViewModelProviderFactory)
            .get(QRBannerDialogViewModel::class.java)
    }

    override val dismissFlow: Flow<Boolean>
        get() = viewModel.dismissFlow

    override fun configureBanner(
        inflater: LayoutInflater,
        config: IBannerConfig
    ): View? {

        if (config !is IQRBannerConfig) {
            dismissSafely()
            return null
        }

        viewModel.setConfig(config)

        configureDialogParams(config.appearance)

        val binding = when (config.appearance.layoutTemplate) {
            "qr_code_3_1" -> PropellerBannerQrBinding.inflate(inflater)
                .apply {
                    applyStyle(config)
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

        configureFullScreen(
            isFullWidth = appearance.isFullWidth,
            isFullHeight = appearance.isFullHeight
        )

        configureGravity(
            vertical = appearance.gravity,
        )

        configureBackground(
            color = appearance.backgroundColor,
            hasRoundedCorners = appearance.hasRoundedCorners,
        )
    }
}