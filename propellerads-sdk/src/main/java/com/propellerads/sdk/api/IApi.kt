package com.propellerads.sdk.api

import com.propellerads.sdk.api.dto.DeviceTypeReq
import com.propellerads.sdk.api.dto.OkRes
import com.propellerads.sdk.api.dto.QRCodeSettingsRes
import com.propellerads.sdk.api.dto.SettingsRes
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


internal interface IApi {

    @GET("settings")
    suspend fun getSettings(
        @Query("publisher_id") publisherId: String,
        @Query("user_id") userId: String,
        @Query("device_type") deviceType: DeviceTypeReq,
    ): SettingsRes

    @GET
    suspend fun impressionCallback(@Url url: String): OkRes

    @GET
    suspend fun getQRCode(@Url url: String): QRCodeSettingsRes
}