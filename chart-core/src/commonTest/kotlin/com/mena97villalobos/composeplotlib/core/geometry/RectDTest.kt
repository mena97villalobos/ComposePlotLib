package com.mena97villalobos.composeplotlib.core.geometry

import com.mena97villalobos.composeplotlib.core.math.FpMath
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RectDTest {
    @Test
    fun intersectsDiscDetectsOverlapWhenCenterOutside() {
        val r = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 50.0)
        val center = OffsetD(x = 102.0, y = 25.0)
        assertFalse(r.contains(center))
        assertTrue(r.intersectsDisc(center, radius = 4.0))
        assertFalse(r.intersectsDisc(center, radius = 1.0))
    }

    @Test
    fun intersectsDiscAtCorners() {
        val r = RectD(left = 0.0, top = 0.0, right = 10.0, bottom = 10.0)
        val cornerTouch = OffsetD(x = 12.0, y = 12.0)
        assertTrue(r.intersectsDisc(cornerTouch, radius = 3.0))
        assertFalse(r.intersectsDisc(cornerTouch, radius = 1.0))
    }

    @Test
    fun intersectsDiscZeroRadiusMatchesContains() {
        val r = RectD(left = 1.0, top = 1.0, right = 9.0, bottom = 9.0)
        val inside = OffsetD(5.0, 5.0)
        val outside = OffsetD(50.0, 5.0)
        assertTrue(r.intersectsDisc(inside, radius = 0.0))
        assertTrue(r.intersectsDisc(inside, radius = FpMath.EPS / 2.0))
        assertFalse(r.intersectsDisc(outside, radius = 0.0))
    }
}
