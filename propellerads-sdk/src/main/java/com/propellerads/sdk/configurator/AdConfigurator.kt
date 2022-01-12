package com.propellerads.sdk.configurator

import com.propellerads.sdk.provider.adId.IAdIdProvider
import com.propellerads.sdk.provider.deviceType.IDeviceTypeProvider
import com.propellerads.sdk.provider.publisherId.IPublisherIdProvider
import com.propellerads.sdk.repository.AdConfiguration
import com.propellerads.sdk.repository.IPropellerRepository
import com.propellerads.sdk.repository.OK
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext

internal class AdConfigurator(
    private val repository: IPropellerRepository,
    private val adIdProvider: IAdIdProvider,
    private val publisherIdProvider: IPublisherIdProvider,
    private val deviceTypeProvider: IDeviceTypeProvider,
) : IAdConfigurator, CoroutineScope {

    private companion object {
        const val REQUEST_TIMEOUT_MS = 5_000L
    }

    private val _status = MutableStateFlow<AdConfigStatus>(AdConfigStatus.Loading)
    override val status: Flow<AdConfigStatus>
        get() = _status

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
                _status.emit(AdConfigStatus.Error(NoPublisherIdException))
            }
            return
        }
        launch {
            repository.getSettings(publisherId, userId, deviceType)
                .collect(::handleSettingsRes)
        }
    }

    private suspend fun handleSettingsRes(resource: Resource<AdConfiguration>) {
        when (resource) {
            is Resource.Loading -> {
                _status.emit(AdConfigStatus.Loading)
            }
            is Resource.Success -> {
                _status.emit(AdConfigStatus.Success(resource.data))
            }
            is Resource.Fail -> {
                Logger.d("Ad Settings exception: ${resource.exception?.message ?: "Unknown exception"}")
                _status.emit(AdConfigStatus.Error(AdSettingsRequestException))
                tryAgain(resource.exception, REQUEST_TIMEOUT_MS, "Ad Settings", ::getConfiguration)
            }
        }
    }

    override fun impressionCallback(url: String) {
        launch {
            Logger.d("Invoke impression callback: $url")
            repository.impressionCallback(url)
                .collect { res ->
                    handleImpressionRes(url, res)
                }
        }
    }

    private suspend fun handleImpressionRes(url: String, resource: Resource<OK>) {
        if (resource is Resource.Fail) {
            Logger.d("Impression Callback exception: ${resource.exception?.message ?: "Unknown exception"}")
            tryAgain(resource.exception, REQUEST_TIMEOUT_MS, "Impression callback") {
                impressionCallback(url)
            }
        }
    }

    private suspend fun tryAgain(exception: Exception?, delay: Long, name: String, block: () -> Unit) {
        // retry in case of Network problems
        if (exception is IOException) {
            Logger.d("Delay ${delay}ms")
            delay(delay)
            Logger.d("Try request $name again")
            block()
        }
    }
}

internal sealed class AdConfigStatus {
    object Loading : AdConfigStatus()
    data class Success(val config: AdConfiguration) : AdConfigStatus()
    data class Error(val exception: Exception?) : AdConfigStatus()
}

internal object NoPublisherIdException : Exception(
    "Please, provide PropellerAds Publisher ID in the app AndroidManifest.xml"
)

internal object AdSettingsRequestException : Exception(
    "Settings request exception"
)