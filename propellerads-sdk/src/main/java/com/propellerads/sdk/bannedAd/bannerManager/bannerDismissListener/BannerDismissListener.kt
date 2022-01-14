package com.propellerads.sdk.bannedAd.bannerManager.bannerDismissListener

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannedAd.ui.IAdBanner
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.utils.Logger
import java.lang.ref.WeakReference
import java.util.*

internal class BannerDismissListener(
    val onBannerDismissed: (
        UUID, BannerConfig, WeakReference<FragmentManager>
    ) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    private companion object {
        const val TAG = "Banner"
    }

    override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
        if (fragment is IAdBanner) {

            val config = fragment.arguments
                ?.getSerializable(IAdBanner.CONFIG) as? BannerConfig

            val requestUUID = fragment.arguments
                ?.getSerializable(IAdBanner.REQUEST_UUID) as? UUID

            Logger.d("Banner with Config id: ${config?.id} dismissed", TAG)

            if (requestUUID != null && config != null) {
                onBannerDismissed(requestUUID, config, WeakReference(fm))
            }
        }
    }
}