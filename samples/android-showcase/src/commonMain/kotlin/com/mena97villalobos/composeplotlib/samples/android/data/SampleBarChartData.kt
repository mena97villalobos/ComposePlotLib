package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.ChartAccessibilitySpec
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle

fun sampleBarChartSpec(): BarChartSpec =
    BarChartSpec(
        groups =
            listOf(
                BarGroup(
                    id = "sales",
                    label = "Sales",
                    style = BarStyle(color = ChartColor.Green, barWidth = 18.0),
                    entries =
                        listOf(
                            BarEntry(0.0, 10.0),
                            BarEntry(1.0, 14.0),
                            BarEntry(2.0, 12.0),
                            BarEntry(3.0, 18.0),
                            BarEntry(4.0, 16.0),
                        ),
                ),
                BarGroup(
                    id = "returns",
                    label = "Returns",
                    style = BarStyle(color = ChartColor.Red, barWidth = 14.0),
                    entries =
                        listOf(
                            BarEntry(0.5, 4.0),
                            BarEntry(1.5, 5.0),
                            BarEntry(2.5, 3.0),
                            BarEntry(3.5, 6.0),
                            BarEntry(4.5, 5.0),
                        ),
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Showcase bar chart: Sales and Returns by quarter.",
                requestFocusForAccessibility = true,
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> barQuarterLabel(value) },
                labelStyle = AxisLabelStyle(color = ChartColor.Red),
            ),
        yAxis =
            AxisSpec(
                labelFormatter = { value -> "\$${value.toInt()}k" },
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
        valueFormatter = { entry ->
            "${barQuarterLabel(entry.x)} revenue: \$${entry.y.toInt()}k"
        },
    )

fun emptyBarChartSpec(): BarChartSpec =
    BarChartSpec(
        groups = emptyList(),
        accessibility =
            ChartAccessibilitySpec(
                contentDescription = "Showcase empty bar chart for no-data state.",
            ),
        xAxis =
            AxisSpec(
                labelFormatter = { value -> barQuarterLabel(value) },
            ),
    )

private fun barQuarterLabel(value: Double): String =
    when (value.toInt()) {
        0 -> "Q1"
        1 -> "Q2"
        2 -> "Q3"
        3 -> "Q4"
        4 -> "Q5"
        else -> value.toInt().toString()
    }
