package com.propellerads.sdk.bannerAd.ui.interstitial

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.InterstitialAppearance

internal interface IInterstitialConfig : IBannerConfig {
    val interstitialUrl: String
    val appearance: InterstitialAppearance
}