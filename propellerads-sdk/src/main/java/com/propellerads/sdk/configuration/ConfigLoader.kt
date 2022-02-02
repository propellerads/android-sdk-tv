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
        const val QR_RETRY_MAX_ATTEMPT = 3L
    }

    private val _widgetsStatus = MutableStateFlow<Resource<Map<String, WidgetConfig>>>(Resource.Loading)
    override val widgetsStatus: Flow<Resource<Map<String, WidgetConfig>>>
        get() = _widgetsStatus

    private val _bannersStatus = MutableStateFlow<Resource<Map<String, IBannerConfig>>>(Resource.Loading)
    override val bannersStatus: Flow<Resource<Map<String, IBannerConfig>>>
        get() = _bannersStatus

    private val _qrCodesStatus = MutableStateFlow<Map<String, Resource<QRCode>>>(emptyMap())
    override val qrCodesStatus: Flow<Map<String, Resource<QRCode>>>
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
                _widgetsStatus.emit(Resource.Fail(NoPublisherIdException))
            }
            return
        }
        launch {
            repository.getConfiguration(publisherId, userId, deviceType)
                .doOnLoading {
                    _widgetsStatus.emit(Resource.Loading)
                }
                .retryIfFailed { resource, attempt ->
                    Logger.d("Get Config exception: ${resource.message}")
                    _widgetsStatus.emit(Resource.Fail(AdSettingsRequestException))
                    _bannersStatus.emit(Resource.Fail(AdSettingsRequestException))
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
            _widgetsStatus.emit(Resource.Success(
                data.widgets.associateBy { it.id }
            ))
            _bannersStatus.emit(Resource.Success(
                data.banners.associateBy { it.id }
            ))
        }
    }

    override fun getQrCode(banner: IBannerConfig) {
        if (banner !is BannerConfig) return
        launch {
            repository.getQRCode(banner.qrCodeRequestUrl)
                .onEach { updateQRCodeStatus(banner.id, it) }
                .filter { it !is Resource.Loading }
                .retryIfFailed { res, attempt ->
                    handleRetry(res, attempt, QR_RETRY_MAX_ATTEMPT, "Get QR exception")
                }
                .collect()
        }
    }

    override fun getQrCodeBytes(qrCode: QRCode) =
        repository.getQrCodeBytes(qrCode.generateUrl)
            .filter { it !is Resource.Loading }
            .retryIfFailed { res, attempt ->
                handleRetry(res, attempt, QR_RETRY_MAX_ATTEMPT, "Get QR Image exception")
            }

    override fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean> =
        repository.checkQRCode(qrCode.checkUrl)
            .retryUntilFail(qrCode.checkInterval)
            .filter { it is Resource.Fail }
            .onEach {
                // drop used QR code status
                updateQRCodeStatus(bannerId, Resource.Loading)
            }
            .map { true }

    private val updateStatusMutex = Mutex()
    private suspend fun updateQRCodeStatus(bannerId: String, status: Resource<QRCode>) {
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
                .retryIfFailed { res, attempt ->
                    handleRetry(res, attempt, Long.MAX_VALUE, "Post Impression exception")
                }
                .collect()
        }
    }

    private suspend fun handleRetry(res: Resource.Fail, attempt: Long, maxAttempt: Long, logMsg: String) =
        (attempt < maxAttempt).also {
            if (it) {
                val delayMs = getBackoffDelay(attempt)
                Logger.d("$logMsg: ${res.message}; Try again in: $delayMs")
                delay(delayMs)
            }
        }

    private fun getBackoffDelay(attempt: Long) =
        min((attempt + 1) * REQUEST_TIMEOUT_MS_MIN, REQUEST_TIMEOUT_MS_MAX)
}
