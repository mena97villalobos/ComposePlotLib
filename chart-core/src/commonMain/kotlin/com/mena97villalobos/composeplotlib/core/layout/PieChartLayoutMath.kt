package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.min

/** Center, outer radius, and optional inner radius (donut) for a pie laid out in `plotBounds`. */
data class PieRadialLayout(
    val center: OffsetD,
    val outerRadius: Double,
    val innerRadius: Double?,
)

/** Shared pie/donut geometry for [ChartPlanner.planPieChart] and [com.mena97villalobos.composeplotlib.core.selection.PieHitResolver]. */
object PieChartLayoutMath {
    /** First slice starts at 12 o’clock; degrees match [DrawCommand.Arc] / Compose `drawArc`. */
    const val START_ANGLE_DEGREES: Double = -90.0

    /** Outer radius as a fraction of `min(plot width, plot height)`. */
    const val OUTER_RADIUS_FRACTION: Double = 0.45

    fun layout(
        plotBounds: RectD,
        holeRadiusRatio: Double?,
    ): PieRadialLayout {
        val outer = min(plotBounds.width, plotBounds.height) * OUTER_RADIUS_FRACTION
        val hole = holeRadiusRatio?.coerceIn(0.0, 0.999) ?: 0.0
        val inner = if (hole > 0.0) outer * hole else null
        val center =
            OffsetD(
                x = plotBounds.left + plotBounds.width / 2.0,
                y = plotBounds.top + plotBounds.height / 2.0,
            )
        return PieRadialLayout(center = center, outerRadius = outer, innerRadius = inner)
    }
}
