package com.propellerads.sdk.bannedAd.bannerManager

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.repository.BannerConfig
import java.lang.ref.WeakReference

internal interface IBannerManager {

    fun dispatchConfig(
        config: BannerConfig,
        fm: WeakReference<FragmentManager>,
    )
}