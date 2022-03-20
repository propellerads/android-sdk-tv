package com.propellerads.sdk.bannerAd.ui.interstitial

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.propellerads.sdk.bannerAd.ui.base.BaseBannerDialog
import com.propellerads.sdk.bannerAd.ui.base.IBannerBuilder
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.databinding.PropellerBannerInterstitionalBinding
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.utils.hasCustomTabsBrowser
import com.propellerads.sdk.utils.isVisible
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

internal class InterstitialDialog
private constructor() : BaseBannerDialog() {

    companion object : IBannerBuilder {
        override fun build(
            requestUUID: UUID,
            config: IBannerConfig
        ) = InterstitialDialog().apply {
            arguments = Bundle().apply {
                putSerializable(IBannerConfig.REQUEST_UUID, requestUUID)
                putSerializable(IBannerConfig.CONFIG, config)
            }
        }

        private const val TAG = "Banner"
    }

    private val viewModel by lazy {
        ViewModelProvider(viewModelStore, defaultViewModelProviderFactory)
            .get(InterstitialDialogViewModel::class.java)
    }

    override val dismissFlow: Flow<Boolean>
        get() = viewModel.dismissFlow

    private var viewBinding: PropellerBannerInterstitionalBinding? = null

    @Volatile
    private var landingUrl: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun configureBanner(
        inflater: LayoutInflater,
        config: IBannerConfig
    ): View? {

        if (config !is IInterstitialConfig) {
            dismissSafely()
            return null
        }

        val view = configureView(inflater).root

        viewModel.setConfig(config)

        return view
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun configureView(
        inflater: LayoutInflater,
    ): PropellerBannerInterstitionalBinding {

        configureFullScreen(
            isFullWidth = true,
            isFullHeight = true
        )

        val binding = PropellerBannerInterstitionalBinding.inflate(inflater)
        viewBinding = binding

        val client = WebViewClient(
            onLandingLoadedHandler = ::onLandingLoaded,
            landingClickHandler = ::handleUserClickOnLanding
        )

        binding.webView.run {
            settings.javaScriptEnabled = true
            webViewClient = client

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    client.onGestureHandled()
                }
                false
            }
        }

        binding.closeBtn.setOnClickListener {
            viewModel.dismissBanner()
        }

        return binding
    }

    private fun onLandingLoaded() {
        viewBinding?.progress?.isVisible = false
    }

    private fun handleUserClickOnLanding() {
        viewModel.dismissBanner()
        val uri = getLandingUri()
        if (uri == null) {
            Logger.d("Landing URL is empty or damaged", TAG)
            return
        }
        if (requireContext().hasCustomTabsBrowser()) {
            viewModel.callbackImpression()
            openBrowser(uri)
        } else {
            Logger.d("Android device does not support Web browsing", TAG)
        }
    }

    private fun getLandingUri(): Uri? = try {
        Uri.parse(landingUrl)
    } catch (e: Exception) {
        null
    }

    private fun openBrowser(url: Uri) {
        Logger.d("Proceed to browser URL: $url", TAG)
        val browserIntent = Intent(Intent.ACTION_VIEW, url)
        startActivity(browserIntent)
    }

    override fun onResume() {
        super.onResume()
        // Don't move this methods to onCreate
        // Coroutines stops on fragment pause to prevent update UI from background
        subscribeOnLandingData()
        subscribeOnCrossVisibility()
    }

    private fun subscribeOnLandingData() {
        launch {
            viewModel.landingFlow
                .filterNotNull()
                .map { it.landingUrl }
                .collect(::displayLandingInWebView)
        }
    }

    private fun displayLandingInWebView(url: String) {
        if (landingUrl == null) {
            landingUrl = url
            viewBinding?.webView?.loadUrl(url)
        }
    }

    private fun subscribeOnCrossVisibility() {
        launch {
            viewModel.isCrossVisible
                .collect(::setCrossVisibility)
        }
    }

    private fun setCrossVisibility(isVisible: Boolean) {
        viewBinding?.closeBtn?.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}