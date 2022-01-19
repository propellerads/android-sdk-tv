package com.propellerads.sdk.repository

import com.propellerads.sdk.bannerAd.ui.IBannerConfig
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
    override val id: String,
    override val qrCodeRequestUrl: String,
    override val appearance: BannerAppearance,
    override val impressionConfig: ImpressionConfig,
) : IBannerConfig

internal data class ImpressionConfig(
    val interval: Long, // time between ad impressions (ms)
    val timeout: Long,  // the first ad impression timeout (ms)
    val frequency: Int, // max ad impression frequency during time capping
    val capping: Long,   // time interval for frequency measurement (ms)
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
    TOP,
    CENTER,
    BOTTOM,
}

internal data class QRCode(
    val checkUrl: String,
    val generateUrl: String,
    val refreshUrl: String,     // not used in POC
    val qrCodeTtl: Long,        // not used in POC
    val linksExpiredAt: Long,   // not used in POC
    val checkInterval: Long,
) : Serializable

internal object OK