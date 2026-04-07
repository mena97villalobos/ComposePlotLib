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
    SharedY,
    SecondaryY,
}

/**
 * One drawable layer in a [CombinedCartesianChartSpec], in z-order (earlier draws beneath later).
 */
sealed class CombinedCartesianLayer {
    abstract val id: String
    abstract val yAxisBinding: CombinedCartesianYAxisBinding

    data class Line(
        override val id: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val series: List<LineSeries>,
    ) : CombinedCartesianLayer()

    data class Bar(
        override val id: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val groups: List<BarGroup>,
        val orientation: BarChartOrientation = BarChartOrientation.Vertical,
        val barLayout: BarLayoutMode = BarLayoutMode.Independent,
        val clusterBarGapPx: Double = 4.0,
    ) : CombinedCartesianLayer()

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
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    fun resolvedSecondaryBinding(): Boolean = yAxisSecondary != null && layers.any { it.yAxisBinding == CombinedCartesianYAxisBinding.SecondaryY }
}
