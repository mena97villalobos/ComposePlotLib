package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Rendering-oriented invariants: planner output structure (counts, ordering, legend) without pixels.
 * (M15 — testing roadmap.)
 */
class ChartPlannerOutputInvariantTest {
    private val cartesianInput =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 280.0),
            viewport =
                ChartViewport(
                    xRange = ClosedRangeD(0.0, 10.0),
                    yRange = ClosedRangeD(0.0, 20.0),
                ),
        )

    @Test
    fun barChart_legendEntryLabelsMatchGroupLabels() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "alpha",
                            label = "Alpha row",
                            style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                            entries = listOf(BarEntry(1.0, 5.0)),
                        ),
                        BarGroup(
                            id = "beta",
                            label = "Beta row",
                            style = BarStyle(color = ChartColor.Red, barWidth = 20.0),
                            entries = listOf(BarEntry(2.0, 7.0)),
                        ),
                    ),
                legend = LegendSpec(enabled = true),
                xAxis = AxisSpec(showGridLines = true, tickCount = 5),
                yAxis = AxisSpec(showGridLines = true, tickCount = 5),
            )
        val plan = ChartPlanner.planBarChart(cartesianInput, spec, selection = null)
        assertEquals(2, plan.layout.legendEntries.size)
        val labels = plan.layout.legendEntries.map { it.label }.toSet()
        assertEquals(setOf("Alpha row", "Beta row"), labels)
        val gridLines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertTrue(gridLines.isNotEmpty(), "expected horizontal/vertical grid lines")
    }

    @Test
    fun scatterChart_xTickValuesAreNonDecreasing() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "s",
                            label = "S",
                            points =
                                listOf(
                                    ChartPoint(1.0, 2.0),
                                    ChartPoint(4.0, 8.0),
                                ),
                            pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Blue),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 6),
                yAxis = AxisSpec(tickCount = 5),
            )
        val plan = ChartPlanner.planScatterChart(cartesianInput, spec, selection = null)
        val xs = plan.layout.xTicks.map { it.value }
        assertEquals(xs.sorted(), xs)
    }

    @Test
    fun scatterChart_selectionAddsCircleHighlightCommand() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "s",
                            label = "S",
                            points = listOf(ChartPoint(2.0, 6.0)),
                            pointStyle = PointStyle(visible = true, radius = 5.0, color = ChartColor.Blue),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val selection =
            ChartSelection(
                target = HighlightTarget(seriesId = "s", pointIndex = 0, point = XyPoint(2.0, 6.0)),
                pixelOffset = OffsetD(100.0, 120.0),
            )
        val without = ChartPlanner.planScatterChart(cartesianInput, spec, selection = null)
        val withSel = ChartPlanner.planScatterChart(cartesianInput, spec, selection = selection)
        val linesNo = without.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>().size
        val linesSel = withSel.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>().size
        val circlesNo = without.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>().size
        val circlesSel = withSel.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>().size
        assertTrue(linesSel >= linesNo + 1, "selection overlay adds vertical crosshair line")
        assertTrue(circlesSel >= circlesNo + 1, "selection overlay adds highlight point circle")
    }
}
