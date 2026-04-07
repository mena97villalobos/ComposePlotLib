package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScatterRenderPipelineTest {
    @Test
    fun prepareIndexed_filtersToDomainViewportOnly() {
        val pts =
            listOf(
                XyPoint(1.0, 1.0),
                XyPoint(50.0, 2.0),
                XyPoint(5.0, 5.0),
            )
        val vp =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val out = ScatterRenderPipeline.prepareIndexed(pts, vp, ScatterRenderSpec())
        assertEquals(listOf(0 to pts[0], 2 to pts[2]), out)
    }

    @Test
    fun prepareIndexed_uniformCap_neverExceedsMax() {
        val pts =
            (0 until 5000).map { i ->
                XyPoint(x = (i % 200) * 0.05, y = (i / 200) * 0.05)
            }
        val vp =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val spec = ScatterRenderSpec(maxPointsPerSeries = 120, decimation = LineDecimationStrategy.Uniform)
        val out = ScatterRenderPipeline.prepareIndexed(pts, vp, spec)
        assertTrue(out.isNotEmpty())
        assertTrue(out.size <= 120)
        assertTrue(out.all { (idx, p) -> idx in pts.indices && p == pts[idx] })
    }

    @Test
    fun prepareIndexed_largeSeries_completesAndPreservesIndices() {
        val n = 80_000
        val pts = List(n) { i -> XyPoint(x = (i % 1000) * 0.01, y = sin(i * 0.001)) }
        val vp =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(-2.0, 2.0),
            )
        val spec = ScatterRenderSpec(maxPointsPerSeries = 1024, decimation = LineDecimationStrategy.MinMaxY)
        val out = ScatterRenderPipeline.prepareIndexed(pts, vp, spec)
        assertTrue(out.size in 1..1024)
        assertTrue(out.all { (idx, p) -> idx in pts.indices && p == pts[idx] })
    }
}
