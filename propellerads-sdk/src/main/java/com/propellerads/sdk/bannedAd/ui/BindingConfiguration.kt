package com.propellerads.sdk.bannedAd.ui

import androidx.viewbinding.ViewBinding
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerAppearance
import com.propellerads.sdk.utils.Colors

internal fun ViewBinding.configure(appearance: BannerAppearance) = when (this) {
    is PropellerBannerQrBinding -> applyStyle(appearance)
    else -> Unit
}

internal fun PropellerBannerQrBinding.applyStyle(appearance: BannerAppearance) {
    title.text = appearance.titleLabel
    title.setTextColor(Colors.from(appearance.titleColor))
    description.text = appearance.descriptionLabel
    description.setTextColor(Colors.from(appearance.descriptionColor))
    extraDescription.text = appearance.extraDescriptionLabel
    extraDescription.setTextColor(Colors.from(appearance.extraDescriptionColor))
    root.setBackgroundColor(Colors.from(appearance.backgroundColor))
//  todo: generate qrCode
}