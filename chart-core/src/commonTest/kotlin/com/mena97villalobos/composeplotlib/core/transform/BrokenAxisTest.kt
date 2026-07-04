package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.layout.planning.planAxisValues
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisBreak
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokenAxisTest {
    private val transform =
        AxisValueTransform.Broken(breaks = listOf(AxisBreak(10.0, 1000.0)), compressionFactor = 0.05)

    @Test
    fun identityOutsideBreaksAndCompressedInside() {
        assertEquals(5.0, transform.forward(5.0), 1e-9) // before the break
        assertEquals(10.0, transform.forward(10.0), 1e-9) // break start
        // 1000 collapses to 10 + (1000-10)*0.05 = 59.5.
        assertEquals(59.5, transform.forward(1000.0), 1e-9)
        // After the break the slope returns to 1: 1010 -> 69.5.
        assertEquals(69.5, transform.forward(1010.0), 1e-9)
    }

    @Test
    fun forwardThenInverseRoundTrips() {
        listOf(0.0, 5.0, 10.0, 500.0, 1000.0, 1005.0, 2000.0).forEach { v ->
            assertEquals(v, transform.inverse(transform.forward(v)), 1e-6)
        }
    }

    @Test
    fun forScaleBuildsBrokenTransform() {
        val vt = AxisValueTransform.forScale(AxisScale.Broken(listOf(AxisBreak(10.0, 1000.0)), 0.05))
        assertTrue(vt is AxisValueTransform.Broken)
    }

    @Test
    fun ticksInsideBreaksAreDropped() {
        val axis = AxisSpec(scale = AxisScale.Broken(listOf(AxisBreak(10.0, 1000.0))))
        val ticks =
            planAxisValues(
                axis = axis,
                searchRange = ClosedRangeD(0.0, 1010.0),
                viewportRange = ClosedRangeD(0.0, 1010.0),
            )
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.none { it > 10.0 && it < 1000.0 }, "no tick should fall inside the break")
    }
}
