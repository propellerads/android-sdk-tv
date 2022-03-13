package com.propellerads.sdk.bannerAd.bannerManager

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.propellerads.sdk.bannerAd.bannerManager.bannerDismissListener.BannerDismissListener
import com.propellerads.sdk.bannerAd.bannerManager.impressionHistory.IImpressionHistory
import com.propellerads.sdk.bannerAd.bannerManager.impressionHistory.ImpressionHistory
import com.propellerads.sdk.bannerAd.bannerManager.impressionTimeCalculator.IImpressionTimeCalculator
import com.propellerads.sdk.bannerAd.bannerManager.impressionTimeCalculator.ImpressionTimeCalculator
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.bannerAd.ui.base.display
import com.propellerads.sdk.bannerAd.ui.interstitial.IInterstitialConfig
import com.propellerads.sdk.bannerAd.ui.interstitial.InterstitialDialog
import com.propellerads.sdk.bannerAd.ui.qr.IQRBannerConfig
import com.propellerads.sdk.bannerAd.ui.qr.QRBannerDialog
import com.propellerads.sdk.repository.ImpressionConfig
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
    private val activeConfigs = mutableMapOf<UUID, IBannerConfig>()

    // used to cancel scheduled impressions if the request is revoked
    private val scheduledImpressions = mutableMapOf<UUID, Job>()

    // used to calculate next impressions based on the history of previous impressions
    private val impressionHistory: IImpressionHistory = ImpressionHistory()

    // used to calculate the next impression time
    private val impressionTimeCalculator: IImpressionTimeCalculator = ImpressionTimeCalculator(Logger)

    // used to receive banner dismiss callback
    private val dismissListener = BannerDismissListener(::onBannerDismissed)

    // used to notify subscribers about banner state change
    private val bannersStateFlow = MutableSharedFlow<Pair<UUID, Boolean>>()

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @Synchronized
    override fun dispatchConfig(
        requestUUID: UUID,
        bannerConfig: IBannerConfig,
        uniqueSuffix: String,
        fm: WeakReference<FragmentManager>,
    ) {
        if (!activeConfigs.containsKey(requestUUID)) {
            fm.get()?.registerFragmentLifecycleCallbacks(dismissListener, false)
            activeConfigs[requestUUID] = bannerConfig
            scheduleBannerImpression(requestUUID, bannerConfig, uniqueSuffix, fm, isNewConfig = true)
        } else {
            Logger.d("Already dispatched. Config id: ${bannerConfig.id}", TAG)
        }
    }

    @Synchronized
    override fun revokeConfig(
        requestUUID: UUID,
        uniqueSuffix: String,
        fm: WeakReference<FragmentManager>,
    ) {
        activeConfigs
            .remove(requestUUID)
            ?.let { bannerConfig ->
                Logger.d("Banner revoked. Config id: ${bannerConfig.id}", TAG)
            }

        scheduledImpressions
            .remove(requestUUID)
            ?.cancel()

        fm.get()?.unregisterFragmentLifecycleCallbacks(dismissListener)
    }

    private fun scheduleBannerImpression(
        requestUUID: UUID,
        bannerConfig: IBannerConfig,
        uniqueSuffix: String,
        fm: WeakReference<FragmentManager>,
        isNewConfig: Boolean = false,
    ) {
        synchronized(this) {
            // Check that Config is still active
            // (for banners scheduled from displayBanner())
            if (!activeConfigs.containsKey(requestUUID)) return
        }

        val displaySettings = bannerConfig.impressionConfig
        val history = impressionHistory.get(bannerConfig.id, uniqueSuffix)
        val nextImpressionTime = calculateNextImpressionTime(displaySettings, history, isNewConfig)

        // The next impression is not possible
        nextImpressionTime ?: return

        val timeToNextImpression = nextImpressionTime - System.currentTimeMillis()
        if (timeToNextImpression < DISPLAY_NOW_THRESHOLD) {
            checkConditionsAndDisplayBanner(requestUUID, bannerConfig, uniqueSuffix, fm)
        } else {
            Logger.d("Banner for config ${bannerConfig.id} scheduled in ${timeToNextImpression / 1000.0} sec", TAG)
            scheduledImpressions[requestUUID] = launch {
                delay(timeToNextImpression)
                checkConditionsAndDisplayBanner(requestUUID, bannerConfig, uniqueSuffix, fm)
            }
        }
    }

    private fun calculateNextImpressionTime(
        impressionConfig: ImpressionConfig,
        history: List<Long>,
        addForcedTimeout: Boolean = false,
    ): Long? = impressionTimeCalculator
        .calculateNextImpressionTime(
            interval = impressionConfig.interval,
            timeout = impressionConfig.timeout,
            maxFrequency = impressionConfig.frequency,
            capping = impressionConfig.capping,
            history = history,
            addForcedTimeout = addForcedTimeout,
            currentTime = System.currentTimeMillis(),
        )

    private fun checkConditionsAndDisplayBanner(
        requestUUID: UUID,
        config: IBannerConfig,
        uniqueSuffix: String,
        fragmentManager: WeakReference<FragmentManager>,
    ) {
        val bannerId = config.id
        val fm = fragmentManager.get()

        if (fm == null || fm.isStateSaved || fm.isDestroyed) {
            Logger.d("FragmentManager is saved or destroyed; Config id: $bannerId", TAG)
            return
        }

        // if previous banner is still visible then:
        // - skip current impression
        // - add skipped impression to the history to prevent infinity loop
        // - schedule the new banner impression
        if (fm.findFragmentByTag(bannerId) != null) {
            Logger.d("Banner with the same id ($bannerId) is still visible", TAG)
            updateHistoryAndScheduleNextBanner(requestUUID, config, uniqueSuffix, fragmentManager)
            return
        }

        displayBanner(requestUUID, config, fm)
        updateHistoryAndScheduleNextBanner(requestUUID, config, uniqueSuffix, fragmentManager)
    }

    private fun displayBanner(
        requestUUID: UUID,
        config: IBannerConfig,
        fragmentManager: FragmentManager,
    ) {
        onBannerStateChanged(requestUUID, isShow = true)

        Logger.d("Banner for config id ${config.id} is displayed", TAG)

        when (config) {
            is IQRBannerConfig -> QRBannerDialog
            is IInterstitialConfig -> InterstitialDialog
            else -> null
        }?.display(requestUUID, config, fragmentManager)
    }

    private fun updateHistoryAndScheduleNextBanner(
        requestUUID: UUID,
        config: IBannerConfig,
        uniqueSuffix: String,
        fragmentManager: WeakReference<FragmentManager>,
    ) {
        impressionHistory.add(config.id, uniqueSuffix, System.currentTimeMillis())
        scheduleBannerImpression(requestUUID, config, uniqueSuffix, fragmentManager, false)
    }

    private fun onBannerStateChanged(requestUUID: UUID, isShow: Boolean) {
        launch {
            bannersStateFlow.emit(requestUUID to isShow)
        }
    }

    private fun onBannerDismissed(requestUUID: UUID) {
        onBannerStateChanged(requestUUID, isShow = false)
    }

    override fun subscribeOnBannerStateChange(requestUUID: UUID) =
        bannersStateFlow
            .filter { it.first == requestUUID }
            .map { it.second }

    override fun onStateChanged(
        source: LifecycleOwner, event: Lifecycle.Event
    ) {
        if (event == Lifecycle.Event.ON_STOP) {
            job.cancelChildren()
        }
    }
}