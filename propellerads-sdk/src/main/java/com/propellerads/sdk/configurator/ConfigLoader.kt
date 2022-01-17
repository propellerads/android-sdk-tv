package com.propellerads.sdk.configurator

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.provider.adId.IAdIdProvider
import com.propellerads.sdk.provider.deviceType.IDeviceTypeProvider
import com.propellerads.sdk.provider.publisherId.IPublisherIdProvider
import com.propellerads.sdk.repository.*
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    }

    private val _widgetsStatus = MutableStateFlow<WidgetConfigStatus>(WidgetConfigStatus.Loading)
    override val widgetsStatus: Flow<WidgetConfigStatus>
        get() = _widgetsStatus

    private val _bannersStatus = MutableStateFlow<BannerConfigStatus>(BannerConfigStatus.Loading)
    override val bannersStatus: Flow<BannerConfigStatus>
        get() = _bannersStatus

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
                .collect { resource ->
                    resource.dataOrNull()?.let { data ->
                        handleWidgetsRes(data.widgets)
                        handleBannersRes(data.banners)
                    }
                }
        }
    }

    private suspend fun handleWidgetsRes(widgets: List<WidgetConfig>) {
        _widgetsStatus.emit(WidgetConfigStatus.Success(widgets))
    }

    private suspend fun handleBannersRes(banners: List<BannerConfig>) {
        // should be refactored when new banners type implementation will be required
        val qrCodes = getQrCodesForBanners(banners)
        val qrBanners: Map<String, IBannerConfig> = banners
            .mapNotNull { banner ->
                val id = banner.id
                val qr = qrCodes[id]
                qr?.let { id to QRBannerConfig(banner, qr) }
            }
            .toMap()

        _bannersStatus.emit(
            BannerConfigStatus.Success(qrBanners)
        )
    }

    private suspend fun getQrCodesForBanners(banners: List<BannerConfig>): Map<String, QRCodeSettings> =
        coroutineScope {
            val qrCodesRes = banners.map { banner ->
                async {
                    banner.id to getQrCode(banner)
                }
            }
            qrCodesRes
                .awaitAll()
                .mapNotNull { pair ->
                    pair.second?.let {
                        pair.first to it
                    }
                }
                .toMap()
        }

    private suspend fun getQrCode(banner: BannerConfig): QRCodeSettings? =
        repository.getQRCode(banner.qrCodeBackendUrl)
            .filter { it !is Resource.Loading }
            .retryIfFailed { res, attempt ->
                true.also {
                    val delayMs = getBackoffDelay(attempt)
                    Logger.d("Get QR exception: ${res.message}; Try again in: $delayMs")
                    delay(delayMs)
                }
            }
            .first()
            .dataOrNull()

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
