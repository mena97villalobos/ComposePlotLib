package com.mena97villalobos.composeplotlib.core.viewport

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewportStateTest {
    private val domainSquare =
        ChartViewport(
            xRange = ClosedRangeD(0.0, 100.0),
            yRange = ClosedRangeD(0.0, 100.0),
        )

    @Test
    fun zoomHalvesSpanAroundPivot() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 100.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
            )

        val zoomed = state.zoom(scaleX = 2.0, scaleY = 2.0, pivotX = 50.0, pivotY = 50.0, domain = domainSquare)

        assertEquals(25.0, zoomed.viewport.xRange.start)
        assertEquals(75.0, zoomed.viewport.xRange.endInclusive)
        assertEquals(25.0, zoomed.viewport.yRange.start)
        assertEquals(75.0, zoomed.viewport.yRange.endInclusive)
    }

    @Test
    fun zoomEnforcesMinSpanAroundPivot() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 100.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
                constraints = ViewportConstraints(minXSpan = 20.0, minYSpan = 25.0),
            )

        val zoomed = state.zoom(scaleX = 100.0, scaleY = 100.0, pivotX = 50.0, pivotY = 60.0, domain = domainSquare)

        assertEquals(20.0, zoomed.viewport.xRange.span)
        assertEquals(40.0, zoomed.viewport.xRange.start)
        assertEquals(60.0, zoomed.viewport.xRange.endInclusive)

        assertEquals(25.0, zoomed.viewport.yRange.span)
        assertEquals(47.5, zoomed.viewport.yRange.start)
        assertEquals(72.5, zoomed.viewport.yRange.endInclusive)
    }

    @Test
    fun panIgnoresNonFiniteDeltas() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(10.0, 40.0),
                        yRange = ClosedRangeD(10.0, 40.0),
                    ),
            )
        val panned = state.pan(deltaX = Double.NaN, deltaY = Double.POSITIVE_INFINITY, domain = domainSquare)
        assertEquals(ClosedRangeD(10.0, 40.0), panned.viewport.xRange)
        assertEquals(ClosedRangeD(10.0, 40.0), panned.viewport.yRange)
    }

    @Test
    fun panClampsWhenTranslatedPastDomain() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(10.0, 40.0),
                        yRange = ClosedRangeD(10.0, 40.0),
                    ),
            )

        val panned = state.pan(deltaX = 500.0, deltaY = 0.0, domain = domainSquare)

        assertEquals(ClosedRangeD(70.0, 100.0), panned.viewport.xRange)
        assertEquals(ClosedRangeD(10.0, 40.0), panned.viewport.yRange)
    }

    @Test
    fun withViewportClampsViewportToDomainBounds() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 20.0),
            )
        val state = ViewportState(viewport = domain)

        val next =
            state.withViewport(
                nextViewport =
                    ChartViewport(
                        xRange = ClosedRangeD(8.0, 12.0),
                        yRange = ClosedRangeD(-5.0, 5.0),
                    ),
                domain = domain,
            )

        assertEquals(ClosedRangeD(6.0, 10.0), next.viewport.xRange)
        assertEquals(ClosedRangeD(0.0, 10.0), next.viewport.yRange)
    }

    @Test
    fun degenerateDomainXProducesStableVisibleWindow() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(5.0, 5.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val state = ViewportState(viewport = domain)
        val panned = state.pan(deltaX = 100.0, deltaY = 0.0, domain = domain)
        assertEquals(ClosedRangeD(5.0, 5.0), panned.viewport.xRange)
        assertEquals(ClosedRangeD(0.0, 10.0), panned.viewport.yRange)
    }

    @Test
    fun degenerateDomainYProducesStableVisibleWindow() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(4.0, 4.0),
            )
        val state = ViewportState(viewport = domain)
        val panned = state.pan(deltaX = 0.0, deltaY = 100.0, domain = domain)
        assertEquals(ClosedRangeD(0.0, 10.0), panned.viewport.xRange)
        assertEquals(ClosedRangeD(4.0, 4.0), panned.viewport.yRange)
    }

    @Test
    fun nonFiniteZoomScaleIsSanitized() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 100.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
            )
        val z =
            state.zoom(
                scaleX = Double.NaN,
                scaleY = 2.0,
                pivotX = 50.0,
                pivotY = 50.0,
                domain = domainSquare,
            )
        assertEquals(0.0, z.viewport.xRange.start)
        assertEquals(100.0, z.viewport.xRange.endInclusive)
        assertEquals(25.0, z.viewport.yRange.start)
        assertEquals(75.0, z.viewport.yRange.endInclusive)
    }

    @Test
    fun expandedIfDegenerateMakesDrawableRange() {
        val r = ClosedRangeD(3.0, 3.0).expandedIfDegenerate(pad = 2.0)
        assertTrue(r.span >= 1.999)
        assertEquals(3.0, r.midpoint, 1e-9)
    }

    @Test
    fun minSpanLargerThanDomainCapsAtDomainSpan() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 100.0),
            )
        val state =
            ViewportState(
                viewport = domain,
                constraints = ViewportConstraints(minXSpan = 80.0),
            )
        val after = state.withViewport(nextViewport = domain, domain = domain)
        assertEquals(10.0, after.viewport.xRange.span)
        assertEquals(ClosedRangeD(0.0, 10.0), after.viewport.xRange)
    }

    @Test
    fun maxSpanLargerThanDomainZoomsOutToFullDomainWidth() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 50.0),
                yRange = ClosedRangeD(0.0, 100.0),
            )
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(10.0, 40.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
                constraints = ViewportConstraints(maxXSpan = 500.0),
            )
        val out = state.zoom(scaleX = 0.5, scaleY = 1.0, pivotX = 25.0, pivotY = 50.0, domain = domain)
        assertEquals(50.0, out.viewport.xRange.span)
        assertEquals(ClosedRangeD(0.0, 50.0), out.viewport.xRange)
    }

    @Test
    fun zoomWithPivotAtDomainCorner_respectsMinSpanAndStaysInsideDomain() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 100.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
                constraints = ViewportConstraints(minXSpan = 25.0, minYSpan = 25.0),
            )
        val z = state.zoom(scaleX = 4.0, scaleY = 4.0, pivotX = 0.0, pivotY = 0.0, domain = domainSquare)
        assertEquals(25.0, z.viewport.xRange.span, 1e-9)
        assertEquals(25.0, z.viewport.yRange.span, 1e-9)
        assertTrue(z.viewport.xRange.start >= 0.0)
        assertTrue(z.viewport.yRange.start >= 0.0)
        assertTrue(z.viewport.xRange.endInclusive <= 100.0)
        assertTrue(z.viewport.yRange.endInclusive <= 100.0)
    }

    @Test
    fun extremeSanitizedZoomScalesProduceFiniteOrderedRanges() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(20.0, 80.0),
                        yRange = ClosedRangeD(20.0, 80.0),
                    ),
                constraints = ViewportConstraints(minXSpan = 5.0, minYSpan = 5.0),
            )
        val z =
            state.zoom(
                scaleX = FpMath.MAX_ZOOM_SCALE,
                scaleY = FpMath.MIN_ZOOM_SCALE,
                pivotX = 50.0,
                pivotY = 50.0,
                domain = domainSquare,
            )
        assertTrue(z.viewport.xRange.start.isFinite() && z.viewport.xRange.endInclusive.isFinite())
        assertTrue(z.viewport.yRange.start.isFinite() && z.viewport.yRange.endInclusive.isFinite())
        assertTrue(z.viewport.xRange.start <= z.viewport.xRange.endInclusive)
        assertTrue(z.viewport.yRange.start <= z.viewport.yRange.endInclusive)
    }

    @Test
    fun maxSpanBelowMinSpan_isCoercedToMinSpan() {
        val state =
            ViewportState(
                viewport = domainSquare,
                constraints = ViewportConstraints(minXSpan = 40.0, maxXSpan = 10.0),
            )
        val z = state.zoom(scaleX = 2.0, scaleY = 1.0, pivotX = 50.0, pivotY = 50.0, domain = domainSquare)
        assertEquals(40.0, z.viewport.xRange.span, 1e-9)
        assertEquals(30.0, z.viewport.xRange.start, 1e-9)
        assertEquals(70.0, z.viewport.xRange.endInclusive, 1e-9)
    }

    @Test
    fun canonicalDomain_replacesNonFiniteBoundsWithUnitAxis() {
        val bad =
            ChartViewport(
                xRange = ClosedRangeD(Double.NEGATIVE_INFINITY, 0.0),
                yRange = ClosedRangeD(0.0, Double.POSITIVE_INFINITY),
            )
        val state = ViewportState(viewport = bad)
        val panned = state.pan(0.0, 0.0, domain = bad)
        assertEquals(ClosedRangeD(0.0, 1.0), panned.viewport.xRange)
        assertEquals(ClosedRangeD(0.0, 1.0), panned.viewport.yRange)
    }

    @Test
    fun withViewport_replacesBadAxisWithCanonicalDomain() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 20.0),
            )
        val state = ViewportState(viewport = domain)
        val badNext =
            ChartViewport(
                xRange = ClosedRangeD(Double.NEGATIVE_INFINITY, 0.0),
                yRange = ClosedRangeD(5.0, 15.0),
            )
        val out = state.withViewport(badNext, domain = domain)
        assertEquals(ClosedRangeD(0.0, 10.0), out.viewport.xRange)
        assertEquals(ClosedRangeD(5.0, 15.0), out.viewport.yRange)
    }

    @Test
    fun degenerateDomainBothAxes_staysStableUnderZoom() {
        val pointDomain =
            ChartViewport(
                xRange = ClosedRangeD(3.0, 3.0),
                yRange = ClosedRangeD(-1.0, -1.0),
            )
        val state = ViewportState(viewport = pointDomain)
        val z = state.zoom(scaleX = 10.0, scaleY = 10.0, pivotX = 3.0, pivotY = -1.0, domain = pointDomain)
        assertEquals(ClosedRangeD(3.0, 3.0), z.viewport.xRange)
        assertEquals(ClosedRangeD(-1.0, -1.0), z.viewport.yRange)
    }

    @Test
    fun corruptedViewport_repairedOnPan() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 100.0),
                yRange = ClosedRangeD(0.0, 100.0),
            )
        val broken =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                        yRange = ClosedRangeD(10.0, 40.0),
                    ),
            )
        val fixed = broken.pan(0.0, 0.0, domain = domain)
        assertEquals(ClosedRangeD(0.0, 100.0), fixed.viewport.xRange)
        assertEquals(ClosedRangeD(10.0, 40.0), fixed.viewport.yRange)
    }

    @Test
    fun extremePinchZoom_respectsMaxSpanAgainstDomain() {
        val domain =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 100.0),
            )
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(1.0, 9.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
                constraints = ViewportConstraints(maxXSpan = 6.0),
            )
        val z = state.zoom(scaleX = FpMath.MIN_ZOOM_SCALE, scaleY = 1.0, pivotX = 5.0, pivotY = 50.0, domain = domain)
        assertEquals(6.0, z.viewport.xRange.span, 1e-6)
        assertTrue(z.viewport.xRange.start >= 0.0)
        assertTrue(z.viewport.xRange.endInclusive <= 10.0)
    }

    @Test
    fun nonFinitePivotFallsBackToSanitizedMidpointUnderZoom() {
        val state =
            ViewportState(
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(40.0, 60.0),
                        yRange = ClosedRangeD(0.0, 100.0),
                    ),
            )
        val z = state.zoom(scaleX = 2.0, scaleY = 1.0, pivotX = Double.NaN, pivotY = 50.0, domain = domainSquare)
        assertEquals(45.0, z.viewport.xRange.start, 1e-9)
        assertEquals(55.0, z.viewport.xRange.endInclusive, 1e-9)
    }
}
