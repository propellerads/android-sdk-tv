package com.propellerads.sdk.repository

import android.os.Build
import kotlin.math.floor

internal interface IDeviceDataProvider {
    fun getDeviceModel(): String
    fun getAndroidVersion(): Int
}

internal class DeviceDataProvider : IDeviceDataProvider {

    private companion object {
        const val DOUBLE_REGEX = "\\d+(\\.\\d+)?"
    }

    override fun getDeviceModel(): String = Build.MODEL

    override fun getAndroidVersion(): Int {
        return Build.VERSION.RELEASE.toPlainIntVersion()
    }

    private fun String.toPlainIntVersion(): Int {
        val parsedVersion = DOUBLE_REGEX.toRegex()
            .find(this)?.value
        if (parsedVersion.isNullOrBlank()) return 0
        return try {
            parsedVersion
                .toDouble()
                .let { number -> floor(number) }
                .toInt()
        } catch (e: Exception) {
            0
        }
    }
}