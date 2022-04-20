package com.propellerads.sdk.api

import android.util.Base64
import com.propellerads.sdk.api.dto.*
import com.propellerads.sdk.utils.Logger
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.toByteString
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
                    id = "download_now_button",
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
                )
            ),
            banners = if (deviceType == DeviceTypeReq.OTHER) emptyList() else listOf(
                BannerRes(
                    id = "qr_code_1",
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
                        dismissTimerValue = 20,
                        dismissTimerVisibility = false,
                        interval = 40,
                        timeout = 5,
                        frequency = 3,
                        capping = 120,
                    )
                ),
                BannerRes(
                    id = "qr_code_2",
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
                        dismissTimerValue = 5,
                        dismissTimerVisibility = false,
                        interval = 10,
                        timeout = 5,
                        frequency = 3,
                        capping = 50,
                    )
                )
            ),
            interstitials = if (deviceType == DeviceTypeReq.TV) emptyList() else listOf(
                InterstitialRes(
                    id = "interstitial_test",
                    interstitialUrl = "https://www.knowtop.top/",
//                    interstitialUrl = "https://www.knowtop.top/test-stitial",
//                    interstitialUrl = "https://teu.myappluck.com/pydBRp?keyword=propeller&cost={cost}&currency=usd&external_id=\${SUBID}&creative_id={bannerid}&ad_campaign_id={campaignid}&source={zoneid}",
                    settings = InterstitialSettingsRes(
                        interval = 20,
                        timeout = 5,
                        frequency = 2,
                        capping = 120,
                        showCrossTimer = 5,
                        landingLoadTimeout = 5,
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

    override suspend fun getQRCode(url: String): QRCodeRes {
        delay(1000)

        val isRandomException = false
        if (isRandomException && Random.nextBoolean()) {
            throw HttpException(
                Response.error<OkRes>(500, "{}".toResponseBody())
            )
        }

        return QRCodeRes(
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

    private val qrTest =
        "iVBORw0KGgoAAAANSUhEUgAAAK4AAACuCAAAAACKZ2kyAAAAAmJLR0QA/4ePzL8AAAI5SURBVHja7dw7UsMwEIBhnYGCI+YG3MFXgIaCFmY4AuMCXGTomWFofIKUVKag0ciydtcvKfK/VcaJrc9FNtqVHDdcVTi4cOHChQsXLly4cA/FfT6p4t2/iHNu4r5d9L1P3RhPCu6dU8XjEu6LbowTXLjVc3+nk8m3x/WuPYQHwhhx3xIZ6xYu3ANxuyaIfn1uH47RzeY24YhtyI0b4+nNe8/jtuHJDVy4cFtdZkjdBFy4cOvnxvOU+BIuXLg+t2+DuMQzQ7xEjyeFkHsJx+g3L36WcDPUanDr5n6cJ+N1Le7D9Bjnm01aepbsNUpk+3cg4cItiNvdq+Irtfo1nRP+40c3RrvPMqDILWvVEi7cLFwnNsDFT4xeKj+WWImDCxdu6dzRVcUV9xQ3fvLUYbhwK+OmHMqWeXz3i3jyrDkDXLhwN+YOli6+mO/k3LR28QMX7pVwlXNy+XsuZgZxJuHgwoVbFtcyGZDLBHs1sXSKAxfu9XFTEwV7D105PZ+fGeDChbs1N/6zryw6Uk1A3V3ChXsUrvj8Rvyqyvpe25yHCxdufq6txWaoBUwZEi7c6rmmLZv5uTM2xIrtCLGBMSiLFbhwa+MqH/MQv+fyBjFTY2/hQzRw4ZbJlR9eLIpregpb3sRmqSbgwoWb5Cp1Yi4xzhngwoVbKNe0rhbnzllXgwu3Au6Mv/7THZX3kW1T/MCFC3cZ197SS21RV3ba9+xAwoWbm2v6E/z83KIDLly4cOHChQsXLtyauX9guFj88i3DOAAAAABJRU5ErkJggg=="

    override suspend fun getQRCodeBitmap(url: String): ResponseBody {
        delay(1000)
        val qrBytesString = Base64.decode(qrTest, Base64.DEFAULT).toByteString()
        return qrBytesString.toResponseBody()
    }

    override suspend fun getInterstitialLanding(url: String): InterstitialLandingRes {
        delay(1000)

        return InterstitialLandingRes(
            success = true,
            landingUrl = "https://www.google.com",
            impressionUrl = "https://www.google.com",
            isExternalLanding = true,
        )
    }
}