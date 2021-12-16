package com.propellerads.sdk.configurator

import kotlinx.coroutines.flow.Flow

internal interface IAdConfigurator {

    val state: Flow<AdConfigState>

    fun impressionCallback(url: String)
}