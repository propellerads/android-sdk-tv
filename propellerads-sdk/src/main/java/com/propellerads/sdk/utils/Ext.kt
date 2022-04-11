package com.propellerads.sdk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION


internal object Colors {
    fun from(color: String?) = color?.let {
        try {
            Color.parseColor(color)
        } catch (e: Exception) {
            null
        }
    } ?: Color.WHITE
}

internal object UriSafeParser {
    fun parse(uri: String?) = uri?.let {
        try {
            Uri.parse(uri)
        } catch (e: Exception) {
            null
        }
    }
}

internal fun Context.getCustomTabsPackages(): List<ResolveInfo> {

    // Get default VIEW intent handler.
    val activityIntent = Intent()
        .setAction(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.fromParts("http", "", null))

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent()
        serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(info.activityInfo.packageName)
        // Check if this package also resolves the Custom Tabs service.
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info)
        }
    }
    return packagesSupportingCustomTabs
}

internal fun Context.hasCustomTabsBrowser() = getCustomTabsPackages().isNotEmpty()

var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }