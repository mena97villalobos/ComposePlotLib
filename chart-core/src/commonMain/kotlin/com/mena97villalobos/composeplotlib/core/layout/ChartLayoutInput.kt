package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Label measurement hints passed into [ChartPlanner] so axis and legend layout can reserve space.
 *
 * Keys are the label strings produced by formatters; missing keys fall back to the corresponding fallback metrics.
 * Use [tickLabelMetrics] for per-string layout boxes; call `tickLabelMetrics(label)` to resolve one label.
 */
data class ChartTextMeasurementInput(
    val tickLabelMetrics: Map<String, TextLayoutMetrics> = emptyMap(),
    val legendLabelMetrics: Map<String, TextLayoutMetrics> = emptyMap(),
    /** Keys: decimal slice index as string (`"0"`, `"1"`, …) for contributing slices. */
    val pieSliceLabelMeasurements: Map<String, TextLayoutMetrics> = emptyMap(),
    val fallbackTickLabelMetrics: TextLayoutMetrics = TextLayoutMetrics.uniformBox(SizeD.Zero),
    val fallbackLegendLabelMetrics: TextLayoutMetrics = TextLayoutMetrics.uniformBox(SizeD.Zero),
    val fallbackPieSliceLabelMetrics: TextLayoutMetrics = TextLayoutMetrics.uniformBox(SizeD(40.0, 14.0)),
) {
    fun tickLabelMetrics(label: String): TextLayoutMetrics = tickLabelMetrics[label] ?: fallbackTickLabelMetrics

    fun legendLabelMetrics(label: String): TextLayoutMetrics = legendLabelMetrics[label] ?: fallbackLegendLabelMetrics

    /** Bounding box width/height only (ignores baseline), for legacy inset math. */
    fun tickLabelSize(label: String): SizeD = tickLabelMetrics(label).size

    fun legendLabelSize(label: String): SizeD = legendLabelMetrics(label).size

    fun pieSliceLabelMeasurement(sliceIndex: Int): TextLayoutMetrics = pieSliceLabelMeasurements[sliceIndex.toString()] ?: fallbackPieSliceLabelMetrics
}

/**
 * Inputs for layout planning: full canvas size in pixels, current visible [ChartViewport], and optional [ChartTextMeasurementInput].
 *
 * @param labelLayoutScale Scales axis **gaps**, fixed chrome offsets, and legend spacing vs their nominal pixel values.
 * Hosts should pass the text scaling factor aligned with rendered label styles (1.0 by default; larger when typography
 * is scaled for accessibility) so fixed gaps and padding stay proportional to measured labels; tick boxes still come
 * from [textMeasurements].
 */
data class ChartLayoutInput(
    val canvasSize: SizeD,
    val viewport: ChartViewport,
    val textMeasurements: ChartTextMeasurementInput = ChartTextMeasurementInput(),
    val labelLayoutScale: Double = 1.0,
) {
    val canvasBounds: RectD get() = RectD.fromSize(canvasSize)
}
