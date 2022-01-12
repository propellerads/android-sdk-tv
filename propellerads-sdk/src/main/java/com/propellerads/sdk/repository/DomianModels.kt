package com.propellerads.sdk.repository

import java.io.Serializable

internal data class AdConfiguration(
    val widgets: List<WidgetConfig>,
    val banners: List<BannerConfig>,
)

internal data class WidgetConfig(
    val id: String,
    val browserUrl: String,
    val impressionUrl: String,
    val appearance: WidgetAppearance,
)

internal data class WidgetAppearance(
    val buttonLabel: String,
    val buttonLabelSize: Int,
    val buttonLabelColor: String,
    val isButtonLabelBold: Boolean,
    val isButtonLabelItalic: Boolean,
    val buttonLabelShadowColor: String,
    val buttonRadius: Int,
    val buttonColors: List<String>,
    val buttonLabelAllCaps: Boolean,
    val horizontalPadding: Int,
    val verticalPadding: Int,
)

internal data class BannerConfig(
    val id: String,
    val targetUrl: String,
    val appearance: BannerAppearance,
    val impressionConfig: ImpressionConfig,
) : Serializable

internal data class ImpressionConfig(
    val interval: Int, // time between ad impressions (ms)
    val timeout: Int,  // the first ad impression timeout (ms)
    val frequency: Int, // max ad impression frequency during time capping
    val capping: Int,   // time interval for frequency measurement (ms)
) : Serializable

internal data class BannerAppearance(
    val layoutTemplate: String,
    val gravity: BannerGravity,
    val isFullWidth: Boolean,
    val isFullHeight: Boolean,
    val hasRoundedCorners: Boolean,
    val titleLabel: String,
    val descriptionLabel: String,
    val extraDescriptionLabel: String,
    val titleColor: String,
    val descriptionColor: String,
    val extraDescriptionColor: String,
    val backgroundColor: String,
    val qrCodeColor: String,
    val dismissTimerValue: Long,
    val dismissTimerVisibility: Boolean,
) : Serializable

internal enum class BannerGravity {
    TOP, CENTER, BOTTOM
}

internal object OK