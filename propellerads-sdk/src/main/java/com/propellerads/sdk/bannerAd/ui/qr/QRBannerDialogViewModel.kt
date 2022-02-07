package com.propellerads.sdk.bannerAd.ui.qr

import com.propellerads.sdk.bannerAd.ui.base.BaseDialogViewModel
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.configuration.IQRCodeLoader
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.QRCode
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class QRBannerDialogViewModel : BaseDialogViewModel() {

    private companion object {
        const val TAG = "Banner"
    }

    private val qrCodeLoader: IQRCodeLoader = DI.configLoader

    private val qrCodeFlow: MutableStateFlow<QRCode?> = MutableStateFlow(null)

    val qrCodeImageFlow: Flow<Resource<ByteArray>> = qrCodeFlow
        .filterNotNull()
        .flatMapLatest(::getQRCodeBytes)

    fun setConfig(config: IQRBannerConfig) {
        val timeToDismiss = config.appearance.dismissTimerValue
        startAutoDismissTimeout(timeToDismiss)
        if (config.qrCodeRequestUrl != null) {
            getQrCode(config)
        }
    }

    private fun getQrCode(config: IBannerConfig) {
        qrCodeLoader.getQrCode(config)
        launch {
            qrCodeLoader.qrCodesStatus
                .mapNotNull { resource ->
                    (resource[config.id] as? Resource.Success)?.data
                }
                .collect { qrCode ->
                    qrCodeFlow.emit(qrCode)
                    startQRCodeChecking(config.id, qrCode)
                }
        }
    }

    private suspend fun getQRCodeBytes(qrCode: QRCode): Flow<Resource<ByteArray>> =
        qrCodeLoader
            .getQrCodeBytes(qrCode)
            .stateIn(this)

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
                    Logger.d("Dismiss by QR code", TAG)
                    dismissBanner()
                }
        }
    }
}