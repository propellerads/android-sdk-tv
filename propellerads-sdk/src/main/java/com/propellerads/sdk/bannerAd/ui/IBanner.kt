package com.propellerads.sdk.bannerAd.ui

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.repository.ImpressionConfig
import java.io.Serializable

internal interface IBanner {

    fun show(fragmentManager: FragmentManager)
}

internal interface IBannerConfig : Serializable {

    companion object {
        const val REQUEST_UUID = "REQUEST_UUID"
        const val CONFIG = "CONFIG"
    }

    val id: String
    val qrCodeRequestUrl: String?
    val appearance: BannerAppearance
    val impressionConfig: ImpressionConfig
}