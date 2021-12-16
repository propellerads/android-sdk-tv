package com.propellerads.sdk.provider.deviceType

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration

internal class DeviceTypeProvider(
    context: Context,
) : IDeviceTypeProvider {

    private val uiModeManager: UiModeManager? =
        context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager

    override fun getDeviceType(): DeviceType = when (uiModeManager?.currentModeType) {
        Configuration.UI_MODE_TYPE_TELEVISION -> DeviceType.TV
        else -> DeviceType.SMARTPHONE
    }
}

internal enum class DeviceType {
    SMARTPHONE, TV
}