package com.propellerads.sdk.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object PropellerAdsSDK {

    private var isInitialized: Boolean = false

    @Synchronized
    @JvmStatic
    fun init(applicationContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!isInitialized) {
                isInitialized = true
                DI.init(applicationContext)
            }
        }
    }
}