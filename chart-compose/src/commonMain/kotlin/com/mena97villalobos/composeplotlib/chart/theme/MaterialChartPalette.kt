package com.mena97villalobos.composeplotlib.chart.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Common [MaterialTheme.colorScheme] tokens mapped to portable [ChartColor] values for series, axes,
 * surfaces, and legend text in chart specs. This is a convenience snapshot, not a full design-token bridge.
 *
 * Includes tonal accents ([primaryFixed], [secondaryFixed]) and surfaces ([surface], [surfaceVariant]) for
 * fills and multi-series cycling via [seriesAccentColors] / [withMaterialSeriesColors].
 *
 * Build specs in composable scope with [materialChartPalette] or convert arbitrary Compose colors with
 * [toChartColor].
 */
data class MaterialChartPalette(
    /** Primary accent (Material `primary`); first series color and default selection highlight. */
    val primary: ChartColor,
    /** Content color drawn on top of [primary] (Material `onPrimary`). */
    val onPrimary: ChartColor,
    /** Tonal container for [primary] (Material `primaryContainer`); softer fills/backgrounds. */
    val primaryContainer: ChartColor,
    /** Second series accent (Material `secondary`). */
    val secondary: ChartColor,
    /** Third series accent (Material `tertiary`). */
    val tertiary: ChartColor,
    /** Error/emphasis accent (Material `error`); last color in [seriesAccentColors]. */
    val error: ChartColor,
    /** Primary foreground text/marks over surfaces (Material `onSurface`). */
    val onSurface: ChartColor,
    /** Muted foreground for axis/legend text (Material `onSurfaceVariant`). */
    val onSurfaceVariant: ChartColor,
    /** Border/divider color (Material `outline`). */
    val outline: ChartColor,
    /** Subtle border/gridline color (Material `outlineVariant`). */
    val outlineVariant: ChartColor,
    /** Plot / sheet backgrounds and fills (Material `surface`). */
    val surface: ChartColor,
    val surfaceVariant: ChartColor,
    /** Tonal series accents (Material `primaryFixed` / `secondaryFixed`). */
    val primaryFixed: ChartColor,
    val secondaryFixed: ChartColor,
)

/**
 * Maps a Compose [Color] in any color space to sRGB ARGB and wraps it as [ChartColor].
 *
 * @param fallback used when this is `Color.Unspecified` (Material placeholders).
 */
fun Color.toChartColor(fallback: ChartColor = ChartColor.Black): ChartColor =
    if (isUnspecified) {
        fallback
    } else {
        ChartColor(toArgb().toUInt().toLong())
    }

/**
 * Maps a Material 3 [ColorScheme] to [MaterialChartPalette] using [toChartColor].
 */
fun ColorScheme.toMaterialChartPalette(): MaterialChartPalette =
    MaterialChartPalette(
        primary = primary.toChartColor(),
        onPrimary = onPrimary.toChartColor(),
        primaryContainer = primaryContainer.toChartColor(),
        secondary = secondary.toChartColor(),
        tertiary = tertiary.toChartColor(),
        error = error.toChartColor(),
        onSurface = onSurface.toChartColor(),
        onSurfaceVariant = onSurfaceVariant.toChartColor(),
        outline = outline.toChartColor(),
        outlineVariant = outlineVariant.toChartColor(),
        surface = surface.toChartColor(),
        surfaceVariant = surfaceVariant.toChartColor(),
        primaryFixed = primaryFixed.toChartColor(),
        secondaryFixed = secondaryFixed.toChartColor(),
    )

/**
 * Reads the current [MaterialTheme.colorScheme] and returns a [MaterialChartPalette].
 */
@Composable
@ReadOnlyComposable
fun materialChartPalette(): MaterialChartPalette = MaterialTheme.colorScheme.toMaterialChartPalette()
