package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartColorContrastTest {
    @Test
    fun knownRatios() {
        assertEquals(21.0, ChartColor.Black.contrastRatio(ChartColor.White), 1e-9)
        assertEquals(1.0, ChartColor.Blue.contrastRatio(ChartColor.Blue), 1e-9)
        assertEquals(0.0, ChartColor.Black.relativeLuminance(), 1e-12)
        assertEquals(1.0, ChartColor.White.relativeLuminance(), 1e-12)
        // #777777 on white is the classic 4.48:1 example.
        assertEquals(4.48, ChartColor(0xFF777777).contrastRatio(ChartColor.White), 0.01)
    }

    @Test
    fun readableContentColor_picksTheStrongerOfBlackAndWhite() {
        assertEquals(ChartColor.White, ChartColor(0xFF1565C0).readableContentColor())
        assertEquals(ChartColor.Black, ChartColor(0xFFEDC948).readableContentColor())
    }

    @Test
    fun readableContentColor_reachesAA_onEveryBuiltInPaletteColorAndItsTreemapShades() {
        val fills =
            (ChartPalettes.Tableau10 + ChartPalettes.Category10 + ChartPalettes.ColorblindSafe)
                .flatMap { base -> (0..4).map { depth -> base.lighten(0.14 * depth) } } + ChartColor(0xFFBAB0AC)
        fills.forEach { fill ->
            val ratio = fill.contrastRatio(fill.readableContentColor())
            assertTrue(ratio >= 4.5, "fill ${fill.argb.toString(16)} only reaches $ratio")
        }
    }

    @Test
    fun readableContentColor_reachesAA_forEveryGray() {
        for (v in 0..255) {
            val gray = ChartColor.rgb(v, v, v)
            assertTrue(gray.contrastRatio(gray.readableContentColor()) >= 4.5)
        }
    }
}
