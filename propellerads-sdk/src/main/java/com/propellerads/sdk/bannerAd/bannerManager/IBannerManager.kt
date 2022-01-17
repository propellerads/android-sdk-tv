package com.propellerads.sdk.bannerAd.bannerManager

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import java.lang.ref.WeakReference
import java.util.*

internal interface IBannerManager {

    fun dispatchConfig(
        requestUUID: UUID,
        bannerConfig: IBannerConfig,
        fm: WeakReference<FragmentManager>,
    )

    fun revokeConfig(requestUUID: UUID)
}