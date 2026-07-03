package com.mena97villalobos.composeplotlib.core.layout.planning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogTickPlannerTest {
    @Test
    fun logTicksAreDecadesOverWideRange() {
        val ticks = planLogTicks(loRaw = 1.0, hiRaw = 1000.0, base = 10.0, tickCount = 6)
        assertEquals(listOf(1.0, 10.0, 100.0, 1000.0), ticks)
    }

    @Test
    fun logTicksAddMinorsOverNarrowRange() {
        val ticks = planLogTicks(loRaw = 1.0, hiRaw = 100.0, base = 10.0, tickCount = 8)
        // <= 2 decades => include 2x/5x minors.
        assertTrue(ticks.containsAll(listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0)), "got $ticks")
        assertEquals(ticks, ticks.sorted())
    }

    @Test
    fun logTicksEmptyWhenRangeNonPositive() {
        assertTrue(planLogTicks(loRaw = -10.0, hiRaw = -1.0, base = 10.0, tickCount = 5).isEmpty())
    }

    @Test
    fun logTicksFloorNonPositiveLowerBound() {
        // hi positive, lo <= 0 -> floored to a few decades below hi; still yields decade ticks up to 1000.
        val ticks = planLogTicks(loRaw = 0.0, hiRaw = 1000.0, base = 10.0, tickCount = 6)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.contains(1000.0) && ticks.contains(100.0))
        assertTrue(ticks.all { it > 0.0 })
    }

    @Test
    fun symLogTicksIncludeZeroAndSymmetricDecades() {
        val ticks = planSymLogTicks(loRaw = -100.0, hiRaw = 100.0, base = 10.0, linearThreshold = 1.0, tickCount = 8)
        assertTrue(ticks.contains(0.0))
        assertTrue(ticks.containsAll(listOf(-100.0, -10.0, -1.0, 1.0, 10.0, 100.0)), "got $ticks")
        assertEquals(ticks, ticks.sorted())
    }
}
