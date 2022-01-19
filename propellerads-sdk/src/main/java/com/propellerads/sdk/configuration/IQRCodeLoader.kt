package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.repository.QRCode
import kotlinx.coroutines.flow.Flow

internal interface IQRCodeLoader {

    val qrCodesStatus: Flow<Map<String, QRCodeStatus>>

    fun getQrCode(banner: IBannerConfig)

    fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean>
}