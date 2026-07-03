package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.StackedAreaSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AreaStackOffset
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.StackedAreaChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackedAreaTest {
    private fun input(yMax: Double) =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 2.0), yRange = ClosedRangeD(0.0, yMax)),
        )

    private fun spec(offset: AreaStackOffset) =
        StackedAreaChartSpec(
            xValues = listOf(0.0, 1.0, 2.0),
            series =
                listOf(
                    StackedAreaSeries("a", "A", listOf(1.0, 1.0, 1.0), ChartColor.Blue),
                    StackedAreaSeries("b", "B", listOf(3.0, 3.0, 3.0), ChartColor.Green),
                ),
            offset = offset,
            xAxis = AxisSpec(showGridLines = false),
            yAxis = AxisSpec(showGridLines = false),
        )

    private fun firstMoveToY(path: DrawCommand.Path): Double = (path.segments.first() as PathSegment.MoveTo).point.y

    @Test
    fun emitsOneFilledBandPerSeries() {
        val plan = ChartPlanner.planStackedAreaChart(input(4.0), spec(AreaStackOffset.Zero))
        assertEquals(2, plan.layout.plotDrawCommands.count { it is DrawCommand.Path })
    }

    @Test
    fun bandsStackWithUpperBoundaryRising() {
        val plan = ChartPlanner.planStackedAreaChart(input(4.0), spec(AreaStackOffset.Zero))
        val paths = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Path>()
        // A (bottom) upper = 1; B (top) upper = 4. Larger domain y => smaller pixel y => higher on screen.
        assertTrue(firstMoveToY(paths[1]) < firstMoveToY(paths[0]))
    }

    @Test
    fun expandNormalizesTopBoundaryToOne() {
        // With Expand + a [0,1] viewport, the top band's upper boundary reaches domain 1 => plot top.
        val plan = ChartPlanner.planStackedAreaChart(input(1.0), spec(AreaStackOffset.Expand))
        val paths = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Path>()
        val topBandFirstY = firstMoveToY(paths[1])
        assertTrue(kotlin.math.abs(topBandFirstY - plan.layout.plotBounds.top) < 1.0, "topY=$topBandFirstY")
    }

    @Test
    fun emptyForFewerThanTwoXValues() {
        val plan =
            ChartPlanner.planStackedAreaChart(
                input(4.0),
                spec(AreaStackOffset.Zero).copy(xValues = listOf(0.0)),
            )
        assertEquals(0, plan.layout.plotDrawCommands.count { it is DrawCommand.Path })
    }
}
