package com.propellerads.sdk.bannedAd.ui

import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.BannerConfig
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.qrGen.QrGen

internal fun ViewBinding.configure(config: BannerConfig) = when (this) {
    is PropellerBannerQrBinding -> applyStyle(config)
    else -> Unit
}

internal fun PropellerBannerQrBinding.applyStyle(config: BannerConfig) {
    val appearance = config.appearance
    title.text = appearance.titleLabel
    title.setTextColor(Colors.from(appearance.titleColor))
    description.text = appearance.descriptionLabel
    description.setTextColor(Colors.from(appearance.descriptionColor))
    extraDescription.text = appearance.extraDescriptionLabel
    extraDescription.setTextColor(Colors.from(appearance.extraDescriptionColor))
    root.setBackgroundColor(Colors.from(appearance.backgroundColor))
    qrCode.apply {
        val qrBitmap = QrGen.generate(
            config.targetUrl,
            appearance.qrCodeColor
        )
        scaleType = ImageView.ScaleType.FIT_XY
        setImageBitmap(qrBitmap)
    }
}