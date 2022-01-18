package com.propellerads.sdk.bannerAd.ui

import androidx.lifecycle.ViewModel
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.*
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlin.coroutines.CoroutineContext

internal class BannerDialogViewModel : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val _dismissFlow = MutableStateFlow(false)
    val dismissFlow: Flow<Boolean>
        get() = _dismissFlow

    fun setBannerConfig(bannerConfig: QRBannerConfig) {
        startAutoDismissTimeout(bannerConfig.config.appearance)
        startQRCodeChecking(bannerConfig.qrCode)
    }

    private fun startAutoDismissTimeout(appearance: BannerAppearance) {
        val timeToDismiss = appearance.dismissTimerValue
        if (timeToDismiss == 0L) return

        val dismissTime = System.currentTimeMillis() + timeToDismiss

        launch {
            while (isActive) {
                delay(1000)
                // Compare with the current time to prevent the timer being interrupted
                // when the App in the background
                if (System.currentTimeMillis() >= dismissTime) {
                    Logger.d("Dismiss by the Timer", "Banner")
                    dismissBanner()
                }
            }
        }
    }

    private fun startQRCodeChecking(qrCodeSettings: QRCodeSettings) {
        val checkUrl = qrCodeSettings.checkUrl
        val interval = qrCodeSettings.checkInterval

        launch {
            DI.repo.checkQRCode(checkUrl)
                .retryUntilFail(interval)
                .filter { it is Resource.Fail }
                .collect {
                    Logger.d("Dismiss by QR code", "Banner")
                    dismissBanner()
                }
        }
    }

    private fun dismissBanner() {
        job.cancelChildren()
        launch {
            _dismissFlow.emit(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}