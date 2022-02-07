package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.flow.Flow

internal interface IQRCodeLoader {

    val qrCodesStatus: Flow<Map<String, Resource<QRCode>>>

    fun getQrCode(banner: IBannerConfig)

    fun getQrCodeBytes(qrCode: QRCode): Flow<Resource<ByteArray>>

    fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean>
}