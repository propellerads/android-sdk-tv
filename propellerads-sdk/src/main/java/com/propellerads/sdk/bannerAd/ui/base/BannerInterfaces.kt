package com.propellerads.sdk.bannerAd.ui.base

import androidx.fragment.app.FragmentManager
import com.propellerads.sdk.repository.ImpressionConfig
import java.io.Serializable
import java.util.*

internal interface IBanner {

    fun show(fragmentManager: FragmentManager)
}

internal interface IBannerConfig : Serializable {

    companion object {
        const val REQUEST_UUID = "REQUEST_UUID"
        const val CONFIG = "CONFIG"
    }

    val id: String
    val impressionConfig: ImpressionConfig
}

internal interface IBannerBuilder {
    fun build(
        requestUUID: UUID,
        config: IBannerConfig
    ): IBanner
}

internal fun IBannerBuilder.display(
    requestUUID: UUID,
    config: IBannerConfig,
    fragmentManager: FragmentManager,
) = this.build(requestUUID, config).show(fragmentManager)