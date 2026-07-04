package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.PolarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PolarSector
import com.mena97villalobos.composeplotlib.core.spec.resolvedMaxValue
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PolarChartTest {
    private fun plan(spec: PolarChartSpec): ChartLayoutPlan {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(320.0, 320.0),
                viewport = ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)),
            )
        return ChartPlanner.planPolarChart(input, spec).layout
    }

    private val spec =
        PolarChartSpec(
            sectors =
                listOf(
                    PolarSector("N", 10.0, ChartColor(0xFF4E79A7)),
                    PolarSector("E", 6.0, ChartColor(0xFFF28E2B)),
                    PolarSector("S", 8.0, ChartColor(0xFF59A14F)),
                    PolarSector("W", 4.0, ChartColor(0xFFE15759)),
                ),
            maxValue = 10.0,
            gapDegrees = 0.0,
            ringCount = 4,
        )

    @Test
    fun emitsRingsAndFilledWedges() {
        val arcs = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>()
        val rings = arcs.filter { it.style is ShapeDrawStyle.Stroke }
        val wedges = arcs.filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(4, rings.size) // ringCount
        assertEquals(4, wedges.size) // one per sector
        // Each ring spans the full circle.
        rings.forEach { assertEquals(360.0, it.sweepAngleDegrees, 1e-9) }
    }

    @Test
    fun wedgeRadiusIsProportionalToValue() {
        val wedges = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>().filter { it.style is ShapeDrawStyle.Fill }
        // N (value 10 of max 10) is the largest; W (value 4) is the smallest.
        val nRadius = wedges[0].radius
        val wRadius = wedges[3].radius
        assertTrue(nRadius > wRadius)
        // E (6) radius = 0.6 * N radius.
        assertEquals(0.6 * nRadius, wedges[1].radius, 1e-6)
    }

    @Test
    fun wedgesEvenlyDivideTheCircleWithNoGap() {
        val wedges = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>().filter { it.style is ShapeDrawStyle.Fill }
        wedges.forEach { assertEquals(90.0, it.sweepAngleDegrees, 1e-9) } // 360 / 4
    }

    @Test
    fun autoScalesToLargestSector() {
        val auto = spec.copy(maxValue = null)
        assertEquals(10.0, auto.resolvedMaxValue(), 1e-9)
    }
}
