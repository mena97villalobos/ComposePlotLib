package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals

class ScatterPlotPointsFilterTest {
    @Test
    fun filterPreparedToPlotDisc_dropsPointsOutsideDiscIntersection() {
        val plotBounds = RectD(10.0, 20.0, 200.0, 180.0)
        val vp =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val t = ChartTransform.forPlotBounds(vp, plotBounds)
        val prepared =
            listOf(
                0 to XyPoint(0.0, 0.0),
                1 to XyPoint(11.0, 11.0),
            )
        val filtered = ScatterPlotPoints.filterPreparedToPlotDisc(prepared, t, plotBounds, radiusPx = 2.0)
        assertEquals(listOf(0 to XyPoint(0.0, 0.0)), filtered)
    }
}
