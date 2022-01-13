package com.propellerads.sdk.utils

import android.util.Log
import com.propellerads.sdk.BuildConfig

internal interface ILogger {
    fun d(message: String, suffix: String? = null)
}

internal object Logger : ILogger {

    private const val TAG = "AdSDK"

    override fun d(message: String, suffix: String?) {
        val tag = suffix?.let { "${TAG}_$suffix" } ?: TAG
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }
}