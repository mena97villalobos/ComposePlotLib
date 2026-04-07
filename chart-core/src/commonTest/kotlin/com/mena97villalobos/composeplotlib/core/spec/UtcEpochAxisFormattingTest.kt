package com.mena97villalobos.composeplotlib.core.spec

import kotlin.test.Test
import kotlin.test.assertEquals

class UtcEpochAxisFormattingTest {
    @Test
    fun unixEpochFormatsAsUtcMidnight() {
        assertEquals("1970-01-01 00:00", formatEpochMillisUtcYmdHm(0L))
    }

    @Test
    fun laterInstantFormatsUtcWallClock() {
        val ms = 86_400_000L + 3_600_000L + 45 * 60_000L // 1970-01-02 01:45
        assertEquals("1970-01-02 01:45", formatEpochMillisUtcYmdHm(ms))
    }

    @Test
    fun negativeMillisCrossesInto1969() {
        assertEquals("1969-12-31 23:00", formatEpochMillisUtcYmdHm(-3_600_000L))
    }
}
