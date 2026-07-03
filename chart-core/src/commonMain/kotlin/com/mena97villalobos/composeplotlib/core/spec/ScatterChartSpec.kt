package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries

/**
 * Immutable configuration for a scatter chart: one or more [ScatterSeries], axes, grid, legend, and formatting.
 *
 * Unlike [LineChartSpec], series are drawn as individual points only; no connecting segments are emitted.
 *
 * @property series Data sets to plot; empty lists are allowed and typically show a no-data UI in `chart-compose`.
 *   Series [ids][com.mena97villalobos.composeplotlib.core.model.ScatterSeries.id] must be unique (checked in `init`).
 * @property scatterRender Optional domain filtering + decimation when [ScatterRenderSpec.maxPointsPerSeries] is set
 * (or derived from plot width via [ScatterRenderSpec.usePlotWidthPixelBudget] in Compose). Default `0` draws every
 * point that intersects the plot by marker disc (including discs that cross the plot when the center is outside the
 * domain window).
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (vertical) axis appearance and tick behavior; see [AxisSpec].
 * @property valueFormatter Formats the selected point for markers and similar UI.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the current selection; see [SelectionStyle].
 * @property referenceLines Static threshold guide lines drawn over the plot; empty by default. See [ReferenceLine].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class ScatterChartSpec(
    val series: List<ScatterSeries>,
    val scatterRender: ScatterRenderSpec = ScatterRenderSpec(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val valueFormatter: LineValueFormatter = DefaultLineValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val referenceLines: List<ReferenceLine> = emptyList(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "ScatterChartSpec.series ids")
    }
}
