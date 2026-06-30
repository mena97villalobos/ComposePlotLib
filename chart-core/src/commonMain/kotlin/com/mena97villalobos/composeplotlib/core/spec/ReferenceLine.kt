package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** Whether a [ReferenceLine] is a constant-Y (horizontal) or constant-X (vertical) threshold. */
enum class ReferenceOrientation {
    /** A horizontal line at a fixed Y [ReferenceLine.value] (e.g. a zero baseline or budget target). */
    Horizontal,

    /** A vertical line at a fixed X [ReferenceLine.value] (e.g. a "today" marker on a time axis). */
    Vertical,
}

/**
 * A threshold / reference line drawn across the plot at a fixed domain [value], tracking pan/zoom.
 *
 * Reference lines are drawn above the series (so the threshold stays visible) and clipped to the plot area, so a
 * line whose [value] is outside the visible viewport is not shown. Add them via the `referenceLines` field on
 * [LineChartSpec], [BarChartSpec], [ScatterChartSpec], or [CombinedCartesianChartSpec].
 *
 * @property orientation [ReferenceOrientation.Horizontal] reads [value] as a Y coordinate; [ReferenceOrientation.Vertical] reads it as X.
 * @property value Domain coordinate of the line (Y for horizontal, X for vertical).
 * @property dashIntervals Alternating on/off lengths in **pixels** for a dashed line; `null` = solid.
 * @property label Optional short text drawn next to the line in [color].
 */
data class ReferenceLine(
    val orientation: ReferenceOrientation,
    val value: Double,
    val color: ChartColor,
    val strokeWidth: Double = 1.0,
    val dashIntervals: List<Double>? = null,
    val label: String? = null,
)
