package com.propellerads.sdk.bannedAd.bannerManager

import androidx.fragment.app.FragmentManager
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
import kotlin.coroutines.CoroutineContext

internal class BannerManager : CoroutineScope, IBannerManager {

    private companion object {
        const val DISPLAY_NOW_THRESHOLD = 500L
        const val TAG = "Banner"
    }

    // todo: restart after app resume
    private val job = Job() // todo: app lifecycle
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val impressionHistory: IImpressionHistory = ImpressionHistory()

    private val impressionTimeCalculator: IImpressionTimeCalculator = ImpressionTimeCalculator(Logger)

    private val bannerDismissListener = BannerDismissListener(::scheduleBannerImpression)

    override fun dispatchConfig(
        config: BannerConfig,
        fm: WeakReference<FragmentManager>
    ) {
        fm.get()?.run {
            unregisterFragmentLifecycleCallbacks(bannerDismissListener)
            registerFragmentLifecycleCallbacks(bannerDismissListener, false)
        }
        scheduleBannerImpression(config, fm)
    }

    private fun scheduleBannerImpression(
        config: BannerConfig,
        fm: WeakReference<FragmentManager>,
    ) {
        val displaySettings = config.impressionConfig
        val history = impressionHistory.get(config.id)
        val nextImpressionTime = calculateNextImpressionTime(displaySettings, history)

        val timeToNextImpression = nextImpressionTime - System.currentTimeMillis()
        if (timeToNextImpression < DISPLAY_NOW_THRESHOLD) {
            displayBanner(config, fm)
        } else {
            Logger.d("Banner for config ${config.id} scheduled in ${timeToNextImpression / 1000} sec", TAG)
            launch {
                delay(timeToNextImpression)
                displayBanner(config, fm)
            }
        }
    }

    private fun calculateNextImpressionTime(
        impressionConfig: ImpressionConfig,
        history: List<Long>,
    ): Long = impressionTimeCalculator
        .calculateNextImpressionTime(
            interval = impressionConfig.interval,
            timeout = impressionConfig.timeout,
            maxFrequency = impressionConfig.frequency,
            capping = impressionConfig.capping,
            history = history
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
}