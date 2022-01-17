package com.propellerads.sdk.configurator

import kotlinx.coroutines.flow.Flow

internal interface IConfigLoader {

    val widgetsStatus: Flow<WidgetConfigStatus>

    val bannersStatus: Flow<BannerConfigStatus>

    fun impressionCallback(url: String)
}