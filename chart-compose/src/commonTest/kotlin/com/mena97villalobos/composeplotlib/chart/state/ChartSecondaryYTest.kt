package com.mena97villalobos.composeplotlib.chart.state

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.ViewportConstraints
import com.mena97villalobos.composeplotlib.core.viewport.ViewportState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartSecondaryYTest {
    private fun state(): ChartViewportState {
        val domain = ChartViewport(ClosedRangeD(0.0, 10.0), ClosedRangeD(0.0, 10.0))
        val start = ViewportState(viewport = domain, constraints = ViewportConstraints()).withViewport(domain, domain)
        return ChartViewportState(initialViewportState = start, domain = domain)
    }

    @Test
    fun secondaryYDefaultsToNull() {
        assertNull(state().secondaryYViewport)
    }

    @Test
    fun setAndResetSecondaryY() {
        val s = state()
        s.updateSecondaryYViewport(ClosedRangeD(0.0, 100.0))
        assertEquals(ClosedRangeD(0.0, 100.0), s.secondaryYViewport)
        s.resetSecondaryY()
        assertNull(s.secondaryYViewport)
    }

    @Test
    fun panShiftsTheSecondaryWindow() {
        val s = state()
        s.updateSecondaryYViewport(ClosedRangeD(0.0, 100.0))
        s.panSecondaryY(10.0)
        assertEquals(ClosedRangeD(10.0, 110.0), s.secondaryYViewport)
    }

    @Test
    fun zoomShrinksTheSpanAroundPivot() {
        val s = state()
        s.updateSecondaryYViewport(ClosedRangeD(0.0, 100.0))
        s.zoomSecondaryY(scale = 2.0, pivot = 50.0) // zoom in 2x around the midpoint
        val range = s.secondaryYViewport!!
        assertEquals(50.0, range.span, 1e-6)
        assertEquals(50.0, range.midpoint, 1e-6)
    }

    @Test
    fun panAndZoomAreNoOpsUntilRangeIsSet() {
        val s = state()
        s.panSecondaryY(10.0)
        s.zoomSecondaryY(2.0, 5.0)
        assertNull(s.secondaryYViewport)
    }
}
