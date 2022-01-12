package com.propellerads.sdk.api

import com.propellerads.sdk.api.dto.*
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
            ),
            banners = listOf(
                BannerRes(
                    id = "test_banner",
                    zone = 140,
                    experimentBranchId = 220,
                    targetUrl = "https://www.google.com",
                    settings = BannerSettingsRes(
                        layoutTemplate = "qr_code_3_1",
                        positionOnScreen = "bottom",
                        isFullWidth = true,
                        isFullHeight = false,
                        hasRoundedCorners = false,
                        titleLabel = "Confirm you're not a robot",
                        descriptionLabel = "Scan the qr-code with your phone",
                        extraDescriptionLabel = "QR-CAPTCHA",
                        titleColor = "#29BFFF",
                        descriptionColor = "#000000",
                        extraDescriptionColor = "#4D000000",
                        backgroundColor = "#FFFFFF",
                        qrCodeColor = "#789F32",
                        dismissTimerValue = 5_000,
                        dismissTimerVisibility = false,
                        interval = 10_000,
                        timeout = 5_000,
                        frequency = 3,
                        capping = 50_000,
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