package com.propellerads.sdk.bannerAd.ui.interstitial

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.propellerads.sdk.utils.Logger

class WebViewClient(
    private val onLandingLoadedHandler: () -> Unit,
    private val landingClickHandler: () -> Unit,
) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLandingLoadedHandler()
    }

    // For API below 24

    private var oldApiGestureHandled = false

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        if (request?.hasGesture() == true) {
            oldApiGestureHandled = true
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
        baseShouldOverrideUrlLoading(Uri.parse(url), oldApiGestureHandled)


    // For API 24 and above

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean = baseShouldOverrideUrlLoading(request?.url, request?.hasGesture())


    private fun baseShouldOverrideUrlLoading(
        url: Uri?,
        hasGesture: Boolean?
    ): Boolean {
        Logger.d("WebView redirect. Has gesture: $hasGesture; Url: $url")

        if (hasGesture == true) {
            landingClickHandler()
            return true
        }

        return false
    }
}