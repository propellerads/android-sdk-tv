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

    // used to avoid dispatching duplicated pairs <requestId, config>
    private val activeConfigs = mutableMapOf<UUID, BannerConfig>()

    // used to cancel scheduled impressions if the request is revoked
    private val scheduledImpressions = mutableMapOf<UUID, Job>()

    // used to calculate next impressions based on the history of previous impressions
    private val impressionHistory: IImpressionHistory = ImpressionHistory()

    // used to calculate the next impression time
    private val impressionTimeCalculator: IImpressionTimeCalculator = ImpressionTimeCalculator(Logger)

    // used to schedule the nest impression after previous banner is dismissed
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
            scheduleBannerImpression(requestUUID, config, fm, isNewConfig = true)
        } else {
            Logger.d("Already dispatched. Config id: ${config.id}", TAG)
        }
    }

    @Synchronized
    override fun revokeConfig(requestUUID: UUID) {
        activeConfigs.remove(requestUUID)?.let {
            Logger.d("Banner revoked. Config id: ${it.id}", TAG)
        }
        scheduledImpressions.remove(requestUUID)?.cancel()
    }

    private fun scheduleBannerImpression(
        requestUUID: UUID,
        config: BannerConfig,
        fm: WeakReference<FragmentManager>,
        isNewConfig: Boolean = false,
    ) {
        val displaySettings = config.impressionConfig
        val history = impressionHistory.get(config.id)
        val nextImpressionTime = calculateNextImpressionTime(displaySettings, history, isNewConfig)

        val timeToNextImpression = nextImpressionTime - System.currentTimeMillis()
        if (timeToNextImpression < DISPLAY_NOW_THRESHOLD) {
            displayBanner(requestUUID, config, fm)
        } else {
            Logger.d("Banner for config ${config.id} scheduled in ${timeToNextImpression / 1000.0} sec", TAG)
            scheduledImpressions[requestUUID] = launch {
                delay(timeToNextImpression)
                displayBanner(requestUUID, config, fm)
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
        requestUUID: UUID,
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
            .build(requestUUID, config)
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