package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.TrendlineSpec
import com.mena97villalobos.composeplotlib.core.spec.TrendlineType
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrendlineTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private val trendColor = ChartColor.Green

    private fun plan(
        trendlines: List<TrendlineSpec>,
        points: List<ChartPoint> =
            listOf(
                ChartPoint(0.0, 2.0),
                ChartPoint(1.0, 5.0),
                ChartPoint(2.0, 8.0),
                ChartPoint(3.0, 11.0),
                ChartPoint(4.0, 14.0),
            ),
    ): LineChartPlan =
        ChartPlanner.planLineChart(
            input,
            LineChartSpec(
                series = listOf(LineSeries("s", "S", points, pointStyle = PointStyle(visible = false))),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
                trendlines = trendlines,
            ),
            selection = null,
        )

    private fun trendPolylines(p: LineChartPlan): List<DrawCommand.Polyline> =
        p.layout.plotDrawCommands
            .filterIsInstance<DrawCommand.Polyline>()
            .filter { it.color == trendColor }

    @Test
    fun noTrendlineByDefault() {
        assertTrue(trendPolylines(plan(emptyList())).isEmpty())
    }

    @Test
    fun linearTrendlineEmitsAscendingPolyline() {
        val lines = trendPolylines(plan(listOf(TrendlineSpec(type = TrendlineType.Linear, color = trendColor))))
        assertEquals(1, lines.size)
        val pts = lines.first().points
        assertEquals(2, pts.size)
        // Positive data slope => trendline rises left-to-right, i.e. pixel-y decreases as x increases.
        assertTrue(pts.first().x < pts.last().x)
        assertTrue(pts.first().y > pts.last().y)
    }

    @Test
    fun movingAverageEmitsPolyline() {
        val lines =
            trendPolylines(plan(listOf(TrendlineSpec(type = TrendlineType.MovingAverage(2), color = trendColor))))
        assertEquals(1, lines.size)
        // 5 points, window 2 => 4 averaged points.
        assertEquals(4, lines.first().points.size)
    }

    @Test
    fun confidenceBandEmitsFilledPath() {
        val p =
            plan(
                listOf(
                    TrendlineSpec(
                        type = TrendlineType.Linear,
                        color = trendColor,
                        confidenceBand = true,
                        confidenceLevel = 0.95,
                    ),
                ),
            )
        assertTrue(p.layout.plotDrawCommands.any { it is DrawCommand.Path })
    }

    @Test
    fun confidenceBandSkippedForTwoPoints() {
        // n < 3 => standard error is undefined, so no band Path is emitted (line still is).
        val p =
            plan(
                trendlines =
                    listOf(TrendlineSpec(type = TrendlineType.Linear, color = trendColor, confidenceBand = true)),
                points = listOf(ChartPoint(0.0, 2.0), ChartPoint(4.0, 14.0)),
            )
        assertTrue(p.layout.plotDrawCommands.none { it is DrawCommand.Path })
        assertEquals(1, trendPolylines(p).size)
    }
}
