package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/**
 * [ChartLayoutPlan] for a box-and-whisker chart plus the planned boxes (for hit-testing) and active selection.
 *
 * @property boxes Geometry in **draw order**; hit-testing uses [boxesForHitTest].
 */
data class BoxPlotChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection? = null,
    val boxes: List<PlannedBox> = emptyList(),
) {
    /** Topmost-first order for tap hit-testing (reverse of painter z-order). */
    fun boxesForHitTest(): List<PlannedBox> = boxes.asReversed()
}
