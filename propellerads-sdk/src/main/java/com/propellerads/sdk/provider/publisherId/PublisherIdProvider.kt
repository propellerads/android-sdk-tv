package com.propellerads.sdk.provider.publisherId

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

internal class PublisherIdProvider(
    private val context: Context,
) : IPublisherIdProvider {

    companion object {
        const val PUBLISHER_ID_KEY = "com.propellerads.sdk.PublisherId"
    }

    override fun getPublisherId(): String? = try {
        val appInfo: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        appInfo.metaData.getString(PUBLISHER_ID_KEY)
    } catch (e: Exception) {
        null
    }
}