package com.mena97villalobos.composeplotlib.chart.export

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

/** Encodes via `android.graphics.Bitmap.compress(PNG)` at full quality (PNG is lossless). */
actual fun ImageBitmap.encodeToChartPng(): ByteArray {
    val stream = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
