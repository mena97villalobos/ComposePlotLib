package com.mena97villalobos.composeplotlib.core.export

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.ChartTextMeasurementInput
import com.mena97villalobos.composeplotlib.core.layout.TextLayoutMetrics
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Plans [spec] headlessly at the given pixel size and serializes it to an SVG string via [chartLayoutPlanToSvg] —
 * a one-call vector export for line charts, with no Compose or rasterization involved.
 *
 * Label geometry uses uniform fallback metrics (no live text measurement), so axis labels are positioned
 * approximately; the plotted data, grid, axes, and legend are exact.
 *
 * @param spec the line chart to export.
 * @param widthPx / @param heightPx output canvas size in pixels.
 * @param viewport visible domain window; defaults to the data extent of [spec].
 * @param background optional page background.
 */
fun lineChartToSvg(
    spec: LineChartSpec,
    widthPx: Double,
    heightPx: Double,
    viewport: ChartViewport = lineChartDataViewport(spec),
    background: ChartColor? = null,
): String {
    val input =
        ChartLayoutInput(
            canvasSize = SizeD(widthPx, heightPx),
            viewport = viewport,
            textMeasurements =
                ChartTextMeasurementInput(
                    fallbackTickLabelMetrics = TextLayoutMetrics.uniformBox(SizeD(32.0, 14.0)),
                    fallbackLegendLabelMetrics = TextLayoutMetrics.uniformBox(SizeD(52.0, 14.0)),
                ),
        )
    val plan = ChartPlanner.planLineChart(input, spec, selection = null).layout
    return chartLayoutPlanToSvg(plan, background)
}

/** Axis-aligned data extent of every finite point across [spec]'s series; falls back to `[0,1]` when empty. */
fun lineChartDataViewport(spec: LineChartSpec): ChartViewport {
    val points = spec.series.flatMap { it.points }.filter { it.x.isFinite() && it.y.isFinite() }
    if (points.isEmpty()) return ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0))
    val xs = points.map { it.x }
    val ys = points.map { it.y }
    return ChartViewport(
        xRange = paddedRange(xs.min(), xs.max()),
        yRange = paddedRange(ys.min(), ys.max()),
    )
}

private fun paddedRange(
    min: Double,
    max: Double,
): ClosedRangeD {
    if (min == max) {
        val pad = if (min == 0.0) 1.0 else abs(min) * 0.1
        return ClosedRangeD(min - pad, max + pad)
    }
    return ClosedRangeD(min, max)
}

private fun abs(value: Double): Double = if (value < 0.0) -value else value
