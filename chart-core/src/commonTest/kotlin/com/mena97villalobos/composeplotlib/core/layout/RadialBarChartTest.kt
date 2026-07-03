package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.RadialBarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadialBarTrack
import com.mena97villalobos.composeplotlib.core.spec.resolvedMaxValue
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RadialBarChartTest {
    private fun plan(spec: RadialBarChartSpec): ChartLayoutPlan {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(320.0, 320.0),
                viewport = ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)),
            )
        return ChartPlanner.planRadialBarChart(input, spec).layout
    }

    @Test
    fun emitsTrackAndValueArcsPerRing() {
        val spec =
            RadialBarChartSpec(
                tracks =
                    listOf(
                        RadialBarTrack("A", 80.0, ChartColor(0xFF4E79A7)),
                        RadialBarTrack("B", 50.0, ChartColor(0xFFF28E2B)),
                        RadialBarTrack("C", 20.0, ChartColor(0xFF59A14F)),
                    ),
                maxValue = 100.0,
            )
        val arcs = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>()
        // Two arcs per ring (background track + value arc).
        assertEquals(6, arcs.size)
        // Value arc for track A (index 1) has sweep 360 * 80/100 = 288.
        assertEquals(288.0, arcs[1].sweepAngleDegrees, 1e-6)
        // Background arcs span the full 360.
        assertEquals(360.0, arcs[0].sweepAngleDegrees, 1e-6)
    }

    @Test
    fun autoScalesToLargestTrackWhenMaxValueNull() {
        val spec =
            RadialBarChartSpec(
                tracks =
                    listOf(
                        RadialBarTrack("A", 40.0, ChartColor(0xFF4E79A7)),
                        RadialBarTrack("B", 10.0, ChartColor(0xFFF28E2B)),
                    ),
            )
        assertEquals(40.0, spec.resolvedMaxValue(), 1e-9)
        val arcs = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>()
        // Largest track fills the full sweep.
        assertEquals(360.0, arcs[1].sweepAngleDegrees, 1e-6)
    }

    @Test
    fun clampsValueAboveMax() {
        val spec =
            RadialBarChartSpec(
                tracks = listOf(RadialBarTrack("A", 150.0, ChartColor(0xFF4E79A7))),
                maxValue = 100.0,
            )
        val arcs = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.Arc>()
        assertTrue(arcs[1].sweepAngleDegrees <= 360.0 + 1e-9)
        assertEquals(360.0, arcs[1].sweepAngleDegrees, 1e-6)
    }
}
