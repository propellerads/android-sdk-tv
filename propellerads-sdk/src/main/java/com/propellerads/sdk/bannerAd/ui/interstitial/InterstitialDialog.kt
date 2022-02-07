package com.propellerads.sdk.bannerAd.ui.interstitial

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import com.propellerads.sdk.bannerAd.ui.base.BaseBannerDialog
import com.propellerads.sdk.bannerAd.ui.base.IBannerBuilder
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.databinding.PropellerBannerInterstitionalBinding
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.utils.hasCustomTabsBrowser
import com.propellerads.sdk.utils.isVisible
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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

    private val viewModel: InterstitialDialogViewModel by viewModels()

    override val dismissFlow: Flow<Boolean>
        get() = viewModel.dismissFlow

    private var viewBinding: PropellerBannerInterstitionalBinding? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun configureBanner(
        inflater: LayoutInflater,
        bannerConfig: IBannerConfig
    ): View? {

        if (bannerConfig !is IInterstitialConfig) {
            dismissSafely()
            return null
        }

        val view = configureView(inflater, bannerConfig).root

        viewModel.setConfig(bannerConfig)

        return view
    }

    private fun configureView(
        inflater: LayoutInflater,
        config: IInterstitialConfig
    ): PropellerBannerInterstitionalBinding {

        configureFullScreen(
            isFullWidth = true,
            isFullHeight = true
        )

        val binding = PropellerBannerInterstitionalBinding.inflate(inflater)
        viewBinding = binding

        binding.webView.run {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient(::handleRedirect)
            loadUrl(config.interstitialUrl)
        }

        binding.closeBtn.setOnClickListener {
            viewModel.dismissBanner()
        }

        return binding
    }

    private fun handleRedirect(url: Uri) {
        viewModel.dismissBanner()
        if (requireContext().hasCustomTabsBrowser()) {
            viewModel.callbackImpression()
            openBrowser(url)
        } else {
            Logger.d("Android device does not support Web browsing", TAG)
        }
    }

    private fun openBrowser(url: Uri) {
        Logger.d("Proceed to browser URL: $url", TAG)
        val browserIntent = Intent(Intent.ACTION_VIEW, url)
        startActivity(browserIntent)
    }

    override fun onResume() {
        super.onResume()
        subscribeOnCrossVisibility()
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