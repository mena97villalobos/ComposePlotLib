package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import kotlin.math.max
import kotlin.math.min

/** Which axes a range brush constrains its selection to. */
enum class ChartBrushAxis {
    /** Select an x interval spanning the full visible y range (time-range / band brush). */
    Horizontal,

    /** Select a y interval spanning the full visible x range. */
    Vertical,

    /** Free 2-D box selection over both axes. */
    Both,
}

/**
 * A rectangular selection in **domain** space produced by a range brush / box selection, so callers can
 * zoom to it, filter data to it, or drive linked charts.
 *
 * @property xRange selected interval on the x axis, in domain units.
 * @property yRange selected interval on the y axis, in domain units.
 */
data class ChartRangeSelection(
    val xRange: ClosedRangeD,
    val yRange: ClosedRangeD,
)

/**
 * Builds a normalized [ChartRangeSelection] from two domain-space corners (drag start and end), applying the
 * brush [axis] constraint: [ChartBrushAxis.Horizontal] replaces the y span with [fullY], [ChartBrushAxis.Vertical]
 * replaces the x span with [fullX], and [ChartBrushAxis.Both] keeps both dragged spans. Corners may arrive in any
 * order — each axis is reordered so `start <= endInclusive`.
 *
 * @param x0 first corner x (domain). @param y0 first corner y (domain).
 * @param x1 second corner x (domain). @param y1 second corner y (domain).
 * @param axis which axes the brush constrains.
 * @param fullX full visible x range, used to span the unconstrained axis.
 * @param fullY full visible y range, used to span the unconstrained axis.
 */
fun chartRangeSelectionFromDomainCorners(
    x0: Double,
    y0: Double,
    x1: Double,
    y1: Double,
    axis: ChartBrushAxis,
    fullX: ClosedRangeD,
    fullY: ClosedRangeD,
): ChartRangeSelection {
    val x = ClosedRangeD(min(x0, x1), max(x0, x1))
    val y = ClosedRangeD(min(y0, y1), max(y0, y1))
    return when (axis) {
        ChartBrushAxis.Horizontal -> ChartRangeSelection(x, fullY)
        ChartBrushAxis.Vertical -> ChartRangeSelection(fullX, y)
        ChartBrushAxis.Both -> ChartRangeSelection(x, y)
    }
}
