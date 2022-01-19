package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.provider.adId.IAdIdProvider
import com.propellerads.sdk.provider.deviceType.IDeviceTypeProvider
import com.propellerads.sdk.provider.publisherId.IPublisherIdProvider
import com.propellerads.sdk.repository.*
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

internal class ConfigLoader(
    private val repository: IPropellerRepository,
    private val adIdProvider: IAdIdProvider,
    private val publisherIdProvider: IPublisherIdProvider,
    private val deviceTypeProvider: IDeviceTypeProvider,
) : IConfigLoader, CoroutineScope {

    private companion object {
        const val REQUEST_TIMEOUT_MS_MIN = 1_000L
        const val REQUEST_TIMEOUT_MS_MAX = 15_000L
        const val QR_RETRY_MAX_ATTEMPT = 3
    }

    private val _widgetsStatus = MutableStateFlow<WidgetConfigStatus>(WidgetConfigStatus.Loading)
    override val widgetsStatus: Flow<WidgetConfigStatus>
        get() = _widgetsStatus

    private val _bannersStatus = MutableStateFlow<BannerConfigStatus>(BannerConfigStatus.Loading)
    override val bannersStatus: Flow<BannerConfigStatus>
        get() = _bannersStatus

    private val _qrCodesStatus = MutableStateFlow<Map<String, QRCodeStatus>>(emptyMap())
    override val qrCodesStatus: Flow<Map<String, QRCodeStatus>>
        get() = _qrCodesStatus

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    init {
        getConfiguration()
    }

    private fun getConfiguration() {
        val publisherId = publisherIdProvider.getPublisherId()
        val userId = adIdProvider.getAdId()
        val deviceType = deviceTypeProvider.getDeviceType()

        if (publisherId == null) {
            Logger.d(NoPublisherIdException.message!!)
            launch {
                _widgetsStatus.emit(WidgetConfigStatus.Error(NoPublisherIdException))
            }
            return
        }
        launch {
            repository.getConfiguration(publisherId, userId, deviceType)
                .doOnLoading {
                    _widgetsStatus.emit(WidgetConfigStatus.Loading)
                }
                .retryIfFailed { resource, attempt ->
                    Logger.d("Get Config exception: ${resource.message}")
                    _widgetsStatus.emit(WidgetConfigStatus.Error(AdSettingsRequestException))
                    _bannersStatus.emit(BannerConfigStatus.Error(AdSettingsRequestException))
                    (resource.exception is IOException).also {
                        val delayMs = getBackoffDelay(attempt)
                        Logger.d("Try again in: $delayMs")
                        delay(delayMs)
                    }
                }
                .collect(::handleConfigurationRes)
        }
    }

    private suspend fun handleConfigurationRes(resource: Resource<AdConfiguration>) {
        resource.dataOrNull()?.let { data ->
            _widgetsStatus.emit(WidgetConfigStatus.Success(data.widgets))
            _bannersStatus.emit(BannerConfigStatus.Success(
                data.banners.associateBy { it.id }
            ))
        }
    }

    override fun getQrCode(banner: IBannerConfig) {
        if (banner !is BannerConfig) return
        launch {
            repository.getQRCode(banner.qrCodeRequestUrl)
                .onEach { res ->
                    updateQRCodeStatus(banner.id, QRCodeStatus.fromResource(res))
                }
                .filter { it !is Resource.Loading }
                .retryIfFailed { res, attempt ->
                    (attempt < QR_RETRY_MAX_ATTEMPT).also {
                        val delayMs = getBackoffDelay(attempt)
                        Logger.d("Get QR exception: ${res.message}; Try again in: $delayMs")
                        delay(delayMs)
                    }
                }
                .collect()
        }
    }

    override fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean> =
        repository.checkQRCode(qrCode.checkUrl)
            .retryUntilFail(qrCode.checkInterval)
            .filter { it is Resource.Fail }
            .onEach {
                // drop used QR code status
                updateQRCodeStatus(bannerId, QRCodeStatus.Loading)
            }
            .map { true }

    private val updateStatusMutex = Mutex()
    private suspend fun updateQRCodeStatus(bannerId: String, status: QRCodeStatus) {
        updateStatusMutex.withLock {
            val statusMap = _qrCodesStatus.value.toMutableMap()
                .apply { this[bannerId] = status }
            _qrCodesStatus.emit(statusMap)
        }
    }

    override fun impressionCallback(url: String) {
        launch {
            Logger.d("Invoke impression callback: $url")
            repository.impressionCallback(url)
                .retryIfFailed { resource, attempt ->
                    true.also {
                        val delayMs = getBackoffDelay(attempt)
                        Logger.d("Post Impression exception: ${resource.message}; Try again in: $delayMs")
                        delay(delayMs)
                    }
                }
                .collect()
        }
    }

    private fun getBackoffDelay(attempt: Long) =
        min((attempt + 1) * REQUEST_TIMEOUT_MS_MIN, REQUEST_TIMEOUT_MS_MAX)
}
