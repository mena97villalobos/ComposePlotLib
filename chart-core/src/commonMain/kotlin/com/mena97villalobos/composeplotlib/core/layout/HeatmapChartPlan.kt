package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/**
 * Output of [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planHeatmapChart].
 *
 * @property heatmapCells Resolved cell geometry in draw order (first cell drawn first).
 *   Use [cellsForHitTest] for tap hit-testing.
 */
data class HeatmapChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection? = null,
    val heatmapCells: List<PlannedHeatmapCell> = emptyList(),
) {
    /** Topmost-first order for tap hit-testing (reverse of draw order). */
    fun cellsForHitTest(): List<PlannedHeatmapCell> = heatmapCells.reversed()
}
