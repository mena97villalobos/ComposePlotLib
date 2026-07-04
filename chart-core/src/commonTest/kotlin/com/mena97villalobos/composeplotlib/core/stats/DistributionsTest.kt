package com.mena97villalobos.composeplotlib.core.stats

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistributionsTest {
    @Test
    fun histogramBinsCoverRangeAndCountAllValues() {
        val values = listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)
        val bins = histogramBins(values, binCount = 5)
        assertEquals(5, bins.size)
        assertEquals(0.0, bins.first().start, 1e-9)
        assertEquals(10.0, bins.last().end, 1e-9)
        // Every value falls in exactly one bin.
        assertEquals(values.size, bins.sumOf { it.count })
    }

    @Test
    fun histogramEmptyAndDegenerateCases() {
        assertTrue(histogramBins(emptyList(), 4).isEmpty())
        assertTrue(histogramBins(listOf(1.0, 2.0), 0).isEmpty())
        val flat = histogramBins(listOf(3.0, 3.0, 3.0), 4)
        assertEquals(1, flat.size)
        assertEquals(3, flat.first().count)
    }

    @Test
    fun kernelDensityIntegratesToApproximatelyOne() {
        val values = listOf(-2.0, -1.0, 0.0, 0.0, 1.0, 2.0, 0.5, -0.5, 1.5, -1.5)
        val lo = -6.0
        val hi = 6.0
        val steps = 600
        val dx = (hi - lo) / steps
        val xs = (0..steps).map { lo + it * dx }
        val density = kernelDensity(values, xs)
        // Trapezoidal integral of a valid PDF ≈ 1.
        var area = 0.0
        for (i in 0 until steps) area += (density[i] + density[i + 1]) / 2.0 * dx
        assertEquals(1.0, area, 0.02)
        density.forEach { assertTrue(it >= 0.0) }
    }

    @Test
    fun silvermanBandwidthIsPositive() {
        assertTrue(silvermanBandwidth(listOf(1.0, 2.0, 3.0, 4.0, 5.0)) > 0.0)
        assertEquals(1.0, silvermanBandwidth(listOf(5.0)), 1e-9) // too few points → fallback
    }
}
