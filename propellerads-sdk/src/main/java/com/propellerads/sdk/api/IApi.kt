package com.propellerads.sdk.api

import com.propellerads.sdk.api.dto.*
import okhttp3.ResponseBody
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
    suspend fun getQRCode(@Url url: String): QRCodeRes

    @GET
    suspend fun getQRCodeBitmap(@Url url: String): ResponseBody

    @GET
    suspend fun checkQRCode(@Url url: String): OkRes

    @GET
    suspend fun getInterstitialLanding(@Url url: String): InterstitialLandingRes
}