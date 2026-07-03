package com.mena97villalobos.composeplotlib.chart.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Encodes this [ImageBitmap] to PNG bytes with the platform-native encoder
 * (`android.graphics.Bitmap.compress` on Android, Skia `Image.encodeToData` on iOS).
 *
 * @return a self-contained PNG byte array (write it to a file, share sheet, upload, ...).
 */
expect fun ImageBitmap.encodeToChartPng(): ByteArray

/**
 * Snapshots a composed chart (or any composable it is attached to) to an [ImageBitmap] / PNG, so charts
 * can be exported, shared, or embedded as raster images.
 *
 * Obtain one with [rememberChartCaptureController], attach it to the chart with
 * [Modifier.chartCaptureTarget], then call [captureImageBitmap] / [capturePng] from a coroutine once the
 * chart has drawn at least one frame:
 * ```
 * val capture = rememberChartCaptureController()
 * LineChart(spec, modifier = Modifier.chartCaptureTarget(capture))
 * // later, e.g. from an onClick + rememberCoroutineScope():
 * val png = capture.capturePng()
 * ```
 *
 * The capture reflects exactly what the chart drew for its current size, viewport, and selection — markers
 * and overlays included — because it records the composable's own draw commands into a [GraphicsLayer].
 */
@Stable
class ChartCaptureController internal constructor(
    internal val graphicsLayer: GraphicsLayer,
) {
    /** Captures the attached content into an [ImageBitmap]. Call after the chart has composed and drawn. */
    suspend fun captureImageBitmap(): ImageBitmap = graphicsLayer.toImageBitmap()

    /** Captures the attached content and encodes it to PNG bytes via [encodeToChartPng]. */
    suspend fun capturePng(): ByteArray = captureImageBitmap().encodeToChartPng()
}

/** Remembers a [ChartCaptureController] bound to a [rememberGraphicsLayer] for the current composition. */
@Composable
fun rememberChartCaptureController(): ChartCaptureController {
    val layer = rememberGraphicsLayer()
    return remember(layer) { ChartCaptureController(layer) }
}

/**
 * Records the content of the modified composable into [controller]'s graphics layer while still drawing it
 * normally, so [ChartCaptureController.captureImageBitmap] / [ChartCaptureController.capturePng] can snapshot it.
 *
 * @param controller the capture controller to feed; typically from [rememberChartCaptureController].
 */
fun Modifier.chartCaptureTarget(controller: ChartCaptureController): Modifier =
    drawWithContent {
        val layer = controller.graphicsLayer
        layer.record {
            this@drawWithContent.drawContent()
        }
        drawLayer(layer)
    }
