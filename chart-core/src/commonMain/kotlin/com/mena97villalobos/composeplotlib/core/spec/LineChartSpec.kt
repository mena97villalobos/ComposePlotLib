package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.LineSeries

/**
 * Immutable configuration for a line chart: one or more [LineSeries], axes, grid, legend, and formatting.
 *
 * Prefer creating new instances when configuration changes; the Compose layer replans when `spec` changes.
 * Series [ids][com.mena97villalobos.composeplotlib.core.model.LineSeries.id] must be unique (checked in `init`).
 *
 * @property series Data sets to plot; empty lists are allowed and typically show a no-data UI in `chart-compose`.
 * @property accessibility Host-agnostic strings for accessibility (mapped to semantics in Compose); see [ChartAccessibilitySpec].
 * @property xAxis X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (vertical) axis appearance and tick behavior; see [AxisSpec].
 * @property valueFormatter Formats the selected point for markers and similar UI.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the current selection; see [SelectionStyle].
 * @property lineRender Viewport windowing and optional decimation for dense series; see [LineRenderSpec].
 * @property referenceLines Static threshold guide lines drawn over the plot; empty by default. See [ReferenceLine].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 * @property dataLabels Optional per-point value labels; disabled by default. See [DataLabelsSpec].
 * @property hiddenSeriesIds Ids of series to hide (drawn dimmed in the legend); enables interactive/tap-to-toggle
 * legends. Hidden series are excluded from drawing but still occupy a legend row. Empty by default.
 * @property annotations Freeform data-anchored marks (points, text, boxes, segments) drawn over the plot; empty by
 * default. See [ChartAnnotation].
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
    val referenceLines: List<ReferenceLine> = emptyList(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
    val dataLabels: DataLabelsSpec = DataLabelsSpec(),
    val hiddenSeriesIds: Set<String> = emptySet(),
    val annotations: List<ChartAnnotation> = emptyList(),
) {
    init {
        requireUniqueIds(series.map { it.id }, "LineChartSpec.series ids")
    }
}
