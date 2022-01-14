package com.propellerads.sdk.bannedAd.ui

import androidx.fragment.app.FragmentManager

interface IAdBanner {

    companion object {
        const val REQUEST_UUID = "REQUEST_UUID"
        const val CONFIG = "CONFIG"
    }

    fun show(fragmentManager: FragmentManager)
}