package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarGroup

/**
 * Immutable configuration for a grouped bar chart: [BarGroup] list, axes, grid, legend, and formatting.
 *
 * @property groups Logical series (each with [BarGroup.entries]); may be empty for no-data handling upstream.
 *   Group [ids][BarGroup.id] must be unique (checked in `init`).
 * @property orientation Whether bars are drawn as vertical columns or horizontal rows (see [BarChartOrientation]).
 * @property barLayout Requested category-sharing layout; may fall back to [BarLayoutMode.Independent] — see [resolvedBarLayout].
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (vertical) axis appearance and tick behavior; see [AxisSpec].
 * @property valueFormatter Formats the selected [com.mena97villalobos.composeplotlib.core.model.BarEntry] for markers.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the current selection; see [SelectionStyle].
 * @property referenceLines Static threshold guide lines drawn over the plot; empty by default. See [ReferenceLine].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 * @property dataLabels Optional per-bar value labels; disabled by default. See [DataLabelsSpec].
 * @property hiddenSeriesIds Ids of [BarGroup]s to hide (drawn dimmed in the legend); enables interactive/tap-to-toggle
 * legends. Hidden groups are excluded from drawing but still occupy a legend row. Empty by default.
 * @property annotations Freeform data-anchored marks (points, text, boxes, segments) drawn over the plot; empty by
 * default. See [ChartAnnotation].
 */
data class BarChartSpec(
    val groups: List<BarGroup>,
    val orientation: BarChartOrientation = BarChartOrientation.Vertical,
    val barLayout: BarLayoutMode = BarLayoutMode.Independent,
    /**
     * Horizontal gap in **pixels** between bars inside one clustered category (see [BarLayoutMode.Clustered]).
     */
    val clusterBarGapPx: Double = 4.0,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val valueFormatter: BarValueFormatter = DefaultBarValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val referenceLines: List<ReferenceLine> = emptyList(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
    val dataLabels: DataLabelsSpec = DataLabelsSpec(),
    val hiddenSeriesIds: Set<String> = emptySet(),
    val annotations: List<ChartAnnotation> = emptyList(),
) {
    init {
        requireUniqueIds(groups.map { it.id }, "BarChartSpec.groups ids")
    }

    /**
     * Layout mode actually used by [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner]:
     * [BarLayoutMode.Clustered] / [BarLayoutMode.Stacked] fall back to [BarLayoutMode.Independent]
     * when group entry counts differ.
     */
    fun resolvedBarLayout(): BarLayoutMode {
        if (barLayout == BarLayoutMode.Independent || groups.isEmpty()) return barLayout
        val n = groups.first().entries.size
        return if (groups.all { it.entries.size == n }) barLayout else BarLayoutMode.Independent
    }
}
