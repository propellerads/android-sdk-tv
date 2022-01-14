package com.propellerads.adapp

import android.app.Application
import android.os.StrictMode

class App : Application() {

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
            StrictMode.setVmPolicy(policy)
        }
        super.onCreate()
    }
}