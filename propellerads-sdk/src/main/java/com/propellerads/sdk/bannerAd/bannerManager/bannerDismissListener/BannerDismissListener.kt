package com.propellerads.sdk.bannerAd.bannerManager.bannerDismissListener

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannerAd.ui.IBanner
import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.utils.Logger
import java.lang.ref.WeakReference
import java.util.*

internal class BannerDismissListener(
    val onBannerDismissed: (
        UUID, IBannerConfig, WeakReference<FragmentManager>
    ) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    private companion object {
        const val TAG = "Banner"
    }

    override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
        if (fragment is IBanner) {

            val config = fragment.arguments
                ?.getSerializable(IBannerConfig.CONFIG) as? IBannerConfig

            val requestUUID = fragment.arguments
                ?.getSerializable(IBannerConfig.REQUEST_UUID) as? UUID

            Logger.d("Banner with Config id: ${config?.bannerId} dismissed", TAG)

            if (requestUUID != null && config != null) {
                onBannerDismissed(requestUUID, config, WeakReference(fm))
            }
        }
    }
}