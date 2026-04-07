package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisLineStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.AxisTickDensity
import com.mena97villalobos.composeplotlib.core.spec.ChartAccessibilitySpec
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle
import com.mena97villalobos.composeplotlib.core.spec.XTickLabelOverlapStrategy
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.round

fun sampleLineChartSpec(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "downloads",
                    label = "Downloads",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points =
                        listOf(
                            ChartPoint(0.0, 8.0),
                            ChartPoint(1.0, 12.0),
                            ChartPoint(2.0, 11.0),
                            ChartPoint(3.0, 18.0),
                            ChartPoint(4.0, 16.0),
                            ChartPoint(5.0, 22.0),
                            ChartPoint(6.0, 24.0),
                        ),
                ),
                LineSeries(
                    id = "retention",
                    label = "Retention",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points =
                        listOf(
                            ChartPoint(0.0, 6.0),
                            ChartPoint(1.0, 9.0),
                            ChartPoint(2.0, 13.0),
                            ChartPoint(3.0, 12.0),
                            ChartPoint(4.0, 15.0),
                            ChartPoint(5.0, 17.0),
                            ChartPoint(6.0, 20.0),
                        ),
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Showcase line chart: Downloads and Retention by month.",
                requestFocusForAccessibility = true,
                keyboardViewportNudgeEnabled = true,
                keyboardSelectionRoamEnabled = true,
                requestFocusOnMarker = true,
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> lineMonthLabel(value) },
                labelStyle = AxisLabelStyle(color = ChartColor.Blue),
                lineStyle = AxisLineStyle(color = ChartColor.LightGray),
            ),
        yAxis =
            AxisSpec(
                labelFormatter = { value -> "${value.toInt()}k" },
                labelStyle = AxisLabelStyle(color = ChartColor.Green),
            ),
        grid =
            GridSpec(
                lineStyle = GridLineStyle(color = ChartColor.LightGray),
            ),
        legend =
            LegendSpec(
                style =
                    LegendStyle(
                        textColor = ChartColor.Gray,
                        indicatorRadius = 5.0,
                    ),
            ),
        selectionStyle =
            SelectionStyle(
                lineColor = ChartColor.Red,
                lineStrokeWidth = 2.0,
                pointColor = ChartColor.Red,
                pointRadius = 6.0,
            ),
        valueFormatter = { point ->
            "${lineMonthLabel(point.x)}: ${point.y.toInt()}k active users"
        },
    )

fun emptyLineChartSpec(): LineChartSpec =
    LineChartSpec(
        series = emptyList(),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Showcase empty line chart for no-data state.",
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> lineMonthLabel(value) },
            ),
    )

/** Single series with Y values near the top of the default viewport — demo tooltip flips below the anchor. */
fun lineChartSpecTooltipEdgeDemo(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "peak",
                    label = "High Y",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 5.0),
                    points =
                        (0..6).map { i ->
                            ChartPoint(i.toDouble(), 92.0 + (i % 3))
                        },
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Demo line chart for marker placement; Y values cluster near the top.",
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> lineMonthLabel(value) },
            ),
        yAxis =
            AxisSpec(
                labelFormatter = { value -> "${value.toInt()}" },
            ),
        grid =
            GridSpec(
                lineStyle = GridLineStyle(color = ChartColor.LightGray),
            ),
        legend = LegendSpec(),
        selectionStyle =
            SelectionStyle(
                lineColor = ChartColor.Red,
                lineStrokeWidth = 2.0,
                pointColor = ChartColor.Red,
                pointRadius = 6.0,
            ),
        valueFormatter = { point -> "Y≈${point.y.toInt()} at ${lineMonthLabel(point.x)}" },
    )

/** Compact series for custom callout + focusable marker QA (M9). */
fun lineChartSpecCustomCalloutFocusDemo(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "demo",
                    label = "Demo",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 5.0),
                    points =
                        listOf(
                            ChartPoint(0.0, 10.0),
                            ChartPoint(2.0, 22.0),
                            ChartPoint(4.0, 18.0),
                            ChartPoint(6.0, 30.0),
                        ),
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Demo chart: custom callout path and focusable marker content.",
                requestFocusOnMarker = true,
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> lineMonthLabel(value) },
            ),
        yAxis =
            AxisSpec(
                labelFormatter = { value -> "${value.toInt()}" },
            ),
        grid =
            GridSpec(
                lineStyle = GridLineStyle(color = ChartColor.LightGray),
            ),
        legend = LegendSpec(),
        selectionStyle =
            SelectionStyle(
                lineColor = ChartColor.Red,
                lineStrokeWidth = 2.0,
                pointColor = ChartColor.Red,
                pointRadius = 6.0,
            ),
        valueFormatter = { point -> "${lineMonthLabel(point.x)} → ${point.y.toInt()}" },
    )

private const val LARGE_DENSE_LINE_POINT_COUNT = 35_000

/**
 * One synthetic series with tens of thousands of points for performance validation.
 * Uses [LineRenderSpec.usePlotWidthPixelBudget] and [LineDecimationStrategy.MinMaxY] so planning stays bounded.
 */
fun sampleLargeDenseLineChartSpec(): LineChartSpec {
    val pts =
        buildList(capacity = LARGE_DENSE_LINE_POINT_COUNT) {
            for (i in 0 until LARGE_DENSE_LINE_POINT_COUNT) {
                val x = i.toDouble()
                val spike = if (i == LARGE_DENSE_LINE_POINT_COUNT / 2) 45.0 else 0.0
                val y = kotlin.math.sin(i * 0.02) * 18.0 + spike + i * 0.0001
                add(ChartPoint(x, y))
            }
        }
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "dense",
                    label = "Dense series",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(visible = false),
                    points = pts,
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription =
                    "Showcase line chart with a large dense series; vertices are capped from plot width with MinMaxY decimation.",
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { v -> "${v.toLong()}" },
            ),
        yAxis =
            AxisSpec(
                labelFormatter = { v -> (round(v * 10.0) / 10.0).toString() },
            ),
        grid =
            GridSpec(
                lineStyle = GridLineStyle(color = ChartColor.LightGray),
            ),
        legend = LegendSpec(),
        lineRender =
            LineRenderSpec(
                usePlotWidthPixelBudget = true,
                decimation = LineDecimationStrategy.MinMaxY,
            ),
        valueFormatter = { p ->
            val yr = round(p.y * 100.0) / 100.0
            "x≈${p.x.toInt()}, y≈$yr"
        },
    )
}

/** Static viewport matching [sampleLargeDenseLineChartSpec] so the showcase avoids scanning all points for [defaultViewport]. */
fun largeDenseLineChartInitialViewport(): ChartViewport =
    ChartViewport(
        xRange = ClosedRangeD(0.0, (LARGE_DENSE_LINE_POINT_COUNT - 1).toDouble()),
        yRange = ClosedRangeD(-26.0, 72.0),
    )

/**
 * Dense X tick labels with [XTickLabelOverlapStrategy.StaggerAdjacent] and truncated legend text — layout/M5 stress demo.
 */
fun layoutStressLineChartSpec(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "rev",
                    label = "Quarterly revenue North corridor",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points = (0..7).map { i -> ChartPoint(i.toDouble(), 5.0 + i * 1.2) },
                ),
                LineSeries(
                    id = "cost",
                    label = "Operational spend South / LATAM",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points = (0..7).map { i -> ChartPoint(i.toDouble(), 4.0 + i * 0.9) },
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Layout stress: staggered x tick labels and ellipsized legend entries.",
            ),
        xAxis =
            AxisSpec(
                tickCount = 8,
                tickDensity = AxisTickDensity.MaxTicks(8),
                xTickLabelOverlapStrategy = XTickLabelOverlapStrategy.StaggerAdjacent,
                labelFormatter = { v -> "L${v.toInt()}" },
                showGridLines = false,
            ),
        yAxis = AxisSpec(tickCount = 4, showGridLines = false),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend =
            LegendSpec(
                style = LegendStyle(textColor = ChartColor.Gray, indicatorRadius = 5.0, labelMaxChars = 14),
            ),
        selectionStyle =
            SelectionStyle(
                lineColor = ChartColor.Red,
                lineStrokeWidth = 2.0,
                pointColor = ChartColor.Red,
                pointRadius = 6.0,
            ),
        valueFormatter = { p -> "x=${p.x.toInt()}, y=${p.y.format1()}" },
    )

private fun Double.format1(): String {
    val r = round(this * 10.0) / 10.0
    return r.toString()
}

private fun lineMonthLabel(value: Double): String =
    when (value.toInt()) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        else -> value.toInt().toString()
    }
