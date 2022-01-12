package com.propellerads.sdk.configurator

import kotlinx.coroutines.flow.Flow

internal interface IAdConfigurator {

    val status: Flow<AdConfigStatus>

    fun impressionCallback(url: String)
}