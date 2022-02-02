package com.propellerads.sdk.configuration

import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.repository.WidgetConfig
import kotlinx.coroutines.flow.Flow

internal interface IWidgetLoader {

    val widgetsStatus: Flow<Resource<Map<String, WidgetConfig>>>
}