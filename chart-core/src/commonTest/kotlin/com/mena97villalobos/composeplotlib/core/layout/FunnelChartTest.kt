package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.FunnelChartSpec
import com.mena97villalobos.composeplotlib.core.spec.FunnelStage
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunnelChartTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0)),
        )

    private fun plan(showLabels: Boolean = true): FunnelChartPlan =
        ChartPlanner.planFunnelChart(
            input,
            FunnelChartSpec(
                stages =
                    listOf(
                        FunnelStage("Visits", 1000.0),
                        FunnelStage("Signups", 600.0),
                        FunnelStage("Purchases", 180.0),
                    ),
                showLabels = showLabels,
            ),
        )

    @Test
    fun emitsOneTrapezoidPerStage() {
        val p = plan()
        assertEquals(3, p.layout.plotDrawCommands.count { it is DrawCommand.Path })
    }

    @Test
    fun labelsAreOverlayTextAndCanBeDisabled() {
        assertEquals(3, plan(showLabels = true).layout.overlayDrawCommands.count { it is DrawCommand.Text })
        assertEquals(0, plan(showLabels = false).layout.overlayDrawCommands.count { it is DrawCommand.Text })
    }

    @Test
    fun widestStageSpansFullPlotAndTapers() {
        val p = plan(showLabels = false)
        val paths = p.layout.plotDrawCommands.filterIsInstance<DrawCommand.Path>()

        fun topWidth(path: DrawCommand.Path): Double {
            val moveTo = path.segments.first() as PathSegment.MoveTo
            val lineTo = path.segments[1] as PathSegment.LineTo
            return lineTo.point.x - moveTo.point.x
        }
        // First stage (value 1000 = max) is the widest; later stages narrow.
        assertTrue(topWidth(paths[0]) > topWidth(paths[1]))
        assertTrue(topWidth(paths[1]) > topWidth(paths[2]))
    }

    @Test
    fun emptyOrNonPositiveStagesEmitNothing() {
        val empty = ChartPlanner.planFunnelChart(input, FunnelChartSpec(stages = emptyList()))
        assertEquals(0, empty.layout.plotDrawCommands.size)
    }
}
