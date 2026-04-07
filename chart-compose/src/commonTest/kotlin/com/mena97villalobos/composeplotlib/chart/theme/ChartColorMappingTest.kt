package com.mena97villalobos.composeplotlib.chart.theme

import androidx.compose.ui.graphics.Color
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartColorMappingTest {
    @Test
    fun toChartColor_opaqueRed() {
        val c = Color(0xFFFF0000)
        assertEquals(0xFFFF0000L, c.toChartColor().argb)
    }

    @Test
    fun toChartColor_semiTransparent() {
        val c = Color(0x80FF0000)
        assertEquals(0x80FF0000L, c.toChartColor().argb)
    }

    @Test
    fun toChartColor_unspecifiedUsesFallback() {
        assertEquals(ChartColor.Green, Color.Unspecified.toChartColor(fallback = ChartColor.Green))
    }
}
