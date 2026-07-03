package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ErrorBarsSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorBarTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun linePlan(
        points: List<ChartPoint>,
        errorBars: ErrorBarsSpec = ErrorBarsSpec(),
    ): LineChartPlan =
        ChartPlanner.planLineChart(
            input,
            LineChartSpec(
                series = listOf(LineSeries("s", "S", points, pointStyle = PointStyle(visible = false))),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
                errorBars = errorBars,
            ),
            selection = null,
        )

    private fun lineCount(p: LineChartPlan) = p.layout.plotDrawCommands.count { it is DrawCommand.Line }

    @Test
    fun bothBoundsEmitWhiskerPlusTwoCaps() {
        // grid disabled + invisible points => the only Lines are the error bar (1 whisker + 2 caps).
        val p = linePlan(listOf(ChartPoint(2.0, 10.0, yLow = 6.0, yHigh = 14.0)))
        assertEquals(3, lineCount(p))
    }

    @Test
    fun oneSidedBoundEmitsWhiskerPlusOneCap() {
        val p = linePlan(listOf(ChartPoint(2.0, 10.0, yHigh = 14.0)))
        assertEquals(2, lineCount(p))
    }

    @Test
    fun noBoundsEmitsNothing() {
        val p = linePlan(listOf(ChartPoint(1.0, 5.0), ChartPoint(3.0, 12.0)))
        assertEquals(0, lineCount(p))
    }

    @Test
    fun disabledSpecEmitsNothing() {
        val p =
            linePlan(
                listOf(ChartPoint(2.0, 10.0, yLow = 6.0, yHigh = 14.0)),
                errorBars = ErrorBarsSpec(enabled = false),
            )
        assertEquals(0, lineCount(p))
    }

    @Test
    fun verticalBarsEmitWhiskers() {
        val plan =
            ChartPlanner.planBarChart(
                input,
                BarChartSpec(
                    groups =
                        listOf(
                            BarGroup(
                                "g",
                                "G",
                                listOf(BarEntry(1.0, 10.0, yLow = 7.0, yHigh = 13.0)),
                            ),
                        ),
                    xAxis = AxisSpec(showGridLines = false),
                    yAxis = AxisSpec(showGridLines = false),
                ),
            )
        // 1 whisker + 2 caps; bars themselves are DrawCommand.Rect, not Line.
        assertEquals(3, plan.layout.plotDrawCommands.count { it is DrawCommand.Line })
    }
}
