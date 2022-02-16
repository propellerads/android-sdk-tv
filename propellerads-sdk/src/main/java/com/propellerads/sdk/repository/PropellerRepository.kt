package com.propellerads.sdk.repository

import com.propellerads.sdk.api.IApi
import com.propellerads.sdk.api.dto.toDeviceTypeReq
import com.propellerads.sdk.provider.deviceType.DeviceType
import kotlinx.coroutines.flow.Flow

internal class PropellerRepository(
    private val api: IApi,
    private val errorParser: IErrorParser,
) : IPropellerRepository {

    override fun getConfiguration(
        publisherId: String,
        userId: String,
        deviceType: DeviceType
    ) = execute(errorParser) {
        api.getSettings(
            userId = userId,
            publisherId = publisherId,
            deviceType = deviceType.toDeviceTypeReq()
        )
    }

    override fun impressionCallback(url: String) = execute(errorParser) {
        api.impressionCallback(url)
    }

    override fun getQRCode(url: String) = execute(errorParser) {
        api.getQRCode(url)
    }

    override fun getQrCodeBytes(url: String): Flow<Resource<ByteArray>> = executeRaw(
        mapper = { res -> res.use { it.byteStream().readBytes() } },
        request = { api.getQRCodeBitmap(url) }
    )

    override fun checkQRCode(url: String) = execute(errorParser) {
        api.checkQRCode(url)
    }
}