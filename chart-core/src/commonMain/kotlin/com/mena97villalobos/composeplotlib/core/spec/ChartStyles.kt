package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Stroke appearance for line series polylines, with optional area fill under the line.
 *
 * @property areaFill Fill color under the line. `null` (default) keeps the original stroke-only behavior.
 * @property areaFillEnd Optional second color for a vertical gradient: [areaFill] at the top fades to
 * [areaFillEnd] at the bottom of the fill region. `null` = flat [areaFill]. Ignored when [areaFill] is null.
 * @property areaFillMode Where the fill is anchored (plot bottom, a baseline value, or a shortfall band below a
 * threshold). Ignored when [areaFill] is null.
 */
data class LineStyle(
    val color: ChartColor = ChartColor.Blue,
    val strokeWidth: Double = 2.0,
    val areaFill: ChartColor? = null,
    val areaFillEnd: ChartColor? = null,
    val areaFillMode: AreaFillMode = AreaFillMode.ToPlotBottom,
)

/** Anchor for a [LineStyle.areaFill] region. */
sealed interface AreaFillMode {
    /** Fill from the line down to the bottom edge of the plot area. */
    data object ToPlotBottom : AreaFillMode

    /** Fill between the line and a fixed domain [value] (e.g. `0.0` for net worth / equity). */
    data class ToBaseline(
        val value: Double,
    ) : AreaFillMode

    /** Fill only the region where the line is **below** [value] (e.g. a cash-flow shortfall band under a floor). */
    data class BelowThreshold(
        val value: Double,
    ) : AreaFillMode
}

/** Glyph drawn for a scatter / line vertex marker. */
enum class MarkerShape {
    Circle,
    Square,
    Triangle,
    Diamond,
}

/**
 * Optional vertex markers along a line or scatter series.
 *
 * @property shape Default glyph for every point; individual scatter points may override it via
 * [com.mena97villalobos.composeplotlib.core.model.ChartPoint.markerShape].
 */
data class PointStyle(
    val visible: Boolean = true,
    val radius: Double = 3.0,
    val color: ChartColor? = null,
    val shape: MarkerShape = MarkerShape.Circle,
)

/** Fill and width for bars in a [com.mena97villalobos.composeplotlib.core.model.BarGroup]. */
data class BarStyle(
    val color: ChartColor = ChartColor.Green,
    val barWidth: Double = 16.0,
)

/**
 * Appearance for a [com.mena97villalobos.composeplotlib.core.model.CandlestickSeries].
 *
 * A candle is bullish when `close >= open` (drawn with [bullColor]) and bearish otherwise ([bearColor]).
 * [bodyWidth] and [wickWidth] are fixed **pixel** sizes (like [BarStyle.barWidth]), not domain-scaled.
 *
 * @property wickColorFollowsBody When true (default) and [wickColor] is null, the wick uses the body color.
 * @property wickColor Explicit wick color; overrides [wickColorFollowsBody] when non-null.
 */
data class CandlestickStyle(
    val bullColor: ChartColor = ChartColor.Green,
    val bearColor: ChartColor = ChartColor.Red,
    val bodyWidth: Double = 8.0,
    val wickWidth: Double = 1.5,
    val wickColorFollowsBody: Boolean = true,
    val wickColor: ChartColor? = null,
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
