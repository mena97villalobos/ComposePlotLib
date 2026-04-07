package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation

/**
 * @property barSegments Geometry in **draw order** (earlier entries are beneath later ones at overlaps).
 * Hit-testing should use [barSegmentsForHitTest].
 */
data class BarChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection? = null,
    val orientation: BarChartOrientation = BarChartOrientation.Vertical,
    val barSegments: List<PlannedBarSegment> = emptyList(),
) {
    /** Topmost-first order for tap hit-testing (reverse of typical painter z-order). */
    fun barSegmentsForHitTest(): List<PlannedBarSegment> = barSegments.forHitTestFromDrawOrder()
}
