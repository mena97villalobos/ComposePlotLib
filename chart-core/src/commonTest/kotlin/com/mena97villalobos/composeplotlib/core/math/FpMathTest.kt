package com.mena97villalobos.composeplotlib.core.math

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FpMathTest {
    @Test
    fun isDegenerateSpan_nonFiniteOrTiny() {
        assertTrue(FpMath.isDegenerateSpan(0.0))
        assertTrue(FpMath.isDegenerateSpan(FpMath.EPS_SPAN / 2.0))
        assertTrue(FpMath.isDegenerateSpan(-FpMath.EPS_SPAN / 2.0))
        assertTrue(FpMath.isDegenerateSpan(Double.NaN))
        assertTrue(FpMath.isDegenerateSpan(Double.POSITIVE_INFINITY))
    }

    @Test
    fun isDegenerateSpan_nonDegenerate() {
        assertFalse(FpMath.isDegenerateSpan(1e-6))
        assertFalse(FpMath.isDegenerateSpan(FpMath.EPS_SPAN * 10.0))
    }

    @Test
    fun nearlyEqualRel_largeMagnitude() {
        val a = 1.0e18
        val b = a + 1.0
        assertTrue(FpMath.nearlyEqualRel(a, b))
    }

    @Test
    fun nearlyEqualRel_distinctWhenFarApart() {
        assertFalse(FpMath.nearlyEqualRel(0.0, 1.0))
        assertFalse(FpMath.nearlyEqualRel(100.0, 100.1))
    }
}
