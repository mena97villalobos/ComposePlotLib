package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/**
 * [ChartLayoutPlan] for a waterfall chart plus the planned bars (for hit-testing) and active selection.
 *
 * @property bars Geometry in **draw order**; hit-testing uses [barsForHitTest].
 */
data class WaterfallChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection? = null,
    val bars: List<PlannedWaterfallBar> = emptyList(),
) {
    /** Topmost-first order for tap hit-testing (reverse of painter z-order). */
    fun barsForHitTest(): List<PlannedWaterfallBar> = bars.asReversed()
}
