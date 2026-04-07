package com.mena97villalobos.composeplotlib.chart.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Visual chrome for chart canvas rendering: plot frame, legend panel, and clipping.
 * Pass a custom instance to [com.mena97villalobos.composeplotlib.chart.LineChart] /
 * [com.mena97villalobos.composeplotlib.chart.BarChart] via [chromeTheme], or use [chartChromeTheme] for Material 3 defaults.
 */
data class ChartChromeTheme(
    /** Stroke around the plot area; set [plotBorderStrokeWidthPx] to `0` to hide. */
    val plotBorderColor: Color,
    val plotBorderStrokeWidthPx: Float,
    /** Filled behind legend entries; `null` skips the fill. */
    val legendBackgroundColor: Color?,
    /** Outline for the legend region; `null` or zero width skips. */
    val legendBorderColor: Color?,
    val legendBorderStrokeWidthPx: Float,
    val legendCornerRadiusPx: Float,
    /** Clip drawing to [com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan.canvasBounds]. */
    val clipCanvasToCanvasBounds: Boolean,
    /** Clip legend content to [com.mena97villalobos.composeplotlib.core.layout.ChartReservedRegions.legendBounds]. */
    val clipLegendToLegendBounds: Boolean,
    /**
     * When true, [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Group] with fractional alpha uses a
     * canvas save-layer so overlapping translucent children blend as one layer (slightly more expensive).
     */
    val layerCompositingForTranslucentGroups: Boolean = false,
) {
    companion object {
        /** Neutral defaults when no Material theme is available (e.g. tests). */
        val Default: ChartChromeTheme
            get() =
                ChartChromeTheme(
                    plotBorderColor = Color(0xFFE0E0E0),
                    plotBorderStrokeWidthPx = 1f,
                    legendBackgroundColor = null,
                    legendBorderColor = null,
                    legendBorderStrokeWidthPx = 0f,
                    legendCornerRadiusPx = 4f,
                    clipCanvasToCanvasBounds = true,
                    clipLegendToLegendBounds = true,
                    layerCompositingForTranslucentGroups = false,
                )
    }
}

/**
 * Material 3–aware chrome: subtle plot outline, soft legend panel, and safe clipping.
 */
@Composable
@ReadOnlyComposable
fun chartChromeTheme(): ChartChromeTheme {
    val scheme = MaterialTheme.colorScheme
    return ChartChromeTheme(
        plotBorderColor = scheme.outlineVariant.copy(alpha = 0.45f),
        plotBorderStrokeWidthPx = 1f,
        legendBackgroundColor = scheme.surfaceContainerHighest.copy(alpha = 0.55f),
        legendBorderColor = scheme.outlineVariant.copy(alpha = 0.4f),
        legendBorderStrokeWidthPx = 1f,
        legendCornerRadiusPx = 6f,
        clipCanvasToCanvasBounds = true,
        clipLegendToLegendBounds = true,
        layerCompositingForTranslucentGroups = false,
    )
}
