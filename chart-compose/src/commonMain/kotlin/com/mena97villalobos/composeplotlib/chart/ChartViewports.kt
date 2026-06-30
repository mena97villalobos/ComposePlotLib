package com.mena97villalobos.composeplotlib.chart

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs

/** [ChartViewport] that fits all line points; uses a small default range when there is no (finite) data. */
fun defaultViewport(spec: LineChartSpec): ChartViewport {
    val points =
        spec.series
            .flatMap { it.points }
            .map { XyPoint(it.x, it.y) }
            .filter { it.x.isFinite() && it.y.isFinite() }
    if (points.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }.ensureSpan(minX)
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }.ensureSpan(minY)

    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

/** [ChartViewport] that fits all scatter points; uses a small default range when there is no (finite) data. */
fun defaultViewport(spec: ScatterChartSpec): ChartViewport {
    val points =
        spec.series
            .flatMap { it.points }
            .map { XyPoint(it.x, it.y) }
            .filter { it.x.isFinite() && it.y.isFinite() }
    if (points.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }.ensureSpan(minX)
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }.ensureSpan(minY)

    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

/**
 * Dummy [ChartViewport] for pie/donut charts (radial; pan/zoom are normally disabled via [ChartGestureConfig]).
 * Present so [com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput] and measurement passes stay aligned with other families.
 */
fun defaultViewport(spec: PieChartSpec): ChartViewport =
    ChartViewport(
        xRange = ClosedRangeD(0.0, 1.0),
        yRange = ClosedRangeD(0.0, 1.0),
    )

/**
 * Dummy [ChartViewport] for radar/spider charts (radial; pan/zoom are normally disabled via [ChartGestureConfig]).
 * Present so [com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput] and measurement passes stay aligned with other families.
 */
fun defaultViewport(spec: RadarChartSpec): ChartViewport =
    ChartViewport(
        xRange = ClosedRangeD(0.0, 1.0),
        yRange = ClosedRangeD(0.0, 1.0),
    )

/**
 * Dummy [ChartViewport] for gauge / progress-ring charts (radial; non-interactive).
 * Present so [com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput] and measurement passes stay aligned with other families.
 */
fun defaultViewport(spec: GaugeChartSpec): ChartViewport =
    ChartViewport(
        xRange = ClosedRangeD(0.0, 1.0),
        yRange = ClosedRangeD(0.0, 1.0),
    )

/**
 * [ChartViewport] for the **primary** Y scale: spans X from all layers and Y from layers bound to [CombinedCartesianYAxisBinding.SharedY].
 * Secondary Y remains data-derived in the planner. Uses a small default range when there is no shared-Y data.
 */
fun defaultViewport(spec: CombinedCartesianChartSpec): ChartViewport {
    val xs = ArrayList<Double>()
    val ysPrimary = ArrayList<Double>()

    // Skip non-finite (NaN/Infinity) samples so a single bad coordinate cannot make the
    // derived ClosedRangeD non-finite (which would now throw) or silently collapse the chart.
    fun addXy(
        x: Double,
        y: Double,
    ) {
        if (x.isFinite() && y.isFinite()) {
            xs += x
            ysPrimary += y
        }
    }
    for (layer in spec.layers) {
        if (layer.yAxisBinding != CombinedCartesianYAxisBinding.SharedY) continue
        when (layer) {
            is CombinedCartesianLayer.Line ->
                layer.series.forEach { s ->
                    s.points.forEach { addXy(it.x, it.y) }
                }
            is CombinedCartesianLayer.Bar ->
                if (layer.orientation == BarChartOrientation.Horizontal) {
                    layer.groups.forEach { g ->
                        g.entries.forEach { addXy(it.y, it.x) }
                    }
                } else {
                    layer.groups.forEach { g ->
                        g.entries.forEach { addXy(it.x, it.y) }
                    }
                }
            is CombinedCartesianLayer.Scatter ->
                layer.series.forEach { s ->
                    s.points.forEach { addXy(it.x, it.y) }
                }
        }
    }
    if (xs.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    var minX = xs.min()
    var maxX = xs.max().ensureSpan(minX)
    if (ysPrimary.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(minX, maxX),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val hasVerticalBar =
        spec.layers.any {
            it is CombinedCartesianLayer.Bar &&
                it.yAxisBinding == CombinedCartesianYAxisBinding.SharedY &&
                it.orientation == BarChartOrientation.Vertical
        }
    val hasHorizontalBar =
        spec.layers.any {
            it is CombinedCartesianLayer.Bar &&
                it.yAxisBinding == CombinedCartesianYAxisBinding.SharedY &&
                it.orientation == BarChartOrientation.Horizontal
        }
    if (hasHorizontalBar) {
        minX = minOf(0.0, minX)
        maxX = xs.max().ensureSpan(minX)
    }
    val minY =
        when {
            hasVerticalBar -> minOf(0.0, ysPrimary.min())
            else -> ysPrimary.min()
        }
    val maxY = ysPrimary.max().ensureSpan(minY)
    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

/** [ChartViewport] that fits all bar entries (value axis includes zero when data is above the baseline); uses a small default range when empty. */
fun defaultViewport(spec: BarChartSpec): ChartViewport {
    val entries = spec.groups.flatMap { it.entries }.filter { it.x.isFinite() && it.y.isFinite() }
    if (entries.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    return when (spec.orientation) {
        BarChartOrientation.Vertical -> {
            val minX = entries.minOf { it.x }
            val maxX = entries.maxOf { it.x }.ensureSpan(minX)
            val minY = minOf(0.0, entries.minOf { it.y })
            val maxY = entries.maxOf { it.y }.ensureSpan(minY)
            ChartViewport(
                xRange = ClosedRangeD(minX, maxX),
                yRange = ClosedRangeD(minY, maxY),
            )
        }
        BarChartOrientation.Horizontal -> {
            val minCat = entries.minOf { it.x }
            val maxCat = entries.maxOf { it.x }.ensureSpan(minCat)
            val minVal = minOf(0.0, entries.minOf { it.y })
            val maxVal = entries.maxOf { it.y }.ensureSpan(minVal)
            ChartViewport(
                xRange = ClosedRangeD(minVal, maxVal),
                yRange = ClosedRangeD(minCat, maxCat),
            )
        }
    }
}

/**
 * [ChartViewport] that fits all candles: X from the candle positions, Y from the lowest low to the highest
 * high across every series. Unlike bars, the price (Y) axis does **not** force-include zero. Uses a small
 * default range when there is no (finite) data.
 */
fun defaultViewport(spec: CandlestickChartSpec): ChartViewport {
    val points = spec.series.flatMap { it.points }
    val xs = points.map { it.x }.filter { it.isFinite() }
    val lows = points.map { it.low }.filter { it.isFinite() }
    val highs = points.map { it.high }.filter { it.isFinite() }
    if (xs.isEmpty() || lows.isEmpty() || highs.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val minX = xs.min()
    val maxX = xs.max().ensureSpan(minX)
    val minY = lows.min()
    val maxY = highs.max().ensureSpan(minY)
    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

/**
 * [ChartViewport] that centers each cell on its integer index.
 *
 * The X range spans `[-0.5, columnCount - 0.5]` and Y range `[-0.5, rowCount - 0.5]` so that each
 * category tick falls at the center of its cell. Uses a 1-cell default range when the spec is empty.
 */
fun defaultViewport(spec: HeatmapChartSpec): ChartViewport {
    val cols = spec.columnLabels.size.coerceAtLeast(1)
    val rows = spec.rowLabels.size.coerceAtLeast(1)
    return ChartViewport(
        xRange = ClosedRangeD(-0.5, cols - 0.5),
        yRange = ClosedRangeD(-0.5, rows - 0.5),
    )
}

private fun Double.ensureSpan(min: Double): Double = if (this == min || abs(this - min) < FpMath.EPS) this + FpMath.DEFAULT_DEGENERATE_PAD else this
