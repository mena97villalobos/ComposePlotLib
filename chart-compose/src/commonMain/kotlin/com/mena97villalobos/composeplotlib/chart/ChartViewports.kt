package com.mena97villalobos.composeplotlib.chart

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs

/** [ChartViewport] that fits all line points; uses a small default range when there is no data. */
fun defaultViewport(spec: LineChartSpec): ChartViewport {
    val points = spec.series.flatMap { it.points }.map { XyPoint(it.x, it.y) }
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

/** [ChartViewport] that fits all scatter points; uses a small default range when there is no data. */
fun defaultViewport(spec: ScatterChartSpec): ChartViewport {
    val points = spec.series.flatMap { it.points }.map { XyPoint(it.x, it.y) }
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
 * Present so [ChartLayoutInput] and measurement passes stay aligned with other families.
 */
fun defaultViewport(spec: PieChartSpec): ChartViewport {
    return ChartViewport(
        xRange = ClosedRangeD(0.0, 1.0),
        yRange = ClosedRangeD(0.0, 1.0),
    )
}

/**
 * [ChartViewport] for the **primary** Y scale: spans X from all layers and Y from layers bound to [CombinedCartesianYAxisBinding.SharedY].
 * Secondary Y remains data-derived in the planner. Uses a small default range when there is no shared-Y data.
 */
fun defaultViewport(spec: CombinedCartesianChartSpec): ChartViewport {
    val xs = ArrayList<Double>()
    val ysPrimary = ArrayList<Double>()
    for (layer in spec.layers) {
        if (layer.yAxisBinding != CombinedCartesianYAxisBinding.SharedY) continue
        when (layer) {
            is CombinedCartesianLayer.Line ->
                layer.series.forEach { s ->
                    s.points.forEach {
                        xs += it.x
                        ysPrimary += it.y
                    }
                }
            is CombinedCartesianLayer.Bar ->
                layer.groups.forEach { g ->
                    g.entries.forEach {
                        xs += it.x
                        ysPrimary += it.y
                    }
                }
            is CombinedCartesianLayer.Scatter ->
                layer.series.forEach { s ->
                    s.points.forEach {
                        xs += it.x
                        ysPrimary += it.y
                    }
                }
        }
    }
    if (xs.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val minX = xs.min()
    val maxX = xs.max().ensureSpan(minX)
    if (ysPrimary.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(minX, maxX),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val hasBar =
        spec.layers.any {
            it is CombinedCartesianLayer.Bar && it.yAxisBinding == CombinedCartesianYAxisBinding.SharedY
        }
    val minY = if (hasBar) minOf(0.0, ysPrimary.min()) else ysPrimary.min()
    val maxY = ysPrimary.max().ensureSpan(minY)
    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

/** [ChartViewport] that fits all bar entries (y includes zero when data is above the axis); uses a small default range when empty. */
fun defaultViewport(spec: BarChartSpec): ChartViewport {
    val entries = spec.groups.flatMap { it.entries }
    if (entries.isEmpty()) {
        return ChartViewport(
            xRange = ClosedRangeD(0.0, 1.0),
            yRange = ClosedRangeD(0.0, 1.0),
        )
    }
    val minX = entries.minOf { it.x }
    val maxX = entries.maxOf { it.x }.ensureSpan(minX)
    val minY = minOf(0.0, entries.minOf { it.y })
    val maxY = entries.maxOf { it.y }.ensureSpan(minY)

    return ChartViewport(
        xRange = ClosedRangeD(minX, maxX),
        yRange = ClosedRangeD(minY, maxY),
    )
}

private fun Double.ensureSpan(min: Double): Double = if (this == min || abs(this - min) < FpMath.EPS) this + FpMath.DEFAULT_DEGENERATE_PAD else this
