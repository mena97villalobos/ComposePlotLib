package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/**
 * @property candles Geometry in **draw order** (earlier entries are beneath later ones at overlaps).
 * Hit-testing should use [candlesForHitTest].
 */
data class CandlestickChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection? = null,
    val candles: List<PlannedCandle> = emptyList(),
) {
    /** Topmost-first order for tap hit-testing (reverse of typical painter z-order). */
    fun candlesForHitTest(): List<PlannedCandle> = candles.forHitTestFromDrawOrder()
}
