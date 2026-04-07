package com.mena97villalobos.composeplotlib.core.geometry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PolylineRectClipTest {
    private val rect = RectD(left = 10.0, top = 10.0, right = 90.0, bottom = 90.0)

    @Test
    fun horizontalSegmentTrimsToLeftAndRightEdges() {
        val out =
            PolylineRectClip.clipSegmentToRect(
                p0 = OffsetD(0.0, 50.0),
                p1 = OffsetD(100.0, 50.0),
                rect = rect,
            )
        assertNotNull(out)
        assertEquals(10.0, out.first.x)
        assertEquals(90.0, out.second.x)
        assertEquals(50.0, out.first.y)
        assertEquals(50.0, out.second.y)
    }

    @Test
    fun polylineFullyInsideIsUnchanged() {
        val pts =
            listOf(
                OffsetD(20.0, 20.0),
                OffsetD(50.0, 50.0),
                OffsetD(80.0, 40.0),
            )
        val fragments = PolylineRectClip.clipPolylineToRect(pts, rect)
        assertEquals(1, fragments.size)
        assertEquals(pts, fragments.single())
    }

    @Test
    fun polylineOutsideReturnsNoFragments() {
        val pts =
            listOf(
                OffsetD(0.0, 5.0),
                OffsetD(5.0, 5.0),
            )
        val fragments = PolylineRectClip.clipPolylineToRect(pts, rect)
        assertTrue(fragments.isEmpty())
    }

    @Test
    fun segmentFullyOutside_returnsNull() {
        assertNull(
            PolylineRectClip.clipSegmentToRect(
                p0 = OffsetD(0.0, 5.0),
                p1 = OffsetD(100.0, 5.0),
                rect = rect,
            ),
        )
    }
}
