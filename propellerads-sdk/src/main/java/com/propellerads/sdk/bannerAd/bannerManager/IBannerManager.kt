package com.propellerads.sdk.bannerAd.bannerManager

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import kotlinx.coroutines.flow.Flow
import java.lang.ref.WeakReference
import java.util.*

internal interface IBannerManager {

    fun dispatchConfig(
        requestUUID: UUID,
        bannerConfig: IBannerConfig,
        uniqueSuffix: String,
        fm: WeakReference<FragmentManager>,
    )

    fun revokeConfig(
        requestUUID: UUID,
        uniqueSuffix: String,
        fm: WeakReference<FragmentManager>,
    )

    fun subscribeOnBannerStateChange(
        requestUUID: UUID,
    ): Flow<Boolean>
}