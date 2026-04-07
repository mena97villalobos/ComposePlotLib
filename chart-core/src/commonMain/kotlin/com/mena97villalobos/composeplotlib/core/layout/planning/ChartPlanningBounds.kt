package com.mena97villalobos.composeplotlib.core.layout.planning

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Pass 1 — canvas space and visible domain ([ChartLayoutInput]).
 *
 * Later passes derive [com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan.plotBounds] and ticks from this.
 */
data class PlanningBounds(
    val canvasBounds: RectD,
    val viewport: ChartViewport,
)

fun ChartLayoutInput.toPlanningBounds(): PlanningBounds = PlanningBounds(canvasBounds = canvasBounds, viewport = viewport)
