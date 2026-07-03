package com.mena97villalobos.composeplotlib.chart.state

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.ViewportConstraints
import com.mena97villalobos.composeplotlib.core.viewport.ViewportState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartViewportSyncTest {
    private fun viewport(
        xStart: Double,
        xEnd: Double,
        yStart: Double = 0.0,
        yEnd: Double = 10.0,
    ): ChartViewport = ChartViewport(ClosedRangeD(xStart, xEnd), ClosedRangeD(yStart, yEnd))

    private fun stateFor(domain: ChartViewport): ChartViewportState {
        val start = ViewportState(viewport = domain, constraints = ViewportConstraints()).withViewport(domain, domain)
        return ChartViewportState(initialViewportState = start, domain = domain)
    }

    private fun sig(
        state: ChartViewportState,
        axis: ChartSyncAxis,
    ): List<ClosedRangeD> =
        when (axis) {
            ChartSyncAxis.X -> listOf(state.viewport.xRange)
            ChartSyncAxis.Y -> listOf(state.viewport.yRange)
            ChartSyncAxis.Both -> listOf(state.viewport.xRange, state.viewport.yRange)
        }

    private fun snapshot(
        states: List<ChartViewportState>,
        axis: ChartSyncAxis,
    ): List<List<ClosedRangeD>> = states.map { sig(it, axis) }

    private fun assertRangeClose(
        expected: ClosedRangeD,
        actual: ClosedRangeD,
    ) {
        assertEquals(expected.start, actual.start, 1e-6)
        assertEquals(expected.endInclusive, actual.endInclusive, 1e-6)
    }

    @Test
    fun `x sync mirrors the leader window onto followers`() {
        val a = stateFor(viewport(0.0, 10.0))
        val b = stateFor(viewport(0.0, 10.0))
        val group = listOf(a, b)
        val previous = snapshot(group, ChartSyncAxis.X)

        a.zoomTo(xSpan = 2.0, ySpan = a.viewport.yRange.span, centerX = 3.0, centerY = a.viewport.yRange.midpoint)
        val current = snapshot(group, ChartSyncAxis.X)

        syncChartViewportsStep(group, previous, current, ChartSyncAxis.X)

        assertRangeClose(ClosedRangeD(2.0, 4.0), b.viewport.xRange)
    }

    @Test
    fun `x sync leaves the value axis independent`() {
        val a = stateFor(viewport(0.0, 10.0))
        val b = stateFor(viewport(0.0, 10.0, yStart = 100.0, yEnd = 200.0))
        val group = listOf(a, b)
        val previous = snapshot(group, ChartSyncAxis.X)

        a.zoomTo(xSpan = 4.0, ySpan = a.viewport.yRange.span, centerX = 5.0, centerY = a.viewport.yRange.midpoint)
        syncChartViewportsStep(group, previous, snapshot(group, ChartSyncAxis.X), ChartSyncAxis.X)

        assertRangeClose(ClosedRangeD(3.0, 7.0), b.viewport.xRange)
        // b keeps its own y window untouched.
        assertRangeClose(ClosedRangeD(100.0, 200.0), b.viewport.yRange)
    }

    @Test
    fun `both mode mirrors x and y`() {
        val a = stateFor(viewport(0.0, 10.0))
        val b = stateFor(viewport(0.0, 10.0))
        val group = listOf(a, b)
        val previous = snapshot(group, ChartSyncAxis.Both)

        a.zoomTo(xSpan = 2.0, ySpan = 4.0, centerX = 3.0, centerY = 5.0)
        syncChartViewportsStep(group, previous, snapshot(group, ChartSyncAxis.Both), ChartSyncAxis.Both)

        assertRangeClose(ClosedRangeD(2.0, 4.0), b.viewport.xRange)
        assertRangeClose(ClosedRangeD(3.0, 7.0), b.viewport.yRange)
    }

    @Test
    fun `no change is a no-op and converges`() {
        val a = stateFor(viewport(2.0, 6.0))
        val b = stateFor(viewport(2.0, 6.0))
        val group = listOf(a, b)
        val previous = snapshot(group, ChartSyncAxis.X)

        val next = syncChartViewportsStep(group, previous, snapshot(group, ChartSyncAxis.X), ChartSyncAxis.X)

        assertRangeClose(ClosedRangeD(2.0, 6.0), b.viewport.xRange)
        // The returned baseline equals the unchanged snapshot, so a follow-up step also does nothing.
        assertEquals(previous, next)
    }

    @Test
    fun `follower clamps the mirrored window to its own domain`() {
        val a = stateFor(viewport(0.0, 10.0))
        val b = stateFor(viewport(0.0, 5.0))
        val group = listOf(a, b)
        val previous = snapshot(group, ChartSyncAxis.X)

        a.zoomTo(xSpan = 2.0, ySpan = a.viewport.yRange.span, centerX = 7.0, centerY = a.viewport.yRange.midpoint)
        syncChartViewportsStep(group, previous, snapshot(group, ChartSyncAxis.X), ChartSyncAxis.X)

        // Center 7 is outside b's [0,5] domain, so it clamps to the right edge while preserving the span.
        assertTrue(b.viewport.xRange.endInclusive <= 5.0 + 1e-6)
        assertEquals(2.0, b.viewport.xRange.span, 1e-6)
    }
}
