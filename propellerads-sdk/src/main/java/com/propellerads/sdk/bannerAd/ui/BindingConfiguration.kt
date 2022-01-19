package com.propellerads.sdk.bannerAd.ui

import com.propellerads.sdk.R
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.utils.Colors

internal fun PropellerBannerQrBinding.applyStyle(bannerConfig: IBannerConfig) {
    val appearance = bannerConfig.appearance
    title.text = appearance.titleLabel
    title.setTextColor(Colors.from(appearance.titleColor))
    description.text = appearance.descriptionLabel
    description.setTextColor(Colors.from(appearance.descriptionColor))
    extraDescription.text = appearance.extraDescriptionLabel
    extraDescription.setTextColor(Colors.from(appearance.extraDescriptionColor))
    root.setBackgroundColor(Colors.from(appearance.backgroundColor))
    // todo: display qr from link
    qrCode.setImageResource(R.drawable.mock_qr)
}