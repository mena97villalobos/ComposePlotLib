package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.BubblePoint
import com.mena97villalobos.composeplotlib.core.model.BubbleSeries
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BubbleChartSpecTest {
    @Test
    fun sizeScaleIsAreaProportional() {
        val scale = BubbleSizeScale(minValue = 0.0, maxValue = 100.0, minRadiusPx = 0.0, maxRadiusPx = 10.0)
        assertEquals(0.0, scale.radiusFor(0.0), 1e-9)
        assertEquals(10.0, scale.radiusFor(100.0), 1e-9)
        // Area-proportional midpoint: radius = sqrt(0.5) * 10 ~= 7.07, NOT the linear 5.
        assertEquals(kotlin.math.sqrt(50.0), scale.radiusFor(50.0), 1e-9)
        assertTrue(scale.radiusFor(50.0) > 5.0)
    }

    @Test
    fun sizeScaleClampsAndHandlesNonFinite() {
        val scale = BubbleSizeScale(0.0, 10.0, 2.0, 8.0)
        assertEquals(2.0, scale.radiusFor(-5.0), 1e-9)
        assertEquals(8.0, scale.radiusFor(50.0), 1e-9)
        assertEquals(2.0, scale.radiusFor(Double.NaN), 1e-9)
    }

    @Test
    fun autoDerivesRange() {
        val scale = BubbleSizeScale.auto(listOf(4.0, 1.0, Double.NaN, 9.0))
        assertEquals(1.0, scale.minValue, 1e-9)
        assertEquals(9.0, scale.maxValue, 1e-9)
    }

    @Test
    fun legendTicksSpanRange() {
        val scale = BubbleSizeScale(0.0, 100.0)
        val ticks = scale.legendTicks(3)
        assertEquals(listOf(0.0, 50.0, 100.0), ticks.map { it.first })
        assertEquals(scale.radiusFor(100.0), ticks.last().second, 1e-9)
    }

    @Test
    fun toScatterSpecMapsRadiusAndColorPerBubble() {
        val spec =
            BubbleChartSpec(
                series =
                    listOf(
                        BubbleSeries(
                            "s",
                            "S",
                            listOf(
                                BubblePoint(0.0, 0.0, z = 0.0),
                                BubblePoint(1.0, 1.0, z = 100.0, color = ChartColor.Red),
                            ),
                            color = ChartColor.Blue,
                        ),
                    ),
                sizeScale = BubbleSizeScale(0.0, 100.0, minRadiusPx = 2.0, maxRadiusPx = 20.0),
            )
        val scatter = spec.toScatterSpec()
        val pts = scatter.series.single().points
        assertEquals(2.0, pts[0].markerRadius)
        assertEquals(20.0, pts[1].markerRadius)
        assertEquals(ChartColor.Blue, pts[0].markerColor) // falls back to series color
        assertEquals(ChartColor.Red, pts[1].markerColor) // per-bubble override
    }
}
