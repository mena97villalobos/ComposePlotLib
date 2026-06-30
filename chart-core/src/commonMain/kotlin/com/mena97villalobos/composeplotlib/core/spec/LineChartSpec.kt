package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.LineSeries

/**
 * Immutable configuration for a line chart: one or more [LineSeries], axes, grid, legend, and formatting.
 *
 * Prefer creating new instances when configuration changes; the Compose layer replans when `spec` changes.
 *
 * @property series Data sets to plot; empty lists are allowed and typically show a no-data UI in `chart-compose`.
 * @property accessibility Host-agnostic strings for accessibility (mapped to semantics in Compose).
 * @property valueFormatter Formats the selected point for markers and similar UI.
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class LineChartSpec(
    val series: List<LineSeries>,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val valueFormatter: LineValueFormatter = DefaultLineValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val lineRender: LineRenderSpec = LineRenderSpec(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "LineChartSpec.series ids")
    }
}
