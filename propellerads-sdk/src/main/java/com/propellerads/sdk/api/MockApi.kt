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
                    id = "test_widget_1",
                    zone = 140,
                    experimentBranchId = 220,
                    targetUrl = "http://google.com",
                    impressionUrl = "https://domain.com/test",
                    settings = WidgetSettingsRes(
                        buttonLabel = "Download Now",
                        buttonLabelSize = 18,
                        buttonLabelColor = "#FFFFFF",
                        isButtonLabelBold = true,
                        isButtonLabelItalic = false,
                        buttonLabelShadowColor = "#40000000",
                        buttonRadius = 0,
                        buttonColors = listOf("#B2D96D", "#789F32"),
                        buttonLabelAllCaps = false,
                        horizontalPadding = 54,
                        verticalPadding = 7,
                    )
        ),
                WidgetRes(
                    id = "test_widget_2",
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
                        horizontalPadding = 0,
                        verticalPadding = 0,
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

