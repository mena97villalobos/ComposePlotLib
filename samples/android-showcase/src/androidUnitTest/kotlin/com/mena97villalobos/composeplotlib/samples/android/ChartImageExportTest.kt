package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mena97villalobos.composeplotlib.chart.export.encodeToChartPng
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Exercises the Android `encodeToChartPng` actual under native graphics (chart-compose's own unit tests
 * have no Robolectric graphics backend, so the raster-encode path is verified from the sample module).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ChartImageExportTest {
    @Test
    fun encodesBitmapToValidPng() {
        val side = 16
        val bitmap = ImageBitmap(side, side)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(side.toFloat(), side.toFloat()),
        ) {
            drawRect(Color.Red)
        }

        val png = bitmap.encodeToChartPng()

        // PNG magic number: 89 50 4E 47 0D 0A 1A 0A.
        val signature = listOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        assertTrue("PNG should carry content beyond the 8-byte signature", png.size > signature.size)
        assertTrue(
            "Output should start with the PNG signature",
            signature.indices.all { png[it] == signature[it].toByte() },
        )
    }
}
