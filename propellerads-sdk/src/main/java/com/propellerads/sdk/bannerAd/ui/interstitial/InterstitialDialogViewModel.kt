package com.propellerads.sdk.bannerAd.ui.interstitial

import android.os.Build
import androidx.annotation.RequiresApi
import com.propellerads.sdk.bannerAd.ui.base.BaseDialogViewModel
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.configuration.ICallbackHandler
import com.propellerads.sdk.configuration.IInterstitialLoader
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.InterstitialLanding
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class InterstitialDialogViewModel : BaseDialogViewModel() {

    private val interstitialLoader: IInterstitialLoader = DI.configLoader
    private val callbackHandler: ICallbackHandler = DI.configLoader

    private val _viewCommandFlow = MutableStateFlow<InterstitialCommand?>(null)
    val viewCommandFlow: StateFlow<InterstitialCommand?>
        get() = _viewCommandFlow

    private var landingData: InterstitialLanding? = null

    @Volatile
    private var isPageFinished = false

    @Volatile
    private var isPageFailed = false

    fun setConfig(config: IInterstitialConfig) {
        getInterstitialLanding(config)

        val appearance = config.appearance
        startShowCrossTimer(appearance.showCrossTimer)
        startLoadingTimer(appearance.loadingTimeout)
    }

    private fun getInterstitialLanding(config: IBannerConfig) {
        launch {
            interstitialLoader.getInterstitialLanding(config)
                .collect(::handleLandingDataResponse)
        }
    }

    private fun handleLandingDataResponse(resource: Resource<InterstitialLanding>) {
        val data = (resource as? Resource.Success)?.data
        val isSuccess = data?.isSuccess ?: false
        val url = data?.landingUrl ?: ""
        if (isSuccess && url.isNotEmpty()) {
            landingData = data
            launch {
                postViewCommand(InterstitialCommand.LoadUrl(url))
            }
        } else {
            dismissBanner()
        }
    }

    private fun startShowCrossTimer(timerValue: Long) {
        if (timerValue == 0L) return
        val showTime = System.currentTimeMillis() + timerValue

        launch {
            while (isActive) {
                delay(1000)
                // Compare with the current time to prevent the timer being paused
                // when the App in the background
                if (System.currentTimeMillis() >= showTime) {
                    postViewCommand(InterstitialCommand.SetCrossVisibility(true))
                }
            }
        }
    }

    // dismiss the dialog if landing was not loaded during the timeout
    private fun startLoadingTimer(timerValue: Long) {
        if (timerValue == 0L) return
        val dismissTime = System.currentTimeMillis() + timerValue

        launch {
            while (isActive) {
                delay(1000)

                // Keep the checks order. Failed page is also finished !!!

                if (isPageFailed) dismissBanner()

                if (isPageFinished) break

                if (System.currentTimeMillis() >= dismissTime) {
                    dismissBanner()
                }
            }
        }

    }

    fun onPageFailed() {
        isPageFailed = true
    }

    fun onPageFinished() {
        if (isPageFinished) return

        isPageFinished = true
        postViewCommand(InterstitialCommand.SetProgressVisibility(false))
        if (!isPageFailed) {
            callbackImpression()
        }
    }

    fun onLandingClicked(clickUrl: String?) {
        val data = landingData ?: return
        val isExternalLanding = data.isExternalLanding
        val uri = if (isExternalLanding) clickUrl else data.landingUrl
        if (uri != null) {
            postViewCommand(InterstitialCommand.OpenBrowser(uri))
        }
        dismissBanner()
    }

    private fun postViewCommand(command: InterstitialCommand) {
        _viewCommandFlow.value = command
    }

    private fun callbackImpression() {
        landingData?.let { data ->
            val impressionUrl = data.impressionUrl
            if (impressionUrl.isNotBlank()) {
                callbackHandler.callbackImpression(impressionUrl)
            }
        }
    }
}