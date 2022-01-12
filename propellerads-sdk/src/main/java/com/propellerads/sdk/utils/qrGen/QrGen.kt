package com.propellerads.sdk.utils.qrGen

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.propellerads.sdk.utils.Colors
import com.propellerads.sdk.utils.qrGen.encoder.BarcodeEncoder

internal object QrGen {

    private val barcodeEncoder = BarcodeEncoder()

    fun generate(content: String, color: String): Bitmap? = try {
        val intColor = Colors.from(color)
        barcodeEncoder.encodeBitmap(
            content,
            BarcodeFormat.QR_CODE,
            400,
            intColor
        )
    } catch (e: Exception) {
        null
    }
}