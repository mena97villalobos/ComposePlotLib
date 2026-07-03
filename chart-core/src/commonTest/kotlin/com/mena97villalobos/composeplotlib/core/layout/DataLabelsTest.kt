package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.DataLabelsSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataLabelsTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun textLabels(commands: List<DrawCommand>): List<String> = commands.filterIsInstance<DrawCommand.Text>().map { it.text }

    private fun lineSpec(
        points: List<ChartPoint>,
        labels: DataLabelsSpec,
    ) = LineChartSpec(
        series = listOf(LineSeries(id = "s", label = "S", points = points)),
        dataLabels = labels,
    )

    @Test
    fun disabledByDefaultEmitsNoLabels() {
        val plan =
            ChartPlanner.planLineChart(
                input,
                lineSpec(listOf(ChartPoint(0.0, 5.0), ChartPoint(2.0, 15.0)), DataLabelsSpec()),
                selection = null,
            )
        assertTrue(textLabels(plan.layout.plotDrawCommands).isEmpty())
    }

    @Test
    fun enabledEmitsOneLabelPerSpacedPoint() {
        // Interior points (away from the plot edges, where a centered label box would clip and be dropped).
        val points = listOf(ChartPoint(0.5, 5.0), ChartPoint(1.5, 12.0), ChartPoint(2.5, 8.0), ChartPoint(3.5, 14.0))
        val plan = ChartPlanner.planLineChart(input, lineSpec(points, DataLabelsSpec(enabled = true)), selection = null)
        val labels = textLabels(plan.layout.plotDrawCommands)
        assertEquals(listOf("5", "12", "8", "14"), labels)
    }

    @Test
    fun overlappingLabelsAreDroppedWhenAvoidOverlap() {
        // Two near-identical points collide; avoidOverlap keeps one.
        val points = listOf(ChartPoint(1.0, 10.0), ChartPoint(1.02, 10.0))
        val avoided =
            ChartPlanner.planLineChart(input, lineSpec(points, DataLabelsSpec(enabled = true, avoidOverlap = true)), selection = null)
        assertEquals(1, textLabels(avoided.layout.plotDrawCommands).size)
        val notAvoided =
            ChartPlanner.planLineChart(input, lineSpec(points, DataLabelsSpec(enabled = true, avoidOverlap = false)), selection = null)
        assertEquals(2, textLabels(notAvoided.layout.plotDrawCommands).size)
    }

    @Test
    fun customFormatterIsUsed() {
        val plan =
            ChartPlanner.planLineChart(
                input,
                lineSpec(
                    listOf(ChartPoint(1.0, 10.0)),
                    DataLabelsSpec(enabled = true, valueFormatter = { "y=${it.toInt()}" }),
                ),
                selection = null,
            )
        assertEquals(listOf("y=10"), textLabels(plan.layout.plotDrawCommands))
    }

    @Test
    fun barChartEmitsPerBarLabels() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "g",
                            label = "G",
                            style = BarStyle(color = ChartColor.Green),
                            entries = listOf(BarEntry(1.0, 6.0), BarEntry(2.0, 12.0), BarEntry(3.0, 9.0)),
                        ),
                    ),
                dataLabels = DataLabelsSpec(enabled = true),
            )
        val plan = ChartPlanner.planBarChart(input, spec, selection = null)
        val labels = textLabels(plan.layout.plotDrawCommands)
        assertEquals(3, labels.size)
        assertTrue(labels.containsAll(listOf("6", "12", "9")), "got $labels")
    }
}
