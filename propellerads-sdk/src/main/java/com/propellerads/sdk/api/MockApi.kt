package com.propellerads.sdk.api

import com.propellerads.sdk.api.dto.DeviceTypeReq
import com.propellerads.sdk.api.dto.OkRes
import com.propellerads.sdk.api.dto.SettingsRes
import com.propellerads.sdk.api.dto.WidgetRes
import com.propellerads.sdk.api.dto.WidgetSettingsRes
import kotlinx.coroutines.delay

internal class MockApi : IApi {

    override suspend fun getSettings(
        publisherId: String,
        userId: String,
        deviceType: DeviceTypeReq,
    ): SettingsRes {

        delay(1000)

        return SettingsRes(
            widgets = listOf(
                WidgetRes(
                    id = "widget2",
                    zone = 140,
                    experimentBranchId = 220,
                    targetUrl = "http://google.com",
                    impressionUrl = "https://domain.com/test",
                    settings = WidgetSettingsRes(
                        buttonLabel = "Hello Ad Button",
                        buttonLabelSize = 18,
                        buttonLabelColor = "#AE4A60",
                        isButtonLabelBold = false,
                        isButtonLabelItalic = false,
                        buttonLabelShadowColor = "",
                        buttonRadius = 4,
                        buttonColors = listOf("#4AAE98"),
                        buttonLabelAllCaps = false,
                    )
                )
            )
        )
    }

    override suspend fun impressionCallback(url: String): OkRes {

        delay(1000)

        return OkRes
    }
}

