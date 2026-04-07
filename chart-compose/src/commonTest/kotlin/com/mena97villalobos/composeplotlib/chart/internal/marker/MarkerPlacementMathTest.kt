package com.mena97villalobos.composeplotlib.chart.internal.marker

import com.mena97villalobos.composeplotlib.chart.ChartMarkerCustomCallout
import com.mena97villalobos.composeplotlib.chart.nudgeAxisAlignedRectToAvoidOverlap
import com.mena97villalobos.composeplotlib.chart.stackAxisAlignedRectBelow
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MarkerPlacementMathTest {
    @Test
    fun preferAbove_whenTopFits_usesAbovePlacement() {
        val bounds = RectD(left = 0.0, top = 0.0, right = 200.0, bottom = 200.0)
        val (x, y) =
            computeContainedMarkerTopLeft(
                anchorX = 100.0,
                anchorY = 100.0,
                markerWidth = 80,
                markerHeight = 40,
                gapPx = 8,
                bounds = bounds,
                preferAbove = true,
            )
        assertEquals(60, x)
        assertEquals(52, y)
    }

    @Test
    fun flipsBelow_whenNotEnoughRoomAbove() {
        val bounds = RectD(left = 0.0, top = 0.0, right = 200.0, bottom = 200.0)
        val (x, y) =
            computeContainedMarkerTopLeft(
                anchorX = 100.0,
                anchorY = 20.0,
                markerWidth = 80,
                markerHeight = 40,
                gapPx = 8,
                bounds = bounds,
                preferAbove = true,
            )
        assertEquals(60, x)
        assertEquals(28, y)
    }

    @Test
    fun slidesHorizontally_whenCenteredWouldOverflowRight() {
        val bounds = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 200.0)
        val markerW = 60
        val anchorX = 95.0
        val (x, y) =
            computeContainedMarkerTopLeft(
                anchorX = anchorX,
                anchorY = 100.0,
                markerWidth = markerW,
                markerHeight = 40,
                gapPx = 8,
                bounds = bounds,
                preferAbove = true,
            )
        assertEquals((bounds.right - markerW).toInt(), x)
        assertEquals(52, y)
    }

    @Test
    fun legacyClampsToOrigin() {
        val (x, y) =
            computeMarkerTopLeftLegacy(
                anchorX = 5.0,
                anchorY = 5.0,
                markerWidth = 40,
                markerHeight = 30,
                gapPx = 12,
            )
        assertEquals(0, x)
        assertEquals(0, y)
    }

    @Test
    fun calloutElbow_whenAnchorBelowBox_attachesToBottomEdge() {
        val poly =
            computeCalloutElbowPolyline(
                anchorX = 100.0,
                anchorY = 120.0,
                markerOffsetX = 50,
                markerOffsetY = 20,
                markerWidth = 100,
                markerHeight = 40,
            )
        assertEquals(3, poly.size)
        assertEquals(100.0, poly[0].first)
        assertEquals(120.0, poly[0].second)
        assertEquals(100.0, poly[1].first)
        assertEquals(60.0, poly[1].second)
        assertEquals(100.0, poly[2].first)
        assertEquals(60.0, poly[2].second)
    }

    @Test
    fun calloutElbow_whenAnchorAboveBox_attachesToTopEdge() {
        val poly =
            computeCalloutElbowPolyline(
                anchorX = 80.0,
                anchorY = 20.0,
                markerOffsetX = 30,
                markerOffsetY = 48,
                markerWidth = 80,
                markerHeight = 36,
            )
        assertEquals(3, poly.size)
        assertEquals(48.0, poly[1].second)
        assertEquals(48.0, poly[2].second)
        assertEquals(70.0, poly[2].first)
        assertEquals(80.0, poly[1].first)
    }

    @Test
    fun calloutElbow_emptyWhenMarkerNotLaidOut() {
        assertEquals(
            emptyList<Pair<Double, Double>>(),
            computeCalloutElbowPolyline(
                anchorX = 1.0,
                anchorY = 2.0,
                markerOffsetX = 0,
                markerOffsetY = 0,
                markerWidth = 0,
                markerHeight = 10,
            ),
        )
    }

    @Test
    fun customCalloutStraightSegment() {
        val path =
            ChartMarkerCustomCallout { ax, ay, left, top, w, h ->
                listOf(ax to ay, left + w to top + h)
            }
        val poly = path.polyline(0.0, 0.0, 10.0, 10.0, 20.0, 20.0)
        assertEquals(2, poly.size)
        assertEquals(0.0, poly[0].first)
        assertEquals(0.0, poly[0].second)
        assertEquals(30.0, poly[1].first)
        assertEquals(30.0, poly[1].second)
    }

    @Test
    fun nudgeAxisAlignedRectSeparatesFromObstacle() {
        val obstacle = RectD(0.0, 0.0, 50.0, 50.0)
        val moving = RectD(10.0, 10.0, 60.0, 60.0)
        val out = nudgeAxisAlignedRectToAvoidOverlap(moving, obstacle, gap = 0.0)
        assertFalse(out.intersects(obstacle))
    }

    @Test
    fun stackAxisAlignedRectBelow_centersAndRespectsGap() {
        val upper = RectD(10.0, 10.0, 110.0, 50.0)
        val lower = stackAxisAlignedRectBelow(upper, width = 40.0, height = 20.0, gap = 8.0)
        assertEquals(58.0, lower.top, 1e-9)
        assertEquals(40.0, lower.left, 1e-9)
        assertEquals(60.0, lower.left + lower.width / 2.0, 1e-9)
    }
}
