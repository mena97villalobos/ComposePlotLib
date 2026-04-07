package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/** [ChartLayoutPlan] for a pie or donut chart plus echo of the active [selection] for renderers. */
data class PieChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection?,
)
