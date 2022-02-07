package com.propellerads.sdk.bannerAd.bannerManager.bannerDismissListener

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannerAd.ui.base.IBanner
import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.utils.Logger
import java.util.*

internal class BannerDismissListener(
    private val onBannerDismissed: (requestUUID: UUID) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    private companion object {
        const val TAG = "Banner"
    }

    override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
        if (fragment is IBanner) {

            val requestUUID = fragment.arguments
                ?.getSerializable(IBannerConfig.REQUEST_UUID) as? UUID

            Logger.d("Banner dismissed. Request UUID: $requestUUID", TAG)

            requestUUID?.let(onBannerDismissed)
        }
    }
}