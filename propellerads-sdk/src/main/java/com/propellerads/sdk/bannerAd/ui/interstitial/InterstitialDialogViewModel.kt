package com.propellerads.sdk.bannerAd.ui.interstitial

import com.propellerads.sdk.bannerAd.ui.base.BaseDialogViewModel
import com.propellerads.sdk.configuration.ICallbackHandler
import com.propellerads.sdk.di.DI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class InterstitialDialogViewModel : BaseDialogViewModel() {

    private val callbackHandler: ICallbackHandler = DI.configLoader

    val isCrossVisible = MutableStateFlow(false)

    private var config: IInterstitialConfig? = null

    fun setConfig(config: IInterstitialConfig) {
        this.config = config
        val appearance = config.appearance
        startShowCrossTimer(appearance.showCrossTimer)
    }

    private fun startShowCrossTimer(timerValue: Long) {
        if (timerValue == 0L) return

        val showTime = System.currentTimeMillis() + timerValue

        launch {
            while (isActive) {
                delay(1000)
                // Compare with the current time to prevent the timer being interrupted
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