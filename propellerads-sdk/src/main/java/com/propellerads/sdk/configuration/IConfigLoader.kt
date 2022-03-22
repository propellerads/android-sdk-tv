package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.InterstitialLanding
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.repository.WidgetConfig
import kotlinx.coroutines.flow.emptyFlow

internal interface IConfigLoader :
    IWidgetLoader,
    IBannerLoader,
    IQRCodeLoader,
    ICallbackHandler,
    IInterstitialLoader {

    companion object {
        val STUB = object : IConfigLoader {
            override val widgetsStatus = emptyFlow<Resource<Map<String, WidgetConfig>>>()
            override val bannersStatus = emptyFlow<Resource<Map<String, IBannerConfig>>>()
            override fun getQrCode(banner: IBannerConfig) = emptyFlow<Resource<QRCode>>()
            override fun getQrCodeBytes(qrCode: QRCode) = emptyFlow<Resource<ByteArray>>()
            override fun checkQrCode(bannerId: String, qrCode: QRCode) = emptyFlow<Boolean>()
            override fun callbackImpression(url: String) = Unit
            override fun getInterstitialLanding(banner: IBannerConfig) = emptyFlow<Resource<InterstitialLanding>>()

        }
    }
}