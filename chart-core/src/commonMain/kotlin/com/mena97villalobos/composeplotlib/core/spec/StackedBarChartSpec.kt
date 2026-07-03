package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarGroup

/**
 * Immutable configuration for a **stacked** bar chart. Same data model as [BarChartSpec], but layout is always
 * [BarLayoutMode.Stacked] (falling back to [BarLayoutMode.Independent] when group entry counts differ —
 * see [resolvedBarLayout] / [BarChartSpec.resolvedBarLayout]).
 *
 * Use [toBarChartSpec] with the `BarChart` composable (in `chart-compose`) or the `StackedBarChart` composable.
 *
 * @property groups Logical series stacked at matching category indices (first group at the base); may be empty.
 *   Group [ids][BarGroup.id] must be unique (checked in `init`).
 * @property orientation Whether bars stack as vertical columns or horizontal rows; see [BarChartOrientation].
 * @property clusterBarGapPx Pixel gap carried through to the equivalent [BarChartSpec]; unused while stacked.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (vertical) axis appearance and tick behavior; see [AxisSpec].
 * @property valueFormatter Formats the selected [com.mena97villalobos.composeplotlib.core.model.BarEntry] for markers.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the current selection; see [SelectionStyle].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class StackedBarChartSpec(
    val groups: List<BarGroup>,
    val orientation: BarChartOrientation = BarChartOrientation.Vertical,
    val clusterBarGapPx: Double = 4.0,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val valueFormatter: BarValueFormatter = DefaultBarValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(groups.map { it.id }, "StackedBarChartSpec.groups ids")
    }

    /**
     * Layout actually used by the planner: [BarLayoutMode.Stacked] unless group entry counts differ, in which
     * case it falls back to [BarLayoutMode.Independent] (delegates to [BarChartSpec.resolvedBarLayout]).
     */
    fun resolvedBarLayout(): BarLayoutMode = toBarChartSpec().resolvedBarLayout()

    /** Converts this spec to an equivalent [BarChartSpec] with [BarLayoutMode.Stacked], preserving all styling. */
    fun toBarChartSpec(): BarChartSpec =
        BarChartSpec(
            groups = groups,
            orientation = orientation,
            barLayout = BarLayoutMode.Stacked,
            clusterBarGapPx = clusterBarGapPx,
            accessibility = accessibility,
            xAxis = xAxis,
            yAxis = yAxis,
            valueFormatter = valueFormatter,
            grid = grid,
            legend = legend,
            selectionStyle = selectionStyle,
            contentPadding = contentPadding,
        )
}
