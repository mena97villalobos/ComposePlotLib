package com.mena97villalobos.composeplotlib.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

/**
 * Minimal, axis-less, non-interactive line chart for inline trends (e.g. a table cell, list row, or KPI card).
 *
 * A thin convenience wrapper over [LineChart]: no axes, grid, legend, content padding, vertex markers, or gestures
 * — just the line, optionally with an area fill. The viewport fits the data tightly. For anything richer (axes,
 * selection, reference lines, pan/zoom), build a [LineChartSpec] and use [LineChart] directly.
 *
 * @param points the series in domain space (x is typically an index or timestamp, y the value).
 * @param modifier layout modifier for the chart.
 * @param color line color.
 * @param strokeWidth line width in pixels.
 * @param areaFill optional fill under the line; `null` = stroke only.
 * @param areaFillEnd optional second color to fade [areaFill] into a vertical gradient (ignored when [areaFill] is null).
 */
@Composable
fun Sparkline(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    color: ChartColor = ChartColor.Blue,
    strokeWidth: Double = 2.0,
    areaFill: ChartColor? = null,
    areaFillEnd: ChartColor? = null,
) {
    val spec =
        remember(points, color, strokeWidth, areaFill, areaFillEnd) {
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "sparkline",
                            label = "",
                            points = points,
                            lineStyle =
                                LineStyle(
                                    color = color,
                                    strokeWidth = strokeWidth,
                                    areaFill = areaFill,
                                    areaFillEnd = areaFillEnd,
                                ),
                            pointStyle = PointStyle(visible = false),
                        ),
                    ),
                xAxis = SparklineAxis,
                yAxis = SparklineAxis,
                grid = GridSpec(showVerticalLines = false, showHorizontalLines = false),
                legend = LegendSpec(enabled = false),
                contentPadding = InsetsD.Zero,
            )
        }
    LineChart(
        spec = spec,
        modifier = modifier,
        gestureConfig = ChartGestureConfig.readOnly(),
    )
}

/** Axis with all chrome (ticks, labels, line, grid) suppressed, used for both sparkline axes. */
private val SparklineAxis = AxisSpec(enabled = false, showGridLines = false)
