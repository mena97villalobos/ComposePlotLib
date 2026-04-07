package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries

/**
 * Immutable configuration for a scatter chart: one or more [ScatterSeries], axes, grid, legend, and formatting.
 *
 * Unlike [LineChartSpec], series are drawn as individual points only; no connecting segments are emitted.
 *
 * @property series Data sets to plot; empty lists are allowed and typically show a no-data UI in `chart-compose`.
 * @property scatterRender Optional domain filtering + decimation when [ScatterRenderSpec.maxPointsPerSeries] is set
 * (or derived from plot width via [ScatterRenderSpec.usePlotWidthPixelBudget] in Compose). Default `0` draws every
 * point that intersects the plot by marker disc (including discs that cross the plot when the center is outside the
 * domain window).
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
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
)
