package com.propellerads.sdk.api.dto

import com.propellerads.sdk.provider.deviceType.DeviceType
import com.propellerads.sdk.repository.Mappable
import com.propellerads.sdk.repository.OK
import com.propellerads.sdk.repository.WidgetAppearance
import com.propellerads.sdk.repository.WidgetConfig

internal data class SettingsRes(
    val widgets: List<WidgetRes>
) : Mappable<List<WidgetConfig>> {
    override fun map() = widgets.map(WidgetRes::map)
}

internal data class WidgetRes(
    val id: String,
    val zone: Long,
    val experimentBranchId: Long,
    val targetUrl: String,  // link to open in browser
    val impressionUrl: String,  // link to notify server about click
    val settings: WidgetSettingsRes,
) : Mappable<WidgetConfig> {
    override fun map() = WidgetConfig(id, targetUrl, impressionUrl, settings.map())
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
        buttonLabel,
        buttonLabelSize,
        buttonLabelColor,
        isButtonLabelBold,
        isButtonLabelItalic,
        buttonLabelShadowColor,
        buttonRadius,
        buttonColors,
        buttonLabelAllCaps,
        horizontalPadding,
        verticalPadding,
    )
}

internal object OkRes : Mappable<OK> {
    override fun map() = OK
}

internal enum class DeviceTypeReq {
    TV, OTHER
}

internal fun DeviceType.toDeviceTypeReq(): DeviceTypeReq = when (this) {
    DeviceType.SMARTPHONE -> DeviceTypeReq.OTHER
    DeviceType.TV -> DeviceTypeReq.TV
}