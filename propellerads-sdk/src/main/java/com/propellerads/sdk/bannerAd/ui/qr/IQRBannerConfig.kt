package com.propellerads.sdk.bannerAd.ui.qr

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.BannerAppearance

internal interface IQRBannerConfig : IBannerConfig {
    val qrCodeRequestUrl: String?
    val appearance: BannerAppearance
}