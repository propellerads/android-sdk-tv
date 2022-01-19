package com.propellerads.sdk.configuration

import kotlinx.coroutines.flow.Flow

internal interface IBannerLoader {

    val bannersStatus: Flow<BannerConfigStatus>
}