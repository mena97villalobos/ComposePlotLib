package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.HeatmapCell
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.HeatmapColorScale

fun sampleHeatmapChartSpec(): HeatmapChartSpec =
    HeatmapChartSpec(
        id = "activity",
        columnLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        rowLabels = listOf("Week 1", "Week 2", "Week 3", "Week 4"),
        cells =
            listOf(
                // Week 1
                HeatmapCell(0, 0, 20.0),
                HeatmapCell(1, 0, 45.0),
                HeatmapCell(2, 0, 80.0),
                HeatmapCell(3, 0, 55.0),
                HeatmapCell(4, 0, 70.0),
                HeatmapCell(5, 0, 10.0),
                HeatmapCell(6, 0, 5.0),
                // Week 2
                HeatmapCell(0, 1, 60.0),
                HeatmapCell(1, 1, 75.0),
                HeatmapCell(2, 1, 90.0),
                HeatmapCell(3, 1, 85.0),
                HeatmapCell(4, 1, 50.0),
                HeatmapCell(5, 1, 30.0),
                HeatmapCell(6, 1, 15.0),
                // Week 3
                HeatmapCell(0, 2, 40.0),
                HeatmapCell(1, 2, 65.0),
                HeatmapCell(2, 2, 100.0),
                HeatmapCell(3, 2, 95.0),
                HeatmapCell(4, 2, 80.0),
                HeatmapCell(5, 2, 25.0),
                HeatmapCell(6, 2, 20.0),
                // Week 4
                HeatmapCell(0, 3, 35.0),
                HeatmapCell(1, 3, 55.0),
                HeatmapCell(2, 3, 70.0),
                HeatmapCell(3, 3, 60.0),
                HeatmapCell(4, 3, 45.0),
                HeatmapCell(5, 3, 40.0),
                HeatmapCell(6, 3, 30.0),
            ),
        colorScale = HeatmapColorScale(0.0, 100.0, ChartColor.LightGray, ChartColor.Blue),
    )

fun emptyHeatmapChartSpec(): HeatmapChartSpec =
    HeatmapChartSpec(
        columnLabels = listOf("A"),
        rowLabels = listOf("1"),
        cells = emptyList(),
        colorScale = HeatmapColorScale(),
    )
