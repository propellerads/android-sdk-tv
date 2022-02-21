package com.propellerads.sdk.bannerAd.ui.interstitial

import com.propellerads.sdk.bannerAd.ui.base.BaseDialogViewModel
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.configuration.ICallbackHandler
import com.propellerads.sdk.configuration.IInterstitialLoader
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.InterstitialLanding
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class InterstitialDialogViewModel : BaseDialogViewModel() {

    private val interstitialLoader: IInterstitialLoader = DI.configLoader
    private val callbackHandler: ICallbackHandler = DI.configLoader

    val landingFlow: MutableStateFlow<InterstitialLanding?> = MutableStateFlow(null)
    val isCrossVisible = MutableStateFlow(false)

    private var config: IInterstitialConfig? = null

    fun setConfig(config: IInterstitialConfig) {

        this.config = config
        getInterstitialLanding(config)

        val appearance = config.appearance
        startShowCrossTimer(appearance.showCrossTimer)
    }

    private fun getInterstitialLanding(config: IBannerConfig) {
        launch {
            interstitialLoader.getInterstitialLanding(config)
                .mapNotNull { resource ->
                    (resource as? Resource.Success)?.data
                }
                .collect { landing ->
                    landingFlow.emit(landing)
                }
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
                    isCrossVisible.value = true
                }
            }
        }
    }

    fun callbackImpression() {
        config?.let {
            callbackHandler.callbackImpression(it.impressionUrl)
        }
    }
}