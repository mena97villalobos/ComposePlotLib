package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PieHitResolverTest {
    private val plot = RectD(left = 0.0, top = 0.0, right = 200.0, bottom = 200.0)

    @Test
    fun hit_top_quadrant_firstSlice_of_four() {
        val spec =
            PieChartSpec(
                id = "p",
                slices =
                    List(4) { i ->
                        PieChartSlice(1.0, "s$i", ChartColor.Blue)
                    },
                legend = LegendSpec(enabled = false),
            )
        val sel = PieHitResolver.resolve(OffsetD(100.0, 20.0), spec, plot)
        assertNotNull(sel)
        assertEquals(0, sel.target.pointIndex)
        assertEquals("p", sel.target.seriesId)
    }

    @Test
    fun hit_right_quadrant_secondSlice() {
        val spec =
            PieChartSpec(
                slices =
                    List(4) { i ->
                        PieChartSlice(1.0, "s$i", ChartColor.Blue)
                    },
                legend = LegendSpec(enabled = false),
            )
        val sel = PieHitResolver.resolve(OffsetD(180.0, 100.0), spec, plot)
        assertNotNull(sel)
        assertEquals(1, sel.target.pointIndex)
    }

    @Test
    fun outside_outer_radius_returnsNull() {
        val spec =
            PieChartSpec(
                slices = listOf(PieChartSlice(1.0, "a", ChartColor.Blue)),
                legend = LegendSpec(enabled = false),
            )
        assertNull(PieHitResolver.resolve(OffsetD(100.0, 5.0), spec, plot))
    }

    @Test
    fun inside_donut_hole_returnsNull() {
        val spec =
            PieChartSpec(
                slices =
                    listOf(
                        PieChartSlice(1.0, "a", ChartColor.Blue),
                        PieChartSlice(1.0, "b", ChartColor.Green),
                    ),
                holeRadiusRatio = 0.5,
                legend = LegendSpec(enabled = false),
            )
        assertNull(PieHitResolver.resolve(OffsetD(100.0, 100.0), spec, plot))
    }

    @Test
    fun skipsZeroValue_sliceIndexStillOriginal() {
        val spec =
            PieChartSpec(
                slices =
                    listOf(
                        PieChartSlice(1.0, "a", ChartColor.Blue),
                        PieChartSlice(0.0, "b", ChartColor.Green),
                        PieChartSlice(1.0, "c", ChartColor.Red),
                    ),
                legend = LegendSpec(enabled = false),
            )
        val sel = PieHitResolver.resolve(OffsetD(100.0, 190.0), spec, plot)
        assertNotNull(sel)
        assertEquals(2, sel.target.pointIndex)
    }

    @Test
    fun nonFiniteTouch_returnsNull() {
        val spec =
            PieChartSpec(
                slices = listOf(PieChartSlice(1.0, "a", ChartColor.Blue)),
                legend = LegendSpec(enabled = false),
            )
        assertNull(PieHitResolver.resolve(OffsetD(Double.NaN, 100.0), spec, plot))
    }

    @Test
    fun hit_inSliceGap_returnsNull() {
        val spec =
            PieChartSpec(
                id = "p",
                slices =
                    listOf(
                        PieChartSlice(1.0, "a", ChartColor.Blue),
                        PieChartSlice(1.0, "b", ChartColor.Green),
                    ),
                sliceGapDegrees = 10.0,
                legend = LegendSpec(enabled = false),
            )
        val centerX = plot.left + plot.width / 2.0
        val centerY = plot.top + plot.height / 2.0
        val r = minOf(plot.width, plot.height) * 0.45 * 0.5
        val deg = -90.0 + 170.0 + 5.0
        val rad = deg * PI / 180.0
        val touch = OffsetD(centerX + r * cos(rad), centerY + r * sin(rad))
        assertNull(PieHitResolver.resolve(touch, spec, plot))
    }
}
