package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VisibleRangeCalculatorTest {
    @Test
    fun visiblePointRange_emptyPoints_returnsNull() {
        assertNull(
            VisibleRangeCalculator.visiblePointRange(
                points = emptyList(),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 1.0),
                        yRange = ClosedRangeD(0.0, 1.0),
                    ),
            ),
        )
    }

    @Test
    fun visiblePointRange_singlePointInside_returnsSingletonRange() {
        val r =
            VisibleRangeCalculator.visiblePointRange(
                points = listOf(XyPoint(1.0, 2.0)),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 5.0),
                        yRange = ClosedRangeD(0.0, 5.0),
                    ),
            )
        assertNotNull(r)
        assertEquals(0, r.start)
        assertEquals(0, r.endInclusive)
    }

    @Test
    fun visiblePointRange_singlePointOutside_returnsNull() {
        assertNull(
            VisibleRangeCalculator.visiblePointRange(
                points = listOf(XyPoint(100.0, 100.0)),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 5.0),
                        yRange = ClosedRangeD(0.0, 5.0),
                    ),
            ),
        )
    }

    @Test
    fun visiblePointsCoverAllSamplesInSpanWhoseSegmentsIntersectViewportX() {
        val points =
            listOf(
                XyPoint(0.0, 0.0),
                XyPoint(1.0, 2.0),
                XyPoint(2.0, 4.0),
                XyPoint(3.0, 6.0),
            )

        val visible =
            VisibleRangeCalculator.visiblePoints(
                points = points,
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(1.0, 2.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
            )

        // Segments (0,1), (1,2), and (2,3) all intersect the viewport domain rectangle.
        assertEquals(points, visible)
    }

    @Test
    fun visibleRangeIncludesChordCrossingViewportWhenNoSampleXIsInside() {
        val points =
            listOf(
                XyPoint(0.0, 0.0),
                XyPoint(10.0, 10.0),
            )
        val range =
            VisibleRangeCalculator.visiblePointRange(
                points = points,
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(4.0, 6.0),
                        yRange = ClosedRangeD(0.0, 20.0),
                    ),
            )
        assertNotNull(range)
        assertEquals(0, range.start)
        assertEquals(1, range.endInclusive)
    }

    @Test
    fun visiblePointRangeWithContinuity_largePaddingClampsToSeriesBounds() {
        val points = (0..10).map { i -> XyPoint(i.toDouble(), i.toDouble()) }
        val range =
            VisibleRangeCalculator.visiblePointRangeWithContinuity(
                points = points,
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(4.0, 6.0),
                        yRange = ClosedRangeD(0.0, 20.0),
                    ),
                paddingEachSide = 100,
            )
        assertNotNull(range)
        assertEquals(0, range.start)
        assertEquals(10, range.endInclusive)
    }

    @Test
    fun continuityRangeExpandsIndicesOutsideViewportX() {
        val points =
            listOf(
                XyPoint(0.0, 0.0),
                XyPoint(1.0, 2.0),
                XyPoint(2.0, 4.0),
                XyPoint(3.0, 6.0),
            )
        val range =
            VisibleRangeCalculator.visiblePointRangeWithContinuity(
                points = points,
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(1.0, 2.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                paddingEachSide = 1,
            )
        assertNotNull(range)
        assertEquals(0, range.start)
        assertEquals(3, range.endInclusive)
    }

    @Test
    fun segmentExcludedWhenXLiesInViewportButYStrandIsOutside() {
        val points =
            listOf(
                XyPoint(0.0, 50.0),
                XyPoint(5.0, 50.0),
                XyPoint(10.0, 50.0),
            )
        val range =
            VisibleRangeCalculator.visiblePointRange(
                points = points,
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(4.0, 6.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
            )
        assertNull(range)
    }

    @Test
    fun barColumnHorizontalSpanUsesFullPixelWidth() {
        val plot =
            RectD(
                left = 0.0,
                top = 0.0,
                right = 100.0,
                bottom = 100.0,
            )
        assertTrue(
            VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(
                centerXPx = 105.0,
                barWidthPx = 20.0,
                plotBounds = plot,
            ),
        )
        assertTrue(
            !VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(
                centerXPx = 115.0,
                barWidthPx = 20.0,
                plotBounds = plot,
            ),
        )
    }

    @Test
    fun barColumnDefaultSlackKeepsColumnsJustOutsideHardEdge() {
        val plot =
            RectD(
                left = 0.0,
                top = 0.0,
                right = 100.0,
                bottom = 100.0,
            )
        assertTrue(
            !VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(
                centerXPx = 110.25,
                barWidthPx = 20.0,
                plotBounds = plot,
                plotEdgeSlackPx = 0.0,
            ),
        )
        assertTrue(
            VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(
                centerXPx = 110.25,
                barWidthPx = 20.0,
                plotBounds = plot,
            ),
        )
    }
}
