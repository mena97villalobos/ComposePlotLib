package com.mena97villalobos.composeplotlib.core.range

import com.mena97villalobos.composeplotlib.core.math.FpMath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClosedRangeDTest {
    @Test
    fun intersectionClampsToOverlap() {
        val a = ClosedRangeD(0.0, 10.0)
        val b = ClosedRangeD(5.0, 15.0)
        assertEquals(ClosedRangeD(5.0, 10.0), a.intersection(b))
    }

    @Test
    fun intersectionEmptyWhenDisjoint() {
        val a = ClosedRangeD(0.0, 2.0)
        val b = ClosedRangeD(5.0, 6.0)
        assertNull(a.intersection(b))
    }

    @Test
    fun intersectionNearBoundaryWithinSpanEpsilon_isValidAndThin() {
        val a = ClosedRangeD(0.0, 1.0)
        val b = ClosedRangeD(1.0 + FpMath.EPS_SPAN * 0.5, 3.0)
        val got = requireNotNull(a.intersection(b))
        assertTrue(got.start <= got.endInclusive)
        assertTrue(got.span <= FpMath.EPS_SPAN * 2)
    }

    @Test
    fun clampCoercesToInclusiveEndpoints() {
        assertEquals(2.0, ClosedRangeD(2.0, 5.0).clamp(0.0))
        assertEquals(4.0, ClosedRangeD(2.0, 4.0).clamp(99.0))
    }

    @Test
    fun expandedIfDegenerate_respectsMinimumHalfSpan() {
        val r = ClosedRangeD(7.0, 7.0).expandedIfDegenerate(pad = 0.0)
        assertTrue(r.span >= FpMath.EPS_SPAN)
        assertEquals(7.0, r.midpoint, 1e-9)
    }
}
