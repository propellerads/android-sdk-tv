package com.propellerads.sdk.api.dto

import com.propellerads.sdk.provider.deviceType.DeviceType
import com.propellerads.sdk.repository.*
import java.util.*

internal const val MILLISECONDS = 1000L

internal data class SettingsRes(
    val widgets: List<WidgetRes>?,
    val banners: List<BannerRes>?,
    val interstitials: List<InterstitialRes>?
) : Mappable<AdConfiguration> {
    override fun map() = AdConfiguration(
        widgets = widgets?.map(WidgetRes::map) ?: emptyList(),
        banners = banners?.map(BannerRes::map) ?: emptyList(),
        interstitials = interstitials?.map(InterstitialRes::map) ?: emptyList(),
    )
}

internal data class WidgetRes(
    val id: String,
    val zone: Long,
    val experimentBranchId: Long,
    val targetUrl: String,  // link to open in browser
    val impressionUrl: String,  // link to notify server about click
    val settings: WidgetSettingsRes,
) : Mappable<WidgetConfig> {
    override fun map() = WidgetConfig(
        id = id,
        browserUrl = targetUrl,
        impressionUrl = impressionUrl,
        appearance = settings.map(),
    )
}

internal data class WidgetSettingsRes(
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
) : Mappable<WidgetAppearance> {
    override fun map() = WidgetAppearance(
        buttonLabel = buttonLabel,
        buttonLabelSize = buttonLabelSize,
        buttonLabelColor = buttonLabelColor,
        isButtonLabelBold = isButtonLabelBold,
        isButtonLabelItalic = isButtonLabelItalic,
        buttonLabelShadowColor = buttonLabelShadowColor,
        buttonRadius = buttonRadius,
        buttonColors = buttonColors,
        buttonLabelAllCaps = buttonLabelAllCaps,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
    )
}

internal data class BannerRes(
    val id: String,
    val qrCodeBackendUrl: String,
    val settings: BannerSettingsRes,
) : Mappable<BannerConfig> {
    override fun map() = settings.map().let { (appearance, impressionConfig) ->
        BannerConfig(
            id = id,
            qrCodeRequestUrl = qrCodeBackendUrl,
            appearance = appearance,
            impressionConfig = impressionConfig,
        )
    }
}

internal data class BannerSettingsRes(
    val layoutTemplate: String,
    val positionOnScreen: String,
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
    val dismissTimerValue: Long,
    val dismissTimerVisibility: Boolean,
    val interval: Int,
    val timeout: Int,
    val frequency: Int,
    val capping: Int,
) : Mappable<Pair<BannerAppearance, ImpressionConfig>> {
    override fun map(): Pair<BannerAppearance, ImpressionConfig> {
        val gravity = when (
            positionOnScreen.lowercase(Locale.ENGLISH)
        ) {
            "top" -> BannerGravity.TOP
            "bottom" -> BannerGravity.BOTTOM
            else -> BannerGravity.CENTER
        }
        val appearance = BannerAppearance(
            layoutTemplate = layoutTemplate,
            gravity = gravity,
            isFullWidth = isFullWidth,
            isFullHeight = isFullHeight,
            hasRoundedCorners = hasRoundedCorners,
            titleLabel = titleLabel,
            descriptionLabel = descriptionLabel,
            extraDescriptionLabel = extraDescriptionLabel,
            titleColor = titleColor,
            descriptionColor = descriptionColor,
            extraDescriptionColor = extraDescriptionColor,
            backgroundColor = backgroundColor,
            dismissTimerValue = dismissTimerValue * MILLISECONDS,
            dismissTimerVisibility = dismissTimerVisibility,
        )

        val impression = ImpressionConfig(
            interval = interval * MILLISECONDS,
            timeout = timeout * MILLISECONDS,
            frequency = frequency,
            capping = capping * MILLISECONDS,
        )
        return appearance to impression
    }
}

internal data class QRCodeRes(
    val checkUrl: String,
    val generateUrl: String,
    val refreshUrl: String,
    val codeTtl: Int,
    val linksExpiredAt: Long,
    val checkUrlInterval: Long,
) : Mappable<QRCode> {
    override fun map() = QRCode(
        checkUrl = checkUrl,
        generateUrl = generateUrl,
        refreshUrl = refreshUrl,
        qrCodeTtl = codeTtl * MILLISECONDS,
        linksExpiredAt = linksExpiredAt * MILLISECONDS,
        checkInterval = checkUrlInterval * MILLISECONDS,
    )
}

internal data class InterstitialRes(
    val id: String,
    val interstitialUrl: String,
    val impressionUrl: String,
    val settings: InterstitialSettingsRes,
) : Mappable<InterstitialConfig> {
    override fun map() = settings.map().let { (appearance, impressionConfig) ->
        InterstitialConfig(
            id = id,
            interstitialUrl = interstitialUrl,
            impressionUrl = impressionUrl,
            appearance = appearance,
            impressionConfig = impressionConfig,
        )
    }
}

internal data class InterstitialSettingsRes(
    val interval: Int,
    val timeout: Int,
    val frequency: Int,
    val capping: Int,
    val showCrossTimer: Long,
) : Mappable<Pair<InterstitialAppearance, ImpressionConfig>> {
    override fun map(): Pair<InterstitialAppearance, ImpressionConfig> {
        val appearance = InterstitialAppearance(
            showCrossTimer = showCrossTimer * MILLISECONDS,
        )

        val impression = ImpressionConfig(
            interval = interval * MILLISECONDS,
            timeout = timeout * MILLISECONDS,
            frequency = frequency,
            capping = capping * MILLISECONDS,
        )
        return appearance to impression
    }
}

internal data class InterstitialLandingRes(
    val landingUrl: String,
    val isExternalLanding: Boolean,
) : Mappable<InterstitialLanding> {
    override fun map() = InterstitialLanding(
        landingUrl = landingUrl,
        isExternalLanding = isExternalLanding,
    )
}

internal object OkRes : Mappable<OK> {
    override fun map() = OK
}

internal enum class DeviceTypeReq {
    TV,
    OTHER,
}

internal fun DeviceType.toDeviceTypeReq(): DeviceTypeReq = when (this) {
    DeviceType.SMARTPHONE -> DeviceTypeReq.OTHER
    DeviceType.TV -> DeviceTypeReq.TV
}