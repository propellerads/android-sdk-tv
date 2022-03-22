package com.propellerads.sdk.widget

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.di.DI
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.utils.Logger
import com.propellerads.sdk.widget.PropellerBannerRequest.Callback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.CoroutineContext

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class PropellerBannerRequest(
    private val adId: String,
    lifecycle: Lifecycle,
    fragmentManager: FragmentManager,
    private val uniqueSuffix: String = "",
    private val callback: Callback = Callback { },
) : LifecycleEventObserver {

    private companion object {
        const val TAG = "Banner"
    }

    private val job = Job()
    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = job + Dispatchers.Main
    }

    private val bannerManager = DI.bannerManager
    private val configLoader = DI.configLoader

    private val requestUUID = UUID.randomUUID()

    private val weakFM = WeakReference(fragmentManager)

    @Volatile
    private var bannerConfig: IBannerConfig? = null

    init {
        Logger.d("Created request with Config id: $adId", TAG)
        lifecycle.addObserver(this)
    }

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) = when (event) {
        Lifecycle.Event.ON_RESUME -> onLifecycleResume()
        Lifecycle.Event.ON_STOP -> onLifecycleStop()
        else -> Unit
    }

    private fun onLifecycleResume() {
        Logger.d("Resume request $requestUUID", TAG)

        bannerConfig?.let(::handleAdConfiguration)
            ?: obtainConfiguration()
    }

    private fun obtainConfiguration() {
        coroutineScope.launch {
            configLoader.bannersStatus
                .collect(::handleConfigurationRes)
        }
    }

    private fun handleConfigurationRes(resource: Resource<Map<String, IBannerConfig>>) {
        if (resource is Resource.Success) {
            bannerConfig = resource.data[adId]?.also {
                handleAdConfiguration(it)
            }
        }
    }

    private fun handleAdConfiguration(bannerConfig: IBannerConfig) {
        Logger.d("Dispatch Banner config id: $adId", TAG)
        bannerManager.dispatchConfig(requestUUID, bannerConfig, uniqueSuffix, weakFM)
        coroutineScope.launch {
            bannerManager
                .subscribeOnBannerStateChange(requestUUID)
                .collect(callback::onBannerStateChanged)
        }
    }

    private fun onLifecycleStop() {
        Logger.d("Cancel request  $requestUUID", TAG)
        job.cancelChildren()

        Logger.d("Revoke banner config id: $adId", TAG)
        bannerManager.revokeConfig(requestUUID, uniqueSuffix, weakFM)
    }

    fun interface Callback {
        fun onBannerStateChanged(isShow: Boolean)
    }
}