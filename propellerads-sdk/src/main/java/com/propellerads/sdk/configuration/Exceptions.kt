package com.propellerads.sdk.configuration

internal object NoPublisherIdException : Exception(
    "Please, provide PropellerAds Publisher ID in the app AndroidManifest.xml"
)

internal object AdSettingsRequestException : Exception(
    "Settings request exception"
)