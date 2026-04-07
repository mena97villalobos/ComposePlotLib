package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.EpochMillisTickPolicy
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

private val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private val longCategoryLabels =
    listOf(
        "North America",
        "Europe, Middle East, Africa",
        "Asia Pacific region",
        "Latin America",
    )

/** Line chart with [AxisScale.Category] on X (indices `0..weekdayLabels.lastIndex`). */
fun sampleCategoryLineChartSpec(): LineChartSpec {
    val xs = weekdayLabels.indices.map { it.toDouble() }
    val ys = listOf(4.0, 7.0, 5.0, 9.0, 11.0, 6.0, 8.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "sessions",
                    label = "Sessions",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points = xs.zip(ys) { x, y -> ChartPoint(x, y) },
                ),
            ),
        xAxis =
            AxisSpec(
                scale = AxisScale.Category(weekdayLabels),
                tickCount = weekdayLabels.size,
            ),
        yAxis = AxisSpec(tickCount = 5),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

/** Category X with long labels and [AxisSpec.categoryLabelMaxChars] truncation in tick labels. */
fun sampleLongCategoryLineChartSpec(): LineChartSpec {
    val ys = listOf(12.0, 18.0, 22.0, 15.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "regions",
                    label = "Regions",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points = longCategoryLabels.indices.map { i -> ChartPoint(i.toDouble(), ys[i]) },
                ),
            ),
        xAxis =
            AxisSpec(
                scale = AxisScale.Category(longCategoryLabels),
                tickCount = longCategoryLabels.size,
                categoryLabelMaxChars = 14,
            ),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

/**
 * Line chart with X as Unix epoch milliseconds ([AxisScale.TimeEpochMillis]); ~3 days of 6-hour samples.
 * Labels use the default UTC compact formatter from the planner when [AxisSpec.labelFormatter] is default.
 */
fun sampleEpochMillisLineChartSpec(): LineChartSpec {
    val origin = 1_700_000_000_000.0
    val step = 86_400_000.0 / 4.0 // 6 hours
    val ys = listOf(2.0, 3.0, 4.0, 3.0, 5.0, 6.0, 5.0, 8.0, 7.0, 9.0, 8.0, 10.0, 11.0)
    val points =
        ys.mapIndexed { i, y ->
            ChartPoint(origin + i * step, y)
        }
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "dwell",
                    label = "Dwell (min)",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points = points,
                ),
            ),
        xAxis = AxisSpec(scale = AxisScale.TimeEpochMillis, tickCount = 6),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

/**
 * Two weeks of daily samples with [EpochMillisTickPolicy.WeekdaysUtc] on X (UTC Sat/Sun ticks omitted; not holiday-aware).
 */
fun sampleWeekdayEpochMillisLineChartSpec(): LineChartSpec {
    val origin = 1_702_800_000_000.0
    val day = 86_400_000.0
    val ys = listOf(4.0, 5.0, 6.0, 5.5, 7.0, 6.0, 8.0, 7.5, 9.0, 8.5, 7.0, 8.0, 9.0, 8.0)
    val points = ys.mapIndexed { i, y -> ChartPoint(origin + i * day, y) }
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "daily",
                    label = "Daily (weekday ticks)",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points = points,
                ),
            ),
        xAxis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                tickCount = 8,
                epochMillisTickPolicy = EpochMillisTickPolicy.WeekdaysUtc,
            ),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}
