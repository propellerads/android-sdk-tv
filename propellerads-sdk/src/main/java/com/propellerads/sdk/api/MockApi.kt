package com.propellerads.sdk.api

import com.propellerads.sdk.api.dto.*
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.delay
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.random.Random

internal class MockApi : IApi {

    override suspend fun getSettings(
        publisherId: String,
        userId: String,
        deviceType: DeviceTypeReq,
    ): SettingsRes {

        delay(1000)

        val isRandomException = false
        if (isRandomException && Random.nextBoolean()) {
            throw IOException("Hello World Exception!")
        }

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
                    id = "test_banner_1",
                    zone = 140,
                    experimentBranchId = 220,
                    qrCodeBackendUrl = "https://propeller.backend/qrCodeBackendUrl",
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
                        dismissTimerValue = 10,
                        dismissTimerVisibility = false,
                        interval = 20,
                        timeout = 5,
                        frequency = 3,
                        capping = 60,
                    )
                ),
                BannerRes(
                    id = "test_banner_2",
                    zone = 140,
                    experimentBranchId = 220,
                    qrCodeBackendUrl = "https://propeller.backend/qrCodeBackendUrl",
                    settings = BannerSettingsRes(
                        layoutTemplate = "qr_code_3_1",
                        positionOnScreen = "center",
                        isFullWidth = true,
                        isFullHeight = false,
                        hasRoundedCorners = true,
                        titleLabel = "Please scan QR code",
                        descriptionLabel = "Use your phone",
                        extraDescriptionLabel = "Hello World!",
                        titleColor = "#3b2e08",
                        descriptionColor = "#6b6246",
                        extraDescriptionColor = "#6e6a5f",
                        backgroundColor = "#edd282",
                        qrCodeColor = "#332601",
                        dismissTimerValue = 5,
                        dismissTimerVisibility = false,
                        interval = 10,
                        timeout = 5,
                        frequency = 3,
                        capping = 50,
                    )
                )
            )
        )
    }

    override suspend fun impressionCallback(url: String): OkRes {
        delay(1000)

        val isRandomException = false
        if (isRandomException && Random.nextBoolean()) {
            throw IOException("Hello World Exception!")
        }

        return OkRes
    }

    override suspend fun getQRCode(url: String): QRCodeSettingsRes {
        delay(1000)

        val isRandomException = false
        if (isRandomException && Random.nextBoolean()) {
            throw HttpException(
                Response.error<OkRes>(500, "{}".toResponseBody())
            )
        }

        return QRCodeSettingsRes(
            checkUrl = "https://propeller.backend/checkUrl",
            generateUrl = "https://propeller.backend/generateUrl",
            refreshUrl = "https://propeller.backend/refreshUrl",
            codeTtl = 60,
            linksExpiredAt = System.currentTimeMillis() / 1000 + 3 * 60 * 60,
            checkUrlInterval = 1,
        )
    }

    var mockCounter = Random.nextInt(7, 13)

    override suspend fun checkQRCode(url: String): OkRes {
        return if (mockCounter == 0) {
            mockCounter = Random.nextInt(8, 13)
            throw HttpException(
                Response.error<OkRes>(404, "{}".toResponseBody())
            )
        } else {
            mockCounter--
            Logger.d("Attempt to QR read: $mockCounter", "Banner")
            OkRes
        }
    }
}