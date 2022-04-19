package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
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

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    init {
        getConfiguration()
    }

    /**
     * Request all Advertising formats
     */
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
            val banners = data.banners.associateBy { it.id }
            val interstitials = data.interstitials.associateBy { it.id }
            _bannersStatus.emit(
                Resource.Success(
                    banners + interstitials
                )
            )
        }
    }

    /**
     * Request a QR code model for the banner config
     */
    override fun getQrCode(banner: IBannerConfig): Flow<Resource<QRCode>> {
        if (banner !is BannerConfig) return emptyFlow()
        return repository.getQRCode(banner.qrCodeRequestUrl)
            .filter { it !is Resource.Loading }
            .retryIfFailed { res, attempt ->
                handleRetry(res, attempt, QR_RETRY_MAX_ATTEMPT, "Get QR exception")
            }
    }

    /**
     * Request image bytes for QR code
     */
    override fun getQrCodeBytes(qrCode: QRCode) =
        repository.getQrCodeBytes(qrCode.generateUrl)
            .filter { it !is Resource.Loading }
            .retryIfFailed { res, attempt ->
                handleRetry(res, attempt, QR_RETRY_MAX_ATTEMPT, "Get QR Image exception")
            }

    /**
     * Check that the QR code was scanned by user
     */
    override fun checkQrCode(bannerId: String, qrCode: QRCode): Flow<Boolean> =
        repository.checkQRCode(qrCode.checkUrl)
            .retryUntilFail(qrCode.checkInterval)
            .filter { it is Resource.Fail }
            .map { true }

    /**
     * Request an Interstitial model with a landing data
     */
    override fun getInterstitialLanding(banner: IBannerConfig): Flow<Resource<InterstitialLanding>> {
        if (banner !is InterstitialConfig) return emptyFlow()
        return repository.getInterstitialLanding(banner.interstitialUrl)
            .filter { it !is Resource.Loading }
            .retryIfFailed { res, attempt ->
                handleRetry(res, attempt, QR_RETRY_MAX_ATTEMPT, "Get QR exception")
            }
    }

    /**
     * Post the success impression
     */
    override fun callbackImpression(url: String) {
        if (url.isEmpty()) {
            Logger.d("Impression callback url is empty")
            return
        }
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
