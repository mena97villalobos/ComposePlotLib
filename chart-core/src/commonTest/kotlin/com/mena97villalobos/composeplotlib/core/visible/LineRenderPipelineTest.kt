package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineRenderPipelineTest {
    @Test
    fun prepareIndexed_emptySeries_returnsEmpty() {
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 0,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        assertEquals(
            emptyList(),
            LineRenderPipeline.prepareIndexed(emptyList(), viewport, spec),
        )
    }

    @Test
    fun prepareIndexed_singlePointInsideViewport_returnsThatPoint() {
        val p = XyPoint(5.0, 3.0)
        val pts = listOf(p)
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 0,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        assertEquals(listOf(0 to p), LineRenderPipeline.prepareIndexed(pts, viewport, spec))
    }

    @Test
    fun prepareIndexed_noVisibleSegment_returnsEmpty() {
        val pts = listOf(XyPoint(0.0, 0.0), XyPoint(1.0, 0.0))
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 0,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(10.0, 20.0),
            )
        assertEquals(emptyList(), LineRenderPipeline.prepareIndexed(pts, viewport, spec))
    }

    @Test
    fun minMaxYDecimation_verticalSegment_degenerateDomainXSpan_staysFinite() {
        val pts =
            listOf(
                XyPoint(5.0, 0.0),
                XyPoint(5.0, 5.0),
                XyPoint(5.0, 10.0),
            )
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 8,
                decimation = LineDecimationStrategy.MinMaxY,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(4.0, 6.0),
                yRange = ClosedRangeD(2.0, 8.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.isNotEmpty())
        assertTrue(indexed.all { it.second.x.isFinite() && it.second.y.isFinite() })
    }

    @Test
    fun uniformDecimationHonorsCapAndKeepsGlobalIndices() {
        val pts = (0..99).map { i -> XyPoint(i.toDouble(), (i * 2).toDouble()) }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 12,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 99.0),
                yRange = ClosedRangeD(0.0, 250.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 12)
        assertEquals(0, indexed.first().first)
        assertEquals(99, indexed.last().first)
        assertTrue(indexed.map { it.first }.windowed(2).all { it[0] < it[1] })
    }

    @Test
    fun prepareIndexedKeepsChordCrossingViewportWhenNoSampleXLiesInside() {
        val pts = listOf(XyPoint(0.0, 0.0), XyPoint(10.0, 10.0))
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 0,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(4.0, 6.0),
                yRange = ClosedRangeD(0.0, 20.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertEquals(listOf(0 to pts[0], 1 to pts[1]), indexed)
    }

    @Test
    fun minMaxDecimationProducesFewerPointsThanRawWindow() {
        val pts =
            (0..199).map { i ->
                val spike = if (i == 150) 1000.0 else 0.0
                XyPoint(i.toDouble(), spike + i * 0.01)
            }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 40,
                decimation = LineDecimationStrategy.MinMaxY,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 199.0),
                yRange = ClosedRangeD(0.0, 1200.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 40)
        assertTrue(indexed.any { it.first == 150 })
    }

    @Test
    fun prepareIndexedSamplesLargeVisibleWindowWithoutMaterializingFullSublistWhenCapped() {
        val pts = (0..50_000).map { i -> XyPoint(i.toDouble(), (i % 7).toDouble()) }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 24,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 50_000.0),
                yRange = ClosedRangeD(-1.0, 8.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 24)
        assertEquals(0, indexed.first().first)
        assertEquals(50_000, indexed.last().first)
        assertTrue(indexed.map { it.first }.windowed(2).all { it[0] < it[1] })
    }

    @Test
    fun decimateIndexedMinMaxRetainsSpikeFromMaterializedWindow() {
        val window =
            (0..199).map { i ->
                val spike = if (i == 120) 500.0 else 0.0
                i to XyPoint(i.toDouble(), spike + i * 0.01)
            }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 32,
                decimation = LineDecimationStrategy.MinMaxY,
                viewportContinuityPadding = 0,
            )
        val out = LineRenderPipeline.decimateIndexed(window, spec)
        assertTrue(out.size <= 32)
        assertTrue(out.any { it.first == 120 })
    }

    @Test
    fun viewportContinuityPaddingExpandsIndexSpanBeyondVisibleSamples() {
        val pts = (0..20).map { i -> XyPoint(i.toDouble(), 0.0) }
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(5.0, 7.0),
                yRange = ClosedRangeD(-1.0, 1.0),
            )
        val none =
            LineRenderSpec(
                maxPointsPerSeries = 0,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val padded = none.copy(viewportContinuityPadding = 2)
        val i0 = LineRenderPipeline.prepareIndexed(pts, viewport, none)
        val i2 = LineRenderPipeline.prepareIndexed(pts, viewport, padded)
        assertTrue(i2.first().first < i0.first().first)
        assertTrue(i2.last().first > i0.last().first)
    }

    @Test
    fun lttbHonorsCapKeepsEndpointsAndGlobalIndexOrder() {
        val pts = (0..99).map { i -> XyPoint(i.toDouble(), (i * 2).toDouble()) }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 12,
                decimation = LineDecimationStrategy.Lttb,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 99.0),
                yRange = ClosedRangeD(0.0, 250.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 12)
        assertEquals(0, indexed.first().first)
        assertEquals(99, indexed.last().first)
        assertTrue(indexed.map { it.first }.windowed(2).all { it[0] < it[1] })
    }

    @Test
    fun lttbPreservesProminentSpike() {
        val pts =
            (0..199).map { i ->
                val spike = if (i == 150) 1000.0 else 0.0
                XyPoint(i.toDouble(), spike + i * 0.01)
            }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 40,
                decimation = LineDecimationStrategy.Lttb,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 199.0),
                yRange = ClosedRangeD(0.0, 1200.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 40)
        assertTrue(indexed.any { it.first == 150 }) // largest-triangle keeps the spike vertex
    }

    @Test
    fun lttbViaMaterializedWindowRetainsSpikeAndEndpoints() {
        val window =
            (0..199).map { i ->
                val spike = if (i == 120) 500.0 else 0.0
                i to XyPoint(i.toDouble(), spike + i * 0.01)
            }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 32,
                decimation = LineDecimationStrategy.Lttb,
                viewportContinuityPadding = 0,
            )
        val out = LineRenderPipeline.decimateIndexed(window, spec)
        assertTrue(out.size <= 32)
        assertEquals(0, out.first().first)
        assertEquals(199, out.last().first)
        assertTrue(out.any { it.first == 120 })
    }

    @Test
    fun lttbBoundedForVeryLargeSeries() {
        val n = 200_000
        val pts = (0..n).map { i -> XyPoint(i.toDouble(), (i % 7).toDouble()) }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 512,
                decimation = LineDecimationStrategy.Lttb,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, n.toDouble()),
                yRange = ClosedRangeD(-1.0, 8.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 512)
        assertEquals(0, indexed.first().first)
        assertEquals(n, indexed.last().first)
    }

    @Test
    fun prepareIndexedBoundedForVeryLargeSeries() {
        val n = 300_000
        val pts = (0..n).map { i -> XyPoint(i.toDouble(), (i % 7).toDouble()) }
        val spec =
            LineRenderSpec(
                maxPointsPerSeries = 512,
                decimation = LineDecimationStrategy.Uniform,
                viewportContinuityPadding = 0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, n.toDouble()),
                yRange = ClosedRangeD(-1.0, 8.0),
            )
        val indexed = LineRenderPipeline.prepareIndexed(pts, viewport, spec)
        assertTrue(indexed.size <= 512)
        assertEquals(0, indexed.first().first)
        assertEquals(n, indexed.last().first)
    }
}
