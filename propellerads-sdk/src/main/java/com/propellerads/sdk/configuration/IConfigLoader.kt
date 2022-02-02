package com.propellerads.sdk.configuration

internal interface IConfigLoader
    : IWidgetLoader, IBannerLoader, IQRCodeLoader {

    fun impressionCallback(url: String)
}