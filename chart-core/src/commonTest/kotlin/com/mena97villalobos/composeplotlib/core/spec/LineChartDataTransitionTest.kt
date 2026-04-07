package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LineChartDataTransitionTest {
    private val specA =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "a",
                        label = "A",
                        points = listOf(ChartPoint(0.0, 0.0), ChartPoint(1.0, 10.0)),
                    ),
                ),
        )

    private val specB =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "a",
                        label = "A",
                        points = listOf(ChartPoint(0.0, 2.0), ChartPoint(1.0, 4.0)),
                    ),
                ),
        )

    @Test
    fun canMorph_sameStructure() {
        assertTrue(canMorphLineSpecs(specA, specB))
    }

    @Test
    fun canMorph_differentPointCount() {
        val short =
            LineChartSpec(
                series = listOf(LineSeries("a", "A", listOf(ChartPoint(0.0, 0.0)))),
            )
        assertFalse(canMorphLineSpecs(specA, short))
    }

    @Test
    fun lerp_midpoint() {
        val mid = lerpLineChartSpecForDataTransition(specA, specB, 0.5)!!
        assertEquals(1.0, mid.series[0].points[0].y, 1e-9)
        assertEquals(7.0, mid.series[0].points[1].y, 1e-9)
    }

    @Test
    fun lerp_endpoints() {
        assertEquals(specA, lerpLineChartSpecForDataTransition(specA, specB, 0.0))
        assertEquals(specB, lerpLineChartSpecForDataTransition(specA, specB, 1.0))
    }

    @Test
    fun lerp_mismatched_returnsNull() {
        val other =
            LineChartSpec(
                series = listOf(LineSeries("b", "B", listOf(ChartPoint(0.0, 0.0)))),
            )
        assertNotNull(lerpLineChartSpecForDataTransition(specA, specB, 0.5))
        assertEquals(null, lerpLineChartSpecForDataTransition(specA, other, 0.5))
    }
}
