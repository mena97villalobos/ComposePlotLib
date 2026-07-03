package com.mena97villalobos.composeplotlib.chart.export

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/** Encodes via Skia (`Image.encodeToData(PNG)`), the same rasterizer Compose uses to draw on iOS. */
actual fun ImageBitmap.encodeToChartPng(): ByteArray {
    val image = Image.makeFromBitmap(asSkiaBitmap())
    val data =
        image.encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode chart image to PNG.")
    return data.bytes
}
