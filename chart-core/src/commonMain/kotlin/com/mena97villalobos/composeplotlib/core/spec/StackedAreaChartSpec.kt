package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.StackedAreaSeries

/** Baseline offset that determines how stacked areas are positioned (d3-style stack offsets). */
enum class AreaStackOffset {
    /** Baseline at zero; bands stack upward at their raw magnitudes (default). */
    Zero,

    /** **100%**: each x is normalized so bands fill `[0, 1]` and encode composition (share of total). */
    Expand,

    /** **Streamgraph**: the stack is centered around a wiggling baseline (`-total/2`), a symmetric silhouette. */
    Silhouette,
}

/**
 * Immutable configuration for a **stacked-area** chart: index-aligned [series] bands over shared [xValues],
 * accumulated per the [offset] (absolute / 100% / streamgraph). Cartesian with axes, grid, and legend; pan/zoom
 * supported (no per-point selection). `chart-compose`'s `StackedAreaChart` renders it.
 *
 * @property xValues Shared x positions (ascending); each series' `values` aligns to these by index.
 * @property series Bands stacked in list order (first at the base). Series ids must be unique (checked in `init`).
 * @property offset Baseline / normalization mode; see [AreaStackOffset].
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y axis appearance and tick behavior; see [AxisSpec].
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property contentPadding Minimum padding around chart content in **pixels**.
 */
data class StackedAreaChartSpec(
    val xValues: List<Double>,
    val series: List<StackedAreaSeries>,
    val offset: AreaStackOffset = AreaStackOffset.Zero,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "StackedAreaChartSpec.series ids")
    }
}
