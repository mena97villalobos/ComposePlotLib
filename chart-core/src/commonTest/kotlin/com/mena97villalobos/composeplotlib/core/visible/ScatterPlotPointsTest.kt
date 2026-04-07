package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScatterPlotPointsTest {
    @Test
    fun radiusIncludesSamplesNearPlotEdge() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val plot = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 100.0)
        val t = ChartTransform.forPlotBounds(viewport, plot)
        val pts = listOf(ChartPoint(10.2, 5.0))
        assertEquals(
            0,
            ScatterPlotPoints.indexedInPlotBounds(pts, t, plot, radiusPx = 0.0).size,
        )
        val withRadius = ScatterPlotPoints.indexedInPlotBounds(pts, t, plot, radiusPx = 4.0)
        assertEquals(1, withRadius.size)
        assertEquals(0, withRadius.first().first)
        val pixel = t.toPixel(withRadius.first().second)
        assertTrue(!plot.contains(pixel))
        assertTrue(plot.intersectsDisc(pixel, 4.0))
    }
}
