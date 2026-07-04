package com.mena97villalobos.composeplotlib.chart.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Visual chrome for chart canvas rendering: plot frame, legend panel, and clipping.
 * Pass a custom instance to [com.mena97villalobos.composeplotlib.chart.LineChart] /
 * [com.mena97villalobos.composeplotlib.chart.BarChart] via `chromeTheme`, or use [chartChromeTheme] for Material 3 defaults.
 */
data class ChartChromeTheme(
    /** Stroke around the plot area; set [plotBorderStrokeWidthPx] to `0` to hide. */
    val plotBorderColor: Color,
    /** Width of the plot border stroke in pixels; `0` hides the border. */
    val plotBorderStrokeWidthPx: Float,
    /** Filled behind legend entries; `null` skips the fill. */
    val legendBackgroundColor: Color?,
    /** Outline for the legend region; `null` or zero width skips. */
    val legendBorderColor: Color?,
    /** Width of the legend outline stroke in pixels; `0` (or a null [legendBorderColor]) skips it. */
    val legendBorderStrokeWidthPx: Float,
    /** Corner radius of the legend panel background/outline in pixels. */
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
        /** Neutral defaults when no Material theme is available (e.g. tests). Alias of [Light]. */
        val Default: ChartChromeTheme
            get() = Light

        /** Light-mode preset: soft light-gray plot border, no legend panel fill. Material-theme-independent. */
        val Light: ChartChromeTheme
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

        /** Dark-mode preset: translucent-white plot border and a subtle translucent legend panel. */
        val Dark: ChartChromeTheme
            get() =
                ChartChromeTheme(
                    plotBorderColor = Color(0x33FFFFFF),
                    plotBorderStrokeWidthPx = 1f,
                    legendBackgroundColor = Color(0x1FFFFFFF),
                    legendBorderColor = Color(0x33FFFFFF),
                    legendBorderStrokeWidthPx = 1f,
                    legendCornerRadiusPx = 6f,
                    clipCanvasToCanvasBounds = true,
                    clipLegendToLegendBounds = true,
                    layerCompositingForTranslucentGroups = false,
                )
    }
}

/**
 * Picks a [ChartChromeTheme] preset by mode: [ChartChromeTheme.Dark] when [darkTheme] (defaults to the system
 * setting via [isSystemInDarkTheme]), else [ChartChromeTheme.Light]. Unlike [chartChromeTheme] (which derives
 * chrome from the ambient `MaterialTheme.colorScheme`), this uses the fixed light/dark presets — handy when a chart
 * is not inside a Material theme or you want mode-fixed chrome.
 */
@Composable
@ReadOnlyComposable
fun chartChromeThemeAuto(darkTheme: Boolean = isSystemInDarkTheme()): ChartChromeTheme = if (darkTheme) ChartChromeTheme.Dark else ChartChromeTheme.Light

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
