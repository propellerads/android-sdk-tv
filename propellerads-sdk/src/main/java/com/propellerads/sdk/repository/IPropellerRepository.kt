package com.propellerads.sdk.repository

import com.propellerads.sdk.provider.deviceType.DeviceType
import kotlinx.coroutines.flow.Flow

internal interface IPropellerRepository {

    fun getConfiguration(
        publisherId: String,
        userId: String,
        deviceType: DeviceType
    ): Flow<Resource<AdConfiguration>>

    fun impressionCallback(url: String): Flow<Resource<OK>>

    fun getQRCode(url: String): Flow<Resource<QRCode>>

    fun checkQRCode(url: String): Flow<Resource<OK>>
}