package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarGroup

/**
 * Immutable configuration for a **stacked** bar chart. Same data model as [BarChartSpec], but layout is always
 * [BarLayoutMode.Stacked] (falling back to [BarLayoutMode.Independent] when group entry counts differ —
 * see [resolvedBarLayout] / [BarChartSpec.resolvedBarLayout]).
 *
 * Use [toBarChartSpec] with the `BarChart` composable (in `chart-compose`) or the `StackedBarChart` composable.
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

    fun resolvedBarLayout(): BarLayoutMode = toBarChartSpec().resolvedBarLayout()

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
