package com.propellerads.sdk.bannedAd.bannerManager

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.propellerads.sdk.bannedAd.bannerManager.bannerDismissListener.BannerDismissListener
import com.propellerads.sdk.bannedAd.bannerManager.impressionHistory.IImpressionHistory
import com.propellerads.sdk.bannedAd.bannerManager.impressionHistory.ImpressionHistory
import com.propellerads.sdk.bannedAd.bannerManager.impressionTimeCalculator.IImpressionTimeCalculator
import com.propellerads.sdk.bannedAd.bannerManager.impressionTimeCalculator.ImpressionTimeCalculator
import com.propellerads.sdk.bannedAd.ui.AdBannerDialog
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.repository.ImpressionConfig
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.CoroutineContext

internal class BannerManager :
    IBannerManager, CoroutineScope, LifecycleEventObserver {

    private companion object {
        const val DISPLAY_NOW_THRESHOLD = 500L
        const val TAG = "Banner"
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val activeConfigs = mutableMapOf<UUID, BannerConfig>()

    private val impressionHistory: IImpressionHistory = ImpressionHistory()

    private val impressionTimeCalculator: IImpressionTimeCalculator = ImpressionTimeCalculator(Logger)

    private val bannerDismissListener = BannerDismissListener(::scheduleBannerImpression)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @Synchronized
    override fun dispatchConfig(
        requestUUID: UUID,
        config: BannerConfig,
        fm: WeakReference<FragmentManager>
    ) {
        if (!activeConfigs.containsKey(requestUUID)) {
            activeConfigs[requestUUID] = config
            fm.get()?.run {
                unregisterFragmentLifecycleCallbacks(bannerDismissListener)
                registerFragmentLifecycleCallbacks(bannerDismissListener, false)
            }
            scheduleBannerImpression(config, fm, isNewConfig = true)
        } else {
            Logger.d("Already dispatched. Config id: ${config.id}", TAG)
        }
    }

    @Synchronized
    override fun revokeConfig(requestUUID: UUID) {
        activeConfigs.remove(requestUUID)?.let {
            Logger.d("Banner revoked. Config id: ${it.id}", TAG)
        }
    }

    private fun scheduleBannerImpression(
        config: BannerConfig,
        fm: WeakReference<FragmentManager>,
        isNewConfig: Boolean = false,
    ) {
        val displaySettings = config.impressionConfig
        val history = impressionHistory.get(config.id)
        val nextImpressionTime = calculateNextImpressionTime(displaySettings, history, isNewConfig)

        val timeToNextImpression = nextImpressionTime - System.currentTimeMillis()
        if (timeToNextImpression < DISPLAY_NOW_THRESHOLD) {
            displayBanner(config, fm)
        } else {
            Logger.d("Banner for config ${config.id} scheduled in ${timeToNextImpression / 1000.0} sec", TAG)
            launch {
                delay(timeToNextImpression)
                displayBanner(config, fm)
            }
        }
    }

    private fun calculateNextImpressionTime(
        impressionConfig: ImpressionConfig,
        history: List<Long>,
        addForcedTimeout: Boolean = false,
    ): Long = impressionTimeCalculator
        .calculateNextImpressionTime(
            interval = impressionConfig.interval,
            timeout = impressionConfig.timeout,
            maxFrequency = impressionConfig.frequency,
            capping = impressionConfig.capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = System.currentTimeMillis(),
        )

    private fun displayBanner(
        config: BannerConfig,
        fragmentManager: WeakReference<FragmentManager>,
    ) {
        val fm = fragmentManager.get()
        if (fm == null || fm.isStateSaved || fm.isDestroyed) {
            Logger.d("FragmentManager is saved or destroyed; Config id: ${config.id}", TAG)
            return
        }

        Logger.d("Banner for config ${config.id} displayed", TAG)

        AdBannerDialog
            .build(config)
            .show(fm)

        impressionHistory.add(config.id, System.currentTimeMillis())
    }

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) {
        if (event == Lifecycle.Event.ON_STOP) {
            job.cancelChildren()
        }
    }
}