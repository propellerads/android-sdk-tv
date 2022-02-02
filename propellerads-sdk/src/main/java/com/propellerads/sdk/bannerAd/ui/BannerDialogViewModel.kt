package com.propellerads.sdk.bannerAd.ui

import androidx.lifecycle.ViewModel
import com.propellerads.sdk.configuration.IQRCodeLoader
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

internal class BannerDialogViewModel : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val qrCodeLoader: IQRCodeLoader = DI.configLoader

    private val _dismissFlow = MutableStateFlow(false)
    val dismissFlow: StateFlow<Boolean>
        get() = _dismissFlow

    private val qrCodeFlow: MutableStateFlow<QRCode?> = MutableStateFlow(null)

    val qrCodeImageFlow: Flow<Resource<ByteArray>> = qrCodeFlow
        .filterNotNull()
        .flatMapLatest(::getQRCodeBytes)

    fun setBannerConfig(bannerConfig: IBannerConfig) {
        startAutoDismissTimeout(bannerConfig.appearance)
        if (bannerConfig.qrCodeRequestUrl != null) {
            getQrCode(bannerConfig)
        }
    }

    private fun getQrCode(bannerConfig: IBannerConfig) {
        qrCodeLoader.getQrCode(bannerConfig)
        launch {
            qrCodeLoader.qrCodesStatus
                .mapNotNull { resource ->
                    (resource[bannerConfig.id] as? Resource.Success)?.data
                }
                .collect { qrCode ->
                    qrCodeFlow.emit(qrCode)
                    startQRCodeChecking(bannerConfig.id, qrCode)
                }
        }
    }

    private suspend fun getQRCodeBytes(qrCode: QRCode): Flow<Resource<ByteArray>> =
        qrCodeLoader
            .getQrCodeBytes(qrCode)
            .stateIn(this)

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

    private var qrCodeCheckingJob: Job? = null
    private fun startQRCodeChecking(bannerId: String, qrCode: QRCode) {
        qrCodeCheckingJob?.cancel()
        qrCodeCheckingJob = launch {
            // start checking after the generate was successful
            qrCodeImageFlow
                .filter { it is Resource.Success }
                .flatMapLatest {
                    qrCodeLoader.checkQrCode(bannerId, qrCode)
                }
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