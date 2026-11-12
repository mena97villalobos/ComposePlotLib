package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec

fun sampleScatterChartSpec(): ScatterChartSpec =
    ScatterChartSpec(
        series =
            listOf(
                ScatterSeries(
                    id = "alpha",
                    label = "Alpha",
                    points =
                        listOf(
                            ChartPoint(1.2, 2.8),
                            ChartPoint(2.5, 5.1),
                            ChartPoint(4.0, 3.4),
                            ChartPoint(5.5, 7.2),
                            ChartPoint(7.0, 5.9),
                        ),
                    pointStyle = PointStyle(visible = true, radius = 5.0, color = ChartColor.Blue),
                ),
                ScatterSeries(
                    id = "beta",
                    label = "Beta",
                    points =
                        listOf(
                            ChartPoint(1.8, 6.5),
                            ChartPoint(3.2, 4.2),
                            ChartPoint(5.0, 8.1),
                            ChartPoint(6.5, 3.7),
                            ChartPoint(8.5, 6.2),
                        ),
                    pointStyle = PointStyle(visible = true, radius = 4.5, color = ChartColor.Green),
                ),
            ),
        legend = LegendSpec(enabled = true),
        grid = GridSpec(),
    )

fun emptyScatterChartSpec(): ScatterChartSpec =
    ScatterChartSpec(
        series =
            listOf(
                ScatterSeries(id = "empty", label = "No points", points = emptyList()),
            ),
    )
