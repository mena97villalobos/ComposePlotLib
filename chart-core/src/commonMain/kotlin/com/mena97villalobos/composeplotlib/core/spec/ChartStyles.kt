package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** Stroke appearance for line series polylines. */
data class LineStyle(
    val color: ChartColor = ChartColor.Blue,
    val strokeWidth: Double = 2.0,
)

/** Optional vertex markers along a line series. */
data class PointStyle(
    val visible: Boolean = true,
    val radius: Double = 3.0,
    val color: ChartColor? = null,
)

/** Fill and width for bars in a [com.mena97villalobos.composeplotlib.core.model.BarGroup]. */
data class BarStyle(
    val color: ChartColor = ChartColor.Green,
    val barWidth: Double = 16.0,
)

/** Tick label color for an axis. */
data class AxisLabelStyle(
    val color: ChartColor = ChartColor.Gray,
)

/** Baseline line along the axis. */
data class AxisLineStyle(
    val visible: Boolean = true,
    val color: ChartColor = ChartColor.LightGray,
)

/** Stroke for major grid lines. */
data class GridLineStyle(
    val color: ChartColor = ChartColor.LightGray,
    val strokeWidth: Double = 1.0,
    /** When non-null, grid segments are stroked with this pattern (pixel intervals, Compose-style dashes). */
    val strokeStyle: StrokeStyle? = null,
)

/** Legend text and color swatch styling. */
data class LegendStyle(
    val textColor: ChartColor = ChartColor.Gray,
    val indicatorRadius: Double = 4.0,
    /** When non-null, legend entry text is truncated with `…` for layout and drawing (per-entry strings). */
    val labelMaxChars: Int? = null,
)

/** Crosshair / highlight styling for the selected point or bar. */
data class SelectionStyle(
    val lineColor: ChartColor = ChartColor.Red,
    val lineStrokeWidth: Double = 1.0,
    val pointColor: ChartColor = ChartColor.Red,
    val pointRadius: Double = 5.0,
)
