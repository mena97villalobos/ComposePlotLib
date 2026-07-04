package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartColorShadesTest {
    @Test
    fun lightenMovesChannelsTowardWhiteAndKeepsAlpha() {
        val c = ChartColor.argb(200, 100, 100, 100)
        val light = c.lighten(0.5)
        // 100 + (255-100)*0.5 = 177.5 -> 178.
        assertEquals(178, light.red)
        assertEquals(178, light.green)
        assertEquals(178, light.blue)
        assertEquals(200, light.alpha)
    }

    @Test
    fun darkenMovesChannelsTowardBlack() {
        val dark = ChartColor.rgb(100, 100, 100).darken(0.5)
        assertEquals(50, dark.red)
        assertEquals(255, dark.alpha)
    }

    @Test
    fun fractionsAreClamped() {
        val c = ChartColor.rgb(120, 120, 120)
        assertEquals(ChartColor.rgb(255, 255, 255), c.lighten(2.0))
        assertEquals(ChartColor.rgb(0, 0, 0), c.darken(2.0))
        assertEquals(c, c.lighten(-1.0))
    }

    @Test
    fun darkBackgroundPaletteIsLighterThanSource() {
        val source = ChartPalettes.Tableau10
        val adapted = ChartPalettes.forDarkBackground(source)
        assertEquals(source.size, adapted.size)
        // Every adapted color is at least as light as its source (channel-wise, non-decreasing).
        source.zip(adapted).forEach { (s, a) ->
            assertTrue(a.red >= s.red && a.green >= s.green && a.blue >= s.blue)
        }
    }
}
