package com.propellerads.sdk.repository

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
)

internal object OK