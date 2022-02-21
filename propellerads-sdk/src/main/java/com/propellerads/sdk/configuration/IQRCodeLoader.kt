package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.flow.Flow

internal interface IQRCodeLoader {

    fun getQrCode(banner: IBannerConfig): Flow<Resource<QRCode>>

    fun getQrCodeBytes(qrCode: QRCode): Flow<Resource<ByteArray>>

    fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean>
}