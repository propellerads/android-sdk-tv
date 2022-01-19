package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.repository.WidgetConfig

internal sealed class WidgetConfigStatus {
    object Loading : WidgetConfigStatus()
    data class Success(val widgets: List<WidgetConfig>) : WidgetConfigStatus()
    data class Error(val exception: Throwable?) : WidgetConfigStatus()
}

internal sealed class BannerConfigStatus {
    object Loading : BannerConfigStatus()
    data class Success(val banners: Map<String, IBannerConfig>) : BannerConfigStatus()
    data class Error(val exception: Throwable?) : BannerConfigStatus()
}

internal sealed class QRCodeStatus {

    companion object {
        fun fromResource(res: Resource<QRCode>): QRCodeStatus = when (res) {
            is Resource.Loading -> Loading
            is Resource.Fail -> Error(res.exception)
            is Resource.Success -> Success(res.data)
        }
    }

    object Loading : QRCodeStatus()
    data class Success(val qrCode: QRCode) : QRCodeStatus()
    data class Error(val exception: Throwable?) : QRCodeStatus()
}