package com.propellerads.sdk.configurator

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.repository.WidgetConfig

internal sealed class WidgetConfigStatus {
    object Loading : WidgetConfigStatus()
    data class Success(val widgets: List<WidgetConfig>) : WidgetConfigStatus()
    data class Error(val exception: Exception?) : WidgetConfigStatus()
}

internal sealed class BannerConfigStatus {
    object Loading : BannerConfigStatus()
    data class Success(val banners: Map<String, IBannerConfig>) : BannerConfigStatus()
    data class Error(val exception: Exception?) : BannerConfigStatus()
}