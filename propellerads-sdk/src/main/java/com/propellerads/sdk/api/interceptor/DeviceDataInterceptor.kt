package com.propellerads.sdk.api.interceptor

import com.propellerads.sdk.repository.IDeviceDataProvider
import okhttp3.Interceptor
import okhttp3.Response

internal class DeviceDataInterceptor(
    deviceDataProvider: IDeviceDataProvider,
) : Interceptor {

    private var model: String = ""
    private var version: String = ""

    init {
        this.model = deviceDataProvider.getDeviceModel()
        val versionInt = deviceDataProvider.getAndroidVersion()
        this.version = if (versionInt > 0) "$versionInt" else ""
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("device-model", model)
            .addHeader("android-version", version)
            .build()
        return chain.proceed(request)
    }
}