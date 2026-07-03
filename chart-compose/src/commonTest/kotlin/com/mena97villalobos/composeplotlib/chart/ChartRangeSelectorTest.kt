package com.mena97villalobos.composeplotlib.chart

import kotlin.test.Test
import kotlin.test.assertEquals

class ChartRangeSelectorTest {
    // Window spans px 40..80; handle tolerance 10px.
    private fun zone(downX: Double) = chartRangeSelectorZone(downX, windowLeftPx = 40.0, windowRightPx = 80.0, handlePx = 10.0)

    @Test
    fun nearLeftEdgeIsLeftHandle() {
        assertEquals(ChartRangeSelectorZone.LeftHandle, zone(38.0))
    }

    @Test
    fun nearRightEdgeIsRightHandle() {
        assertEquals(ChartRangeSelectorZone.RightHandle, zone(82.0))
    }

    @Test
    fun insideWindowIsBody() {
        assertEquals(ChartRangeSelectorZone.Body, zone(60.0))
    }

    @Test
    fun outsideWindowIsOutside() {
        assertEquals(ChartRangeSelectorZone.Outside, zone(10.0))
        assertEquals(ChartRangeSelectorZone.Outside, zone(120.0))
    }

    @Test
    fun collapsedWindowFavorsNearestEdge() {
        // Left and right coincide; a hit resolves to the left handle deterministically.
        assertEquals(
            ChartRangeSelectorZone.LeftHandle,
            chartRangeSelectorZone(downX = 50.0, windowLeftPx = 50.0, windowRightPx = 50.0, handlePx = 10.0),
        )
    }
}
