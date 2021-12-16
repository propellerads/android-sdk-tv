package com.propellerads.sdk.repository

import com.propellerads.sdk.api.IApi
import com.propellerads.sdk.api.dto.toDeviceTypeReq
import com.propellerads.sdk.provider.deviceType.DeviceType

internal class PropellerRepository(
    private val api: IApi,
    private val errorParser: IErrorParser,
) : IPropellerRepository {

    override fun getSettings(
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
}