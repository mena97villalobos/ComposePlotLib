package com.mena97villalobos.composeplotlib.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartColorTest {
    @Test
    fun rgb_packsOpaqueArgb() {
        assertEquals(0xFF1565C0L, ChartColor.rgb(0x15, 0x65, 0xC0).argb)
    }

    @Test
    fun argb_packsAllChannels() {
        val c = ChartColor.argb(0x80, 0x12, 0x34, 0x56)
        assertEquals(0x80123456L, c.argb)
    }

    @Test
    fun rgb_clampsOutOfRangeChannels() {
        assertEquals(0xFFFF00FFL, ChartColor.rgb(300, -10, 999).argb)
    }

    @Test
    fun channelAccessors_decodeArgb() {
        val c = ChartColor(0x80123456)
        assertEquals(0x80, c.alpha)
        assertEquals(0x12, c.red)
        assertEquals(0x34, c.green)
        assertEquals(0x56, c.blue)
    }

    @Test
    fun hex_sixDigitsIsOpaque() {
        assertEquals(0xFF1565C0L, ChartColor.hex("#1565C0").argb)
        assertEquals(0xFF1565C0L, ChartColor.hex("1565c0").argb)
    }

    @Test
    fun hex_eightDigitsCarriesAlpha() {
        assertEquals(0x801565C0L, ChartColor.hex("#801565C0").argb)
    }

    @Test
    fun hex_threeDigitShorthandExpands() {
        assertEquals(0xFFFF0000L, ChartColor.hex("#F00").argb)
        assertEquals(0xFFAABBCCL, ChartColor.hex("ABC").argb)
    }

    @Test
    fun hex_rejectsBadInput() {
        assertFailsWith<IllegalArgumentException> { ChartColor.hex("#12") }
        assertFailsWith<IllegalArgumentException> { ChartColor.hex("#ZZZZZZ") }
        // A leading sign is not a hex digit and must be rejected (not parsed by toLong's sign handling).
        assertFailsWith<IllegalArgumentException> { ChartColor.hex("#-fffff") }
        assertFailsWith<IllegalArgumentException> { ChartColor.hex("+abcde") }
    }

    @Test
    fun hex_eightDigitAllFIsWhiteWithFullAlpha() {
        val c = ChartColor.hex("#FFFFFFFF")
        assertEquals(0xFFFFFFFFL, c.argb)
        assertEquals(255, c.alpha)
        assertEquals(255, c.red)
    }

    @Test
    fun withAlpha_replacesAlphaKeepsRgb() {
        val base = ChartColor.rgb(0x12, 0x34, 0x56)
        assertEquals(0x80123456L, base.withAlpha(128.0 / 255.0).argb)
        assertEquals(0x00123456L, base.withAlpha(0.0).argb)
        assertEquals(0xFF123456L, base.withAlpha(1.0).argb)
    }

    @Test
    fun withAlpha_clampsFraction() {
        val base = ChartColor.rgb(0, 0, 0)
        assertEquals(0xFF000000L, base.withAlpha(5.0).argb)
        assertEquals(0x00000000L, base.withAlpha(-5.0).argb)
    }

    @Test
    fun hsl_primaryColors() {
        assertEquals(0xFFFF0000L, ChartColor.hsl(0.0, 1.0, 0.5).argb)
        assertEquals(0xFF00FF00L, ChartColor.hsl(120.0, 1.0, 0.5).argb)
        assertEquals(0xFF0000FFL, ChartColor.hsl(240.0, 1.0, 0.5).argb)
    }

    @Test
    fun hsl_grayscaleWhenSaturationZero() {
        assertEquals(0xFF000000L, ChartColor.hsl(0.0, 0.0, 0.0).argb)
        assertEquals(0xFFFFFFFFL, ChartColor.hsl(0.0, 0.0, 1.0).argb)
        assertEquals(0xFF808080L, ChartColor.hsl(210.0, 0.0, 0.5019607843).argb)
    }

    @Test
    fun hsl_wrapsHueAndAppliesAlpha() {
        // hue 360 wraps to 0 (red); alpha 0.5 -> 0x80
        assertEquals(0x80FF0000L, ChartColor.hsl(360.0, 1.0, 0.5, alpha = 128.0 / 255.0).argb)
    }
}
