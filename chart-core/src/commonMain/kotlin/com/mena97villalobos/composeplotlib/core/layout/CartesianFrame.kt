package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Result of the cartesian **final layout** pass: plot rect, transforms, chrome, and resolved ticks.
 * Built by [com.mena97villalobos.composeplotlib.core.layout.planning.CartesianLayoutPlanner].
 */
internal data class CartesianFrame(
    val canvasBounds: RectD,
    val viewport: ChartViewport,
    val plotBounds: RectD,
    val transform: ChartTransform,
    val reservedRegions: ChartReservedRegions,
    val xTicks: List<AxisTick>,
    val yTicks: List<AxisTick>,
    val legendEntries: List<LegendEntryPlan>,
)
