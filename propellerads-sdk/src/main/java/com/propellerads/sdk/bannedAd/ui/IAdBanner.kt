package com.propellerads.sdk.bannedAd.ui

import androidx.fragment.app.FragmentManager

interface IAdBanner {

    companion object {
        const val CONFIG = "CONFIG"
    }

    fun show(fragmentManager: FragmentManager)
}