package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AxisValueTransformTest {
    @Test
    fun logForwardInverseRoundTrip() {
        val log = AxisValueTransform.Log(base = 10.0)
        assertEquals(0.0, log.forward(1.0), 1e-9)
        assertEquals(1.0, log.forward(10.0), 1e-9)
        assertEquals(2.0, log.forward(100.0), 1e-9)
        assertEquals(100.0, log.inverse(2.0), 1e-6)
        assertTrue(log.forward(0.0).isNaN(), "non-positive maps to NaN")
        assertTrue(log.forward(-5.0).isNaN())
    }

    @Test
    fun symLogIsContinuousAcrossZeroAndRoundTrips() {
        val s = AxisValueTransform.SymLog(base = 10.0, linearThreshold = 1.0)
        assertEquals(0.0, s.forward(0.0), 1e-9)
        // Odd symmetry.
        assertEquals(-s.forward(50.0), s.forward(-50.0), 1e-9)
        for (v in listOf(-1000.0, -3.0, -0.5, 0.0, 0.5, 3.0, 1000.0)) {
            assertEquals(v, s.inverse(s.forward(v)), 1e-6, "round-trip for $v")
        }
    }

    @Test
    fun identityIsUnchanged() {
        val id = AxisValueTransform.forScale(AxisScale.Linear)
        assertTrue(id.isIdentity)
        assertEquals(42.0, id.forward(42.0))
        assertEquals(42.0, id.inverse(42.0))
    }

    @Test
    fun logAxisMapsDecadesToEvenlySpacedPixels() {
        // Plot 300px tall (bottom=300, top=0); y domain 1..1000 on a log scale => 3 decades over 300px = 100px/decade.
        val plot = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 300.0)
        val viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(1.0, 1000.0))
        val t = ChartTransform.forPlotBounds(viewport, plot, yScale = AxisScale.Log())
        assertEquals(300.0, t.toPixel(XyPoint(0.5, 1.0)).y, 1e-6)
        assertEquals(200.0, t.toPixel(XyPoint(0.5, 10.0)).y, 1e-6)
        assertEquals(100.0, t.toPixel(XyPoint(0.5, 100.0)).y, 1e-6)
        assertEquals(0.0, t.toPixel(XyPoint(0.5, 1000.0)).y, 1e-6)
        // Inverse round-trips through the log scale.
        assertEquals(10.0, t.toDomain(t.toPixel(XyPoint(0.5, 10.0))).y, 1e-6)
    }

    @Test
    fun logAxisWithNonPositiveLowerBoundStaysFinite() {
        val plot = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 300.0)
        val viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1000.0))
        val t = ChartTransform.forPlotBounds(viewport, plot, yScale = AxisScale.Log())
        val top = t.toPixel(XyPoint(0.5, 1000.0)).y
        val mid = t.toPixel(XyPoint(0.5, 100.0)).y
        assertTrue(top.isFinite() && mid.isFinite())
        assertTrue(top < mid, "larger value renders higher (smaller y pixel)")
    }
}
