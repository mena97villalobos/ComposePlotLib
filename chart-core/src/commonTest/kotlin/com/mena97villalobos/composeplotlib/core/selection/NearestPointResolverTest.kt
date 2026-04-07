package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NearestPointResolverTest {
    @Test
    fun resolvesNearestVisiblePointWithinDistanceThreshold() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                plotBounds = RectD(0.0, 0.0, 100.0, 100.0),
            )

        val selection =
            NearestPointResolver.resolve(
                seriesId = "series-1",
                points =
                    listOf(
                        XyPoint(2.0, 2.0),
                        XyPoint(8.0, 8.0),
                    ),
                touchPoint = OffsetD(79.0, 21.0),
                transform = transform,
                maxDistancePx = 8.0,
            )

        assertNotNull(selection)
        assertEquals("series-1", selection.target.seriesId)
        assertEquals(1, selection.target.pointIndex)
    }

    @Test
    fun rejectsNonFiniteTouchOrMaxDistance() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                plotBounds = RectD(0.0, 0.0, 100.0, 100.0),
            )
        val points = listOf(XyPoint(5.0, 5.0))
        assertNull(
            NearestPointResolver.resolve("s", points, OffsetD(Double.NaN, 50.0), transform, maxDistancePx = 20.0),
        )
        assertNull(
            NearestPointResolver.resolve("s", points, OffsetD(50.0, 50.0), transform, maxDistancePx = Double.NaN),
        )
        assertNull(
            NearestPointResolver.resolve("s", points, OffsetD(50.0, 50.0), transform, maxDistancePx = -1.0),
        )
    }

    @Test
    fun rejectsWhenClosestSampleMapsToNonFinitePixel() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                plotBounds = RectD(0.0, 0.0, 100.0, 100.0),
            )
        assertNull(
            NearestPointResolver.resolve(
                seriesId = "s",
                points = listOf(XyPoint(5.0, Double.NaN)),
                touchPoint = OffsetD(50.0, 50.0),
                transform = transform,
                maxDistancePx = 100.0,
            ),
        )
    }

    @Test
    fun tieBreaksByLowerPointIndexWhenDistancesEqual() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                plotBounds = RectD(0.0, 0.0, 100.0, 100.0),
            )
        val selection =
            NearestPointResolver.resolve(
                seriesId = "s",
                points =
                    listOf(
                        XyPoint(4.0, 5.0),
                        XyPoint(6.0, 5.0),
                    ),
                touchPoint = OffsetD(50.0, 50.0),
                transform = transform,
                maxDistancePx = 20.0,
            )
        assertNotNull(selection)
        assertEquals(0, selection.target.pointIndex)
    }

    @Test
    fun indexedResolveSkipsNonFiniteDomainSamples() {
        val transform =
            ChartTransform.forPlotBounds(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 10.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                plotBounds = RectD(0.0, 0.0, 100.0, 100.0),
            )
        val sel =
            NearestPointResolver.resolveIndexed(
                seriesId = "s",
                indexedPoints =
                    listOf(
                        0 to XyPoint(5.0, Double.NaN),
                        1 to XyPoint(5.0, 5.0),
                    ),
                touchPoint = OffsetD(50.0, 50.0),
                transform = transform,
                maxDistancePx = 10.0,
            )
        assertNotNull(sel)
        assertEquals(1, sel.target.pointIndex)
    }
}
