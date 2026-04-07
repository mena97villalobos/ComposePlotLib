package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice

/**
 * Shared angular math for [ChartPlanner.planPieChart] and [com.mena97villalobos.composeplotlib.core.selection.PieHitResolver]:
 * optional **per-gap** degrees between consecutive drawable slices (draw order), with total gap budget `n * gap`
 * for `n` contributing slices on the circle.
 */
object PieSweep {
    /** Coerces requested gap so `contributingCount * gap < 360°`. */
    fun effectiveSliceGapDegrees(
        contributingCount: Int,
        requestedGapDegrees: Double,
    ): Double {
        if (contributingCount <= 0) return 0.0
        val g = requestedGapDegrees.coerceAtLeast(0.0)
        val maxPer = (360.0 - FpMath.EPS) / contributingCount
        return g.coerceAtMost(maxPer)
    }

    /** Sweep angle (degrees) for each contributing slice after subtracting gap budget from the full circle. */
    fun sweepsDegrees(
        contributing: List<Pair<Int, PieChartSlice>>,
        totalValue: Double,
        sliceGapDegrees: Double,
    ): List<Double> {
        val n = contributing.size
        if (n == 0 || totalValue <= 0.0) return emptyList()
        val gap = effectiveSliceGapDegrees(n, sliceGapDegrees)
        val available = 360.0 - n * gap
        return contributing.map { (_, s) -> (s.value / totalValue) * available }
    }
}
