package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries

/**
 * Which Y scale a [CombinedCartesianLayer] maps through when [CombinedCartesianChartSpec.yAxisSecondary] is set.
 * Layers must use [CombinedCartesianYAxisBinding.SharedY] when no secondary axis is configured.
 */
enum class CombinedCartesianYAxisBinding {
    /** Layer maps through the primary (shared) Y scale — the only valid binding when [CombinedCartesianChartSpec.yAxisSecondary] is null. */
    SharedY,

    /** Layer maps through the secondary Y scale; requires [CombinedCartesianChartSpec.yAxisSecondary] to be non-null. */
    SecondaryY,
}

/**
 * One drawable layer in a [CombinedCartesianChartSpec], in z-order (earlier draws beneath later).
 */
sealed class CombinedCartesianLayer {
    /** Stable identifier for the layer; must be unique within a [CombinedCartesianChartSpec] (checked in its `init`). */
    abstract val id: String

    /** Which Y scale this layer maps through; see [CombinedCartesianYAxisBinding]. */
    abstract val yAxisBinding: CombinedCartesianYAxisBinding

    /**
     * A line layer wrapping one or more [LineSeries].
     *
     * @property series Polylines drawn in this layer.
     */
    data class Line(
        override val id: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val series: List<LineSeries>,
    ) : CombinedCartesianLayer()

    /**
     * A bar layer wrapping one or more [BarGroup]s.
     *
     * @property groups Bar groups drawn in this layer.
     * @property orientation Column vs row bars; see [BarChartOrientation].
     * @property barLayout Category-sharing layout; see [BarLayoutMode].
     * @property clusterBarGapPx Pixel gap between bars within a clustered category; defaults to `4.0`.
     */
    data class Bar(
        override val id: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val groups: List<BarGroup>,
        val orientation: BarChartOrientation = BarChartOrientation.Vertical,
        val barLayout: BarLayoutMode = BarLayoutMode.Independent,
        val clusterBarGapPx: Double = 4.0,
    ) : CombinedCartesianLayer()

    /**
     * A scatter layer wrapping one or more [ScatterSeries].
     *
     * @property series Point series drawn in this layer.
     */
    data class Scatter(
        override val id: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val series: List<ScatterSeries>,
    ) : CombinedCartesianLayer()
}

/**
 * Multi-layer cartesian chart: overlays line, bar, and/or scatter layers with shared X and one or two Y scales.
 *
 * When any layer uses [CombinedCartesianYAxisBinding.SecondaryY], [yAxisSecondary] must be non-null.
 * Secondary Y domain is **data-derived** from secondary-bound layers (not independently panned/zoomed in v1): on each
 * planning pass the planner recomputes min/max **Y** from all points in layers bound to
 * [CombinedCartesianYAxisBinding.SecondaryY] (with the same
 * degenerate-span expansion as single-family charts). The shared **X** window always follows the **primary**
 * viewport **X** range passed into planning; secondary **Y** ticks and transforms update whenever that **X** range,
 * the primary **Y** window, or the **spec’s** secondary-layer data changes (v1 has no independent secondary-**Y** zoom
 * channel in the viewport model).
 *
 * @property layers Drawable layers in z-order (earlier draws beneath later); layer [ids][CombinedCartesianLayer.id] must be unique.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis Shared X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Primary Y (vertical) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxisSecondary Optional secondary Y axis; must be non-null when any layer uses [CombinedCartesianYAxisBinding.SecondaryY]. Null (default) = single Y scale.
 * @property lineRender Viewport windowing and decimation shared by all [CombinedCartesianLayer.Line] layers; see [LineRenderSpec].
 * @property valueFormatter Formats the selected line/scatter point for markers.
 * @property barValueFormatter Formats the selected [com.mena97villalobos.composeplotlib.core.model.BarEntry] for markers.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the current selection; see [SelectionStyle].
 * @property referenceLines Static threshold guide lines drawn over the plot; empty by default. See [ReferenceLine].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class CombinedCartesianChartSpec(
    val layers: List<CombinedCartesianLayer>,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val yAxisSecondary: AxisSpec? = null,
    val lineRender: LineRenderSpec = LineRenderSpec(),
    /** Shared scatter downsampling for all [CombinedCartesianLayer.Scatter] layers (see [ScatterRenderSpec]). */
    val scatterRender: ScatterRenderSpec = ScatterRenderSpec(),
    val valueFormatter: LineValueFormatter = DefaultLineValueFormatter,
    val barValueFormatter: BarValueFormatter = DefaultBarValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val referenceLines: List<ReferenceLine> = emptyList(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(layers.map { it.id }, "CombinedCartesianChartSpec.layer ids")
    }

    /** True when a secondary Y axis is configured **and** at least one layer binds to [CombinedCartesianYAxisBinding.SecondaryY]. */
    fun resolvedSecondaryBinding(): Boolean = yAxisSecondary != null && layers.any { it.yAxisBinding == CombinedCartesianYAxisBinding.SecondaryY }
}
