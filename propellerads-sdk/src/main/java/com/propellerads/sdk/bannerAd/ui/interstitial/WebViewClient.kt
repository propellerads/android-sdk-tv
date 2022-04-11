package com.propellerads.sdk.bannerAd.ui.interstitial

import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.utils.UriSafeParser

class WebViewClient(
    private val onLandingLoadedHandler: () -> Unit,
    private val landingClickHandler: (uri: Uri?) -> Unit,
) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLandingLoadedHandler()
    }

    // For API below 24

    private var oldApiGestureHandled = false

    fun onGestureHandled() {
        oldApiGestureHandled = true
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        baseShouldOverrideUrlLoading(UriSafeParser.parse(url), oldApiGestureHandled)


    // For API 24 and above

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean = baseShouldOverrideUrlLoading(request?.url, request?.hasGesture())


    private fun baseShouldOverrideUrlLoading(
        uri: Uri?,
        hasGesture: Boolean?
    ): Boolean {
        Logger.d("WebView redirect. Has gesture: $hasGesture; Url: $uri")

        if (hasGesture == true) {
            landingClickHandler(uri)
            return true
        }

        return false
    }
}