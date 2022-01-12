package com.propellerads.sdk.bannedAd.bannerManager.bannerDismissListener

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.bannedAd.ui.IAdBanner
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.utils.Logger
import java.lang.ref.WeakReference

internal class BannerDismissListener(
    val onBannerDismissed: (BannerConfig, WeakReference<FragmentManager>) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    private companion object {
        const val TAG = "Banner"
    }

    override fun onFragmentDetached(fm: FragmentManager, fragment: Fragment) {
        if (fragment is IAdBanner) {

            val config = fragment.arguments
                ?.getSerializable(IAdBanner.CONFIG) as? BannerConfig

            Logger.d("Banner with Config id: ${config?.id} dismissed", TAG)

            if (config != null) {
                onBannerDismissed(config, WeakReference(fm))
            }
        }
    }
}