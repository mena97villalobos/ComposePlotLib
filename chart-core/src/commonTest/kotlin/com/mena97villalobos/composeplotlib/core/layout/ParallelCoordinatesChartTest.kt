package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.ParallelAxis
import com.mena97villalobos.composeplotlib.core.spec.ParallelCoordinatesChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ParallelSeries
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParallelCoordinatesChartTest {
    private fun plan(spec: ParallelCoordinatesChartSpec): ChartLayoutPlan {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 300.0),
                viewport = ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)),
            )
        return ChartPlanner.planParallelCoordinatesChart(input, spec).layout
    }

    private val spec =
        ParallelCoordinatesChartSpec(
            axes =
                listOf(
                    ParallelAxis("A", 0.0, 10.0),
                    ParallelAxis("B", 0.0, 100.0),
                    ParallelAxis("C", -1.0, 1.0),
                ),
            series =
                listOf(
                    ParallelSeries("s1", "S1", listOf(5.0, 50.0, 0.0), ChartColor(0xFF4E79A7)),
                    ParallelSeries("s2", "S2", listOf(10.0, 0.0, 1.0), ChartColor(0xFFF28E2B)),
                ),
            legend = LegendSpec(enabled = false),
        )

    @Test
    fun emitsAxisLinesAndSeriesPolylines() {
        val layout = plan(spec)
        // One vertical axis line per axis (overlay).
        assertEquals(3, layout.overlayDrawCommands.filterIsInstance<DrawCommand.Line>().size)
        // One polyline per series (plot).
        assertEquals(2, layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>().size)
    }

    @Test
    fun polylineCrossesEveryAxisInOrder() {
        val layout = plan(spec)
        val polyline = layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>().first()
        assertEquals(3, polyline.points.size)
        // x coordinates are strictly increasing (axes left→right).
        assertTrue(polyline.points[0].x < polyline.points[1].x)
        assertTrue(polyline.points[1].x < polyline.points[2].x)
    }

    @Test
    fun mapsValueToAxisRange() {
        val layout = plan(spec)
        val s1 = layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>().first()
        // S1 on axis A: value 5 of [0,10] = midpoint; on axis C: value 0 of [-1,1] = midpoint too.
        // Both should map to the same y (plot vertical midpoint).
        assertEquals(s1.points[0].y, s1.points[2].y, 1e-6)
    }
}
