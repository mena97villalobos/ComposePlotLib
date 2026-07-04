package com.mena97villalobos.composeplotlib.core.indicators

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TechnicalIndicatorsTest {
    @Test
    fun smaIsNullUntilWindowThenAverages() {
        val sma = TechnicalIndicators.sma(listOf(1.0, 2.0, 3.0, 4.0, 5.0), period = 3)
        assertNull(sma[0])
        assertNull(sma[1])
        assertEquals(2.0, sma[2]!!, 1e-9) // (1+2+3)/3
        assertEquals(3.0, sma[3]!!, 1e-9) // (2+3+4)/3
        assertEquals(4.0, sma[4]!!, 1e-9) // (3+4+5)/3
    }

    @Test
    fun emaSeedsWithSmaThenSmooths() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val ema = TechnicalIndicators.ema(values, period = 3)
        assertNull(ema[1])
        assertEquals(2.0, ema[2]!!, 1e-9) // seed = SMA(1,2,3)
        // k = 2/4 = 0.5; ema[3] = 4*0.5 + 2*0.5 = 3.0; ema[4] = 5*0.5 + 3*0.5 = 4.0
        assertEquals(3.0, ema[3]!!, 1e-9)
        assertEquals(4.0, ema[4]!!, 1e-9)
    }

    @Test
    fun rsiIs100WhenOnlyGains() {
        val values = (0..20).map { it.toDouble() } // strictly increasing → no losses
        val rsi = TechnicalIndicators.rsi(values, period = 14)
        assertNull(rsi[13])
        assertEquals(100.0, rsi[14]!!, 1e-9)
    }

    @Test
    fun rsiStaysWithinBounds() {
        val values = listOf(44.0, 44.3, 44.1, 44.6, 43.8, 44.2, 45.1, 45.4, 45.0, 45.8, 46.0, 45.6, 46.3, 46.6, 46.2, 46.9, 47.1)
        val rsi = TechnicalIndicators.rsi(values, period = 14)
        rsi.filterNotNull().forEach { assertTrue(it in 0.0..100.0) }
        assertTrue(rsi[14] != null)
    }

    @Test
    fun macdHistogramIsMacdMinusSignal() {
        val values = (1..40).map { it.toDouble() }
        val result = TechnicalIndicators.macd(values, fastPeriod = 12, slowPeriod = 26, signalPeriod = 9)
        assertEquals(values.size, result.macd.size)
        val i = values.lastIndex
        val m = result.macd[i]
        val s = result.signal[i]
        val h = result.histogram[i]
        assertTrue(m != null && s != null && h != null)
        assertEquals(m!! - s!!, h!!, 1e-9)
    }

    @Test
    fun bollingerBandsBracketTheMiddle() {
        val values = listOf(2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0)
        val bands = TechnicalIndicators.bollingerBands(values, period = 5, stdDevMultiplier = 2.0)
        val i = values.lastIndex
        val mid = bands.middle[i]!!
        assertEquals(16.0, mid, 1e-9) // SMA(12,14,16,18,20)
        assertTrue(bands.upper[i]!! > mid)
        assertTrue(bands.lower[i]!! < mid)
        // Symmetric around the middle.
        assertEquals(bands.upper[i]!! - mid, mid - bands.lower[i]!!, 1e-9)
    }
}
