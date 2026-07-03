package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BoxPlotSeries

/**
 * Immutable configuration for a box-and-whisker (boxplot) chart: [BoxPlotSeries] list over a numeric/category X
 * axis and a value Y axis.
 *
 * @property series Logical series (each with [BoxPlotSeries.entries]); may be empty for no-data handling upstream.
 *   Series [ids][BoxPlotSeries.id] must be unique (checked in `init`).
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (category) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (value) axis appearance and tick behavior; see [AxisSpec].
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Highlight styling for the selected box; see [SelectionStyle].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class BoxPlotChartSpec(
    val series: List<BoxPlotSeries>,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "BoxPlotChartSpec.series ids")
    }
}
