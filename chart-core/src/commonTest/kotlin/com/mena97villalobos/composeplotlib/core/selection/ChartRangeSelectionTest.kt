package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartRangeSelectionTest {
    private val fullX = ClosedRangeD(0.0, 100.0)
    private val fullY = ClosedRangeD(-10.0, 10.0)

    @Test
    fun horizontalKeepsXAndSpansFullY() {
        val sel =
            chartRangeSelectionFromDomainCorners(
                x0 = 30.0,
                y0 = 5.0,
                x1 = 60.0,
                y1 = -2.0,
                axis = ChartBrushAxis.Horizontal,
                fullX = fullX,
                fullY = fullY,
            )
        assertEquals(ClosedRangeD(30.0, 60.0), sel.xRange)
        assertEquals(fullY, sel.yRange)
    }

    @Test
    fun verticalKeepsYAndSpansFullX() {
        val sel =
            chartRangeSelectionFromDomainCorners(
                x0 = 30.0,
                y0 = 5.0,
                x1 = 60.0,
                y1 = -2.0,
                axis = ChartBrushAxis.Vertical,
                fullX = fullX,
                fullY = fullY,
            )
        assertEquals(fullX, sel.xRange)
        assertEquals(ClosedRangeD(-2.0, 5.0), sel.yRange)
    }

    @Test
    fun bothKeepsBothDraggedSpans() {
        val sel =
            chartRangeSelectionFromDomainCorners(
                x0 = 60.0,
                y0 = -2.0,
                x1 = 30.0,
                y1 = 5.0,
                axis = ChartBrushAxis.Both,
                fullX = fullX,
                fullY = fullY,
            )
        // Corners arrive reversed; each axis is reordered to start <= end.
        assertEquals(ClosedRangeD(30.0, 60.0), sel.xRange)
        assertEquals(ClosedRangeD(-2.0, 5.0), sel.yRange)
    }
}
