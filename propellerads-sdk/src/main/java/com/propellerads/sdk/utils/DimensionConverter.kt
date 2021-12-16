package com.propellerads.sdk.utils

import android.content.res.Resources
import android.util.TypedValue

internal val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

internal val Number.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

internal fun Number.pxToDp() = this.toFloat() / Resources.getSystem().displayMetrics.density

internal fun Number.pxToSp() = this.toFloat() / Resources.getSystem().displayMetrics.scaledDensity