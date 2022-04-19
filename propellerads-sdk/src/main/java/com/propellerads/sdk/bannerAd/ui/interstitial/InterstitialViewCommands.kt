package com.propellerads.sdk.bannerAd.ui.interstitial

sealed class InterstitialCommand {
    class SetCrossVisibility(val isVisible: Boolean) : InterstitialCommand()
    class SetProgressVisibility(val isVisible: Boolean) : InterstitialCommand()
    class LoadUrl(val url: String) : InterstitialCommand()
    class OpenBrowser(val url: String) : InterstitialCommand()
}