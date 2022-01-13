package com.propellerads.sdk.bannedAd.bannerManager

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.repository.BannerConfig
import java.lang.ref.WeakReference
import java.util.*

internal interface IBannerManager {

    fun dispatchConfig(
        requestUUID: UUID,
        config: BannerConfig,
        fm: WeakReference<FragmentManager>,
    )

    fun revokeConfig(requestUUID: UUID)
}