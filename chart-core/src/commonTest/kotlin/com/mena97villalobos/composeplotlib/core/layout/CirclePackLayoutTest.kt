package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CirclePackLayoutTest {
    private fun assertNoOverlaps(circles: List<PackedCircle>) {
        for (i in circles.indices) {
            for (j in i + 1 until circles.size) {
                val a = circles[i]
                val b = circles[j]
                if (a.radius == 0.0 || b.radius == 0.0) continue
                val distance = hypot(a.centerX - b.centerX, a.centerY - b.centerY)
                assertTrue(distance >= a.radius + b.radius - 1e-4, "circles $i and $j overlap")
            }
        }
    }

    @Test
    fun packedCirclesDoNotOverlap() {
        val radii = listOf(10.0, 8.0, 6.0, 6.0, 5.0, 4.0, 3.0, 3.0, 2.0)
        val packed = circlePackLayout(radii)
        assertEquals(radii.size, packed.size)
        radii.indices.forEach { assertEquals(radii[it], packed[it].radius, 1e-9) }
        assertNoOverlaps(packed)
    }

    @Test
    fun radiiEncodeAreaViaSqrt() {
        val radii = circlePackRadii(listOf(4.0, 16.0, 0.0, -3.0))
        assertEquals(2.0, radii[0], 1e-9)
        assertEquals(4.0, radii[1], 1e-9)
        assertEquals(0.0, radii[2], 1e-9)
        assertEquals(0.0, radii[3], 1e-9)
    }

    @Test
    fun fittingCentersAndScalesIntoBounds() {
        val packed = circlePackLayout(listOf(10.0, 8.0, 6.0, 5.0, 4.0))
        val bounds = RectD(0.0, 0.0, 200.0, 100.0)
        val fitted = fitPackedCircles(packed, bounds)
        assertNoOverlaps(fitted)
        fitted.forEach { c ->
            assertTrue(c.centerX - c.radius >= bounds.left - 1e-4)
            assertTrue(c.centerX + c.radius <= bounds.right + 1e-4)
            assertTrue(c.centerY - c.radius >= bounds.top - 1e-4)
            assertTrue(c.centerY + c.radius <= bounds.bottom + 1e-4)
        }
    }
}
