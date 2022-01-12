package com.propellerads.sdk.widget

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.propellerads.sdk.configurator.AdConfigStatus
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class PropellerBannerRequest(
    private val adId: String,
    lifecycle: Lifecycle,
    fragmentManager: FragmentManager,
) : LifecycleEventObserver {

    private companion object {
        const val TAG = "Banner"
    }

    private val job = Job()
    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = job + Dispatchers.Main
    }

    private val weakFM = WeakReference(fragmentManager)

    @Volatile
    private var isRequestExecuted = false

    init {
        Logger.d("Created ${this::class.simpleName} with adId: $adId", TAG)
        lifecycle.addObserver(this)
    }

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) = when (event) {
        Lifecycle.Event.ON_RESUME -> onLifecycleResume()
        Lifecycle.Event.ON_PAUSE -> onLifecyclePause()
        else -> Unit
    }

    private fun onLifecycleResume() {
        if (!isRequestExecuted) {
            obtainConfiguration()
        }
    }

    private fun obtainConfiguration() {
        coroutineScope.launch {
            DI.adConfigurator.status
                .collect(::handleConfigurationStatus)
        }
    }

    private fun handleConfigurationStatus(status: AdConfigStatus) {
        if (status is AdConfigStatus.Success) {
            status.config.banners
                .firstOrNull { it.id == adId }
                ?.let { config ->
                    handleAdConfiguration(config)
                    isRequestExecuted = true
                }
        }
    }

    private fun handleAdConfiguration(config: BannerConfig) {
        Logger.d("Banner config for adId: $adId dispatched to ${DI.bannerManager::class.simpleName}", TAG)
        DI.bannerManager.dispatchConfig(config, weakFM)
    }

    private fun onLifecyclePause() {
        job.cancelChildren()
    }
}