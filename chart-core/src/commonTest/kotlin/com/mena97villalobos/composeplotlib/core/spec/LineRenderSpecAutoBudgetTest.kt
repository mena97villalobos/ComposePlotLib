package com.mena97villalobos.composeplotlib.core.spec

import kotlin.test.Test
import kotlin.test.assertEquals

class LineRenderSpecAutoBudgetTest {
    @Test
    fun autoMaxPointsClampsToMinAndMax() {
        val o = LineRenderSpec.DEFAULT_PLOT_WIDTH_OVERSAMPLE
        assertEquals(
            LineRenderSpec.MIN_AUTO_MAX_POINTS,
            LineRenderSpec.autoMaxPointsForPlotWidthPx(10.0, oversample = o),
        )
        assertEquals(900, LineRenderSpec.autoMaxPointsForPlotWidthPx(400.0, oversample = o))
        assertEquals(
            LineRenderSpec.MAX_AUTO_MAX_POINTS,
            LineRenderSpec.autoMaxPointsForPlotWidthPx(100_000.0, oversample = o),
        )
    }

    @Test
    fun autoMaxPointsDefaultOversampleMatchesConstant() {
        assertEquals(
            LineRenderSpec.autoMaxPointsForPlotWidthPx(400.0),
            LineRenderSpec.autoMaxPointsForPlotWidthPx(400.0, oversample = LineRenderSpec.DEFAULT_PLOT_WIDTH_OVERSAMPLE),
        )
    }

    @Test
    fun autoMaxPointsFallsBackForNonFiniteInput() {
        assertEquals(4096, LineRenderSpec.autoMaxPointsForPlotWidthPx(Double.NaN))
        assertEquals(4096, LineRenderSpec.autoMaxPointsForPlotWidthPx(-1.0))
        assertEquals(4096, LineRenderSpec.autoMaxPointsForPlotWidthPx(100.0, oversample = Double.NaN))
    }
}
