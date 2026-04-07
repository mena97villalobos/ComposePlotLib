package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/** [ChartLayoutPlan] for a radar / spider chart plus echo of the active [selection] for renderers. */
data class RadarChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection?,
)
