package com.propellerads.sdk.utils

import android.util.Log
import com.propellerads.sdk.BuildConfig

internal object Logger {

    private const val TAG = "PropellerAdsSDK"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }
}