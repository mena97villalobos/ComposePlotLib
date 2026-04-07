package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartTransformTest {
    @Test
    fun mapsDomainPointToPixelCoordinates() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
                plotBounds = RectD(10.0, 20.0, 110.0, 220.0),
            )

        val pixel = transform.toPixel(XyPoint(5.0, 50.0))

        assertEquals(60.0, pixel.x)
        assertEquals(120.0, pixel.y)
    }

    @Test
    fun degenerateDomainSpan_mapsToPixelStartAndStableDomainRoundTrip() {
        val x = ClosedRangeD(5.0, 5.0)
        val y = ClosedRangeD(3.0, 3.0 + FpMath.EPS_SPAN / 4.0)
        val t =
            ChartTransform.forPlotBounds(
                viewport = ChartViewport(xRange = x, yRange = y),
                plotBounds = RectD(0.0, 0.0, 100.0, 200.0),
            )
        val p = t.toPixel(XyPoint(99.0, 99.0))
        assertEquals(0.0, p.x, absoluteTolerance = 1e-9)
        // Y maps domain → pixels with bottom→pixelStart, top→pixelEnd (ChartTransform.forPlotBounds).
        assertEquals(200.0, p.y, absoluteTolerance = 1e-9)
        val back = t.toDomain(p)
        assertEquals(5.0, back.x, absoluteTolerance = 1e-9)
        assertTrue(back.y.isFinite())
    }

    @Test
    fun degeneratePlotWidth_mapsDomainXToStart() {
        val t =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 1.0),
                    ),
                plotBounds = RectD(50.0, 0.0, 50.0, 100.0),
            )
        val p = t.toPixel(XyPoint(7.0, 0.5))
        assertEquals(50.0, p.x, absoluteTolerance = 1e-9)
        assertTrue(p.y.isFinite())
    }
}
