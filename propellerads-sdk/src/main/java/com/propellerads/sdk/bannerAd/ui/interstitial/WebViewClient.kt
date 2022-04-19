package com.propellerads.sdk.bannerAd.ui.interstitial

import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.utils.UriSafeParser

class WebViewClient(
    private val onPageFinished: () -> Unit,
    private val onPageFailed: () -> Unit,
    private val onLandingClicked: (uri: String?) -> Unit,
) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        onPageFinished()
    }


    override fun onReceivedHttpError(
        view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
    ) {
        // This works on API 23 and above.
        // No way to determine an http error in previous API versions.
        onPageFailed()
    }

    override fun onReceivedError(
        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
    ) {
        onPageFailed()
    }

    override fun onReceivedSslError(
        view: WebView?, handler: SslErrorHandler?, error: SslError?
    ) {
        onPageFailed()
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
            onLandingClicked(uri?.toString())
            return true
        }

        return false
    }
}