package com.propellerads.sdk.bannerAd.ui

import android.graphics.BitmapFactory
import com.propellerads.sdk.databinding.PropellerBannerQrBinding
import com.propellerads.sdk.repository.Resource
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.isVisible

internal fun PropellerBannerQrBinding.applyStyle(bannerConfig: IBannerConfig) {
    val appearance = bannerConfig.appearance
    title.text = appearance.titleLabel
    title.setTextColor(Colors.from(appearance.titleColor))
    description.text = appearance.descriptionLabel
    description.setTextColor(Colors.from(appearance.descriptionColor))
    extraDescription.text = appearance.extraDescriptionLabel
    extraDescription.setTextColor(Colors.from(appearance.extraDescriptionColor))
    root.setBackgroundColor(Colors.from(appearance.backgroundColor))
}

internal fun PropellerBannerQrBinding.setQRImage(resource: Resource<ByteArray>) {
    when (resource) {
        is Resource.Success -> {
            val bytes = resource.data
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            qrCode.setImageBitmap(bitmap)
            qrProgress.isVisible = false
        }
        else -> {
            // ???
        }
    }
}