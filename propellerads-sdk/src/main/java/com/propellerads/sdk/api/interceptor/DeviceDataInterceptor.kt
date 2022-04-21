package com.propellerads.sdk.api.interceptor

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

internal class DeviceDataInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val device = getDeviceName()
        val android = Build.VERSION.RELEASE
        val request = chain.request()
            .newBuilder()
            .addHeader("device-model", device)
            .addHeader("android-version", android)
            .build()
        return chain.proceed(request)
    }

    private fun getDeviceName(): String {
        val manufacturer: String = Build.MANUFACTURER
        val model: String = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            capitalize(model)
        } else {
            "${capitalize(manufacturer)} $model"
        }
    }

    private fun capitalize(value: String) =
        value.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
}