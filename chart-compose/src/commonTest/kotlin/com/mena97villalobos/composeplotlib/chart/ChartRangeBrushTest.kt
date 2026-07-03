package com.mena97villalobos.composeplotlib.chart

import androidx.compose.ui.geometry.Offset
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartBrushAxis
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChartRangeBrushTest {
    // Plot is a 100x100 px square; x domain 0..10 (left→right), y domain 0..20 (bottom→top).
    private val plotBounds = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 100.0)
    private val viewport = ChartViewport(ClosedRangeD(0.0, 10.0), ClosedRangeD(0.0, 20.0))
    private val transform = ChartTransform.forPlotBounds(viewport = viewport, plotBounds = plotBounds)
    private val brush = ChartRangeBrush(axis = ChartBrushAxis.Horizontal, onBrush = {})

    private fun assertRange(
        expected: ClosedRangeD,
        actual: ClosedRangeD,
    ) {
        assertEquals(expected.start, actual.start, 1e-6)
        assertEquals(expected.endInclusive, actual.endInclusive, 1e-6)
    }

    @Test
    fun horizontalBrushMapsPixelsToDomainX() {
        val sel =
            resolveChartRangeBrush(
                startPx = Offset(20f, 10f),
                endPx = Offset(80f, 90f),
                plotBounds = plotBounds,
                transform = transform,
                viewport = viewport,
                brush = brush,
            )
        requireNotNull(sel)
        // x: 20px→2.0, 80px→8.0. Horizontal keeps x and spans the full y viewport.
        assertRange(ClosedRangeD(2.0, 8.0), sel.xRange)
        assertRange(ClosedRangeD(0.0, 20.0), sel.yRange)
    }

    @Test
    fun tinyDragBelowThresholdClears() {
        val sel =
            resolveChartRangeBrush(
                startPx = Offset(20f, 10f),
                endPx = Offset(22f, 11f),
                plotBounds = plotBounds,
                transform = transform,
                viewport = viewport,
                brush = brush,
            )
        assertNull(sel)
    }

    @Test
    fun dragBeyondPlotClampsToDomainExtent() {
        val sel =
            resolveChartRangeBrush(
                startPx = Offset(-30f, -30f),
                endPx = Offset(130f, 130f),
                plotBounds = plotBounds,
                transform = transform,
                viewport = viewport,
                brush = brush,
            )
        requireNotNull(sel)
        assertRange(ClosedRangeD(0.0, 10.0), sel.xRange)
    }

    @Test
    fun liveRectIsNormalizedAndClamped() {
        val rect = chartLiveBrushRect(Offset(80f, 90f), Offset(20f, 10f), plotBounds)
        assertEquals(20f, rect.left)
        assertEquals(10f, rect.top)
        assertEquals(80f, rect.right)
        assertEquals(90f, rect.bottom)
    }
}
