package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.flow.Flow

internal interface IBannerLoader {

    val bannersStatus: Flow<Resource<Map<String, IBannerConfig>>>
}