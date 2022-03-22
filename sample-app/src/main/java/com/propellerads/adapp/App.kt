package com.propellerads.adapp

import android.os.Build
import androidx.multidex.MultiDexApplication
import com.propellerads.sdk.di.PropellerAdsSDK

class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PropellerAdsSDK.init(applicationContext)
        }
    }
}