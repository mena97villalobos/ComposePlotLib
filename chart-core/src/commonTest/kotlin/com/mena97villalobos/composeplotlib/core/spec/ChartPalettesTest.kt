package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartPalettesTest {
    @Test
    fun tableau10HasTenColorsInOrder() {
        assertEquals(10, ChartPalettes.Tableau10.size)
        assertEquals(0xFF4E79A7L, ChartPalettes.Tableau10.first().argb)
        assertEquals(0xFFBAB0ACL, ChartPalettes.Tableau10.last().argb)
    }

    @Test
    fun cyclicWrapsInBothDirections() {
        val p = ChartPalettes.Tableau10
        assertEquals(p[0], ChartPalettes.cyclic(p, 0))
        assertEquals(p[0], ChartPalettes.cyclic(p, 10))
        assertEquals(p[1], ChartPalettes.cyclic(p, 11))
        assertEquals(p.last(), ChartPalettes.cyclic(p, -1))
        assertEquals(ChartColor.Black, ChartPalettes.cyclic(emptyList(), 3))
    }

    @Test
    fun gradientEndpointsAreExactStops() {
        assertEquals(0xFF440154L, ColorGradient.Viridis.colorAt(0.0).argb)
        assertEquals(0xFFFDE725L, ColorGradient.Viridis.colorAt(1.0).argb)
        // Out-of-range clamps.
        assertEquals(0xFF440154L, ColorGradient.Viridis.colorAt(-1.0).argb)
        assertEquals(0xFFFDE725L, ColorGradient.Viridis.colorAt(2.0).argb)
    }

    @Test
    fun gradientInterpolatesMidpoint() {
        val bw = ColorGradient(listOf(ChartColor(0xFF000000), ChartColor(0xFFFFFFFF)))
        // 0 + (255-0)*0.5 = 127.5 -> 127 = 0x7F per channel.
        assertEquals(0xFF7F7F7FL, bw.colorAt(0.5).argb)
    }

    @Test
    fun sampleReturnsRequestedCountWithMatchingEndpoints() {
        val samples = ColorGradient.Turbo.sample(5)
        assertEquals(5, samples.size)
        assertEquals(ColorGradient.Turbo.colorAt(0.0), samples.first())
        assertEquals(ColorGradient.Turbo.colorAt(1.0), samples.last())
        assertEquals(emptyList(), ColorGradient.Turbo.sample(0))
    }

    @Test
    fun emptyGradientRejected() {
        assertFailsWith<IllegalArgumentException> { ColorGradient(emptyList()) }
    }
}
