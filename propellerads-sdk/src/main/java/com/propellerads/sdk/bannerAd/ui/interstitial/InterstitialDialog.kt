package com.propellerads.sdk.bannerAd.ui.interstitial

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.propellerads.sdk.bannerAd.ui.base.BaseBannerDialog
import com.propellerads.sdk.bannerAd.ui.base.IBannerBuilder
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.databinding.PropellerBannerInterstitionalBinding
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.utils.UriSafeParser
import com.propellerads.sdk.utils.hasCustomTabsBrowser
import com.propellerads.sdk.utils.isVisible
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
            onPageFinished = viewModel::onPageFinished,
            onPageFailed = viewModel::onPageFailed,
            onLandingClicked = viewModel::onLandingClicked
        )

        binding.webView.run {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
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

    override fun onResume() {
        super.onResume()
        // Don't move this methods to onCreate
        // Coroutines stops on fragment pause to prevent update UI from background
        subscribeOnViewCommandsFlow()
    }

    private fun subscribeOnViewCommandsFlow() {
        launch {
            viewModel.viewCommandFlow.collect { command ->
                when (command) {
                    is InterstitialCommand.LoadUrl -> displayLandingInWebView(command.url)
                    is InterstitialCommand.OpenBrowser -> openBrowser(command.url)
                    is InterstitialCommand.SetCrossVisibility -> setCrossVisibility(command.isVisible)
                    is InterstitialCommand.SetProgressVisibility -> setProgressVisibility(command.isVisible)
                    null -> Unit
                }
            }
        }
    }

    private fun displayLandingInWebView(url: String) {
        viewBinding?.webView?.loadUrl(url)
    }

    private fun openBrowser(url: String) {
        if (requireContext().hasCustomTabsBrowser()) {
            val uri = UriSafeParser.parse(url)
            if (uri == null) {
                Logger.d("Landing URL is empty or damaged", TAG)
                return
            }
            Logger.d("Proceed to browser URL: $url", TAG)
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(browserIntent)
        } else {
            Logger.d("Android device does not support Web browsing", TAG)
        }
    }

    private fun setCrossVisibility(isVisible: Boolean) {
        viewBinding?.closeBtn?.isVisible = isVisible
    }

    private fun setProgressVisibility(isVisible: Boolean) {
        viewBinding?.progress?.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}