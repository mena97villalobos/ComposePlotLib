package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.HeatmapCell

/**
 * Specification for a heatmap chart: a 2-D grid of colored cells where each cell's color encodes
 * a numeric [HeatmapCell.value] via a [HeatmapColorScale].
 *
 * Column labels map to the X axis; row labels map to the Y axis (row 0 at the bottom).
 * Both axes are always rendered as [AxisScale.Category] — the [xAxis] and [yAxis] fields control
 * styling (label color, grid lines, line visibility) but their [AxisSpec.scale] is ignored by the
 * planner, which always uses category indices derived from [columnLabels] and [rowLabels].
 *
 * @param cells Data points; out-of-range indices are silently skipped.
 * @param columnLabels Labels for each column, length determines the column count.
 * @param rowLabels Labels for each row, length determines the row count.
 * @param id Chart identity string used for [com.mena97villalobos.composeplotlib.core.selection.ChartSelection] matching.
 * @param colorScale Value-to-color mapping.
 * @param xAxis Styling for the X (column) axis. Scale is overridden to [AxisScale.Category].
 * @param yAxis Styling for the Y (row) axis. Scale is overridden to [AxisScale.Category].
 * @param cellGapPx Visual gap in pixels between adjacent cells (split equally on each edge).
 * @param legend Legend configuration (disabled by default — heatmaps carry semantics via color scale, not series labels).
 * @param selectionStyle Highlight style for the selected cell border.
 * @param contentPadding Space reserved around the plot area for axis labels.
 * @param valueFormatter Formats a cell value for the selection marker / tooltip.
 * @param accessibility Accessibility configuration.
 */
data class HeatmapChartSpec(
    val cells: List<HeatmapCell>,
    val columnLabels: List<String>,
    val rowLabels: List<String>,
    val id: String = "heatmap",
    val colorScale: HeatmapColorScale = HeatmapColorScale(),
    val xAxis: AxisSpec = AxisSpec(showGridLines = false),
    val yAxis: AxisSpec = AxisSpec(showGridLines = false),
    val cellGapPx: Double = 2.0,
    val legend: LegendSpec = LegendSpec(enabled = false),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 24.0, right = 24.0, bottom = 40.0),
    val valueFormatter: (HeatmapCell) -> String = { "%.1f".format(it.value) },
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
)
