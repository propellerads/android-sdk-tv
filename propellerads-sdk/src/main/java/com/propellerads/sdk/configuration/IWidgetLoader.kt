package com.propellerads.sdk.configuration

import kotlinx.coroutines.flow.Flow

internal interface IWidgetLoader {

    val widgetsStatus: Flow<WidgetConfigStatus>
}