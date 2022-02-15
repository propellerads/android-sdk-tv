package com.propellerads.sdk.configuration

import com.propellerads.sdk.bannerAd.ui.base.IBannerConfig
import com.propellerads.sdk.repository.InterstitialLanding
import com.propellerads.sdk.repository.Resource
import kotlinx.coroutines.flow.Flow

internal interface IInterstitialLoader {

    fun getInterstitialLanding(banner: IBannerConfig): Flow<Resource<InterstitialLanding>>
}