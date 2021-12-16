package com.propellerads.sdk.provider.adId

import android.annotation.SuppressLint
import android.content.Context
import java.util.*

/**
 * Should be replaced with
 * https://developer.android.com/training/articles/ad-id
 */
internal class AdIdProvider(
    context: Context,
) : IAdIdProvider {

    private companion object {
        const val PREF_NAME = "propeller_prefs"
        const val AD_USER_ID_KEY = "AD_USER_ID_KEY"
    }

    private val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    @Synchronized
    override fun getAdId(): String {
        val storedAdId = sharedPref.getString(AD_USER_ID_KEY, null)
        if (storedAdId != null) return storedAdId

        val generatedAdId = UUID.randomUUID().toString()
        sharedPref.edit().putString(AD_USER_ID_KEY, generatedAdId).commit()
        return generatedAdId
    }
}