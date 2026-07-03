package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.spec.DataLabelPlacement
import com.mena97villalobos.composeplotlib.core.spec.DataLabelsSpec

/**
 * One value-label placement request: the reference [mark] pixel, the already-formatted [text], and the glyph
 * half-extent [markHalfPx] the label should clear (e.g. a point radius; `0` for a bar top edge).
 */
internal data class DataLabelAnchor(
    val mark: OffsetD,
    val text: String,
    val markHalfPx: Double = 0.0,
)

/**
 * Turns [anchors] into [DrawCommand.Text] labels per [spec]. Label box sizes are estimated from [charWidthPx] /
 * [lineHeightPx]; when [DataLabelsSpec.avoidOverlap] is on, a label is dropped if its box would leave [plotBounds]
 * or collide with an already-placed label (greedy first-wins in anchor order).
 */
internal fun buildDataLabelCommands(
    anchors: List<DataLabelAnchor>,
    spec: DataLabelsSpec,
    plotBounds: RectD,
    charWidthPx: Double,
    lineHeightPx: Double,
): List<DrawCommand.Text> {
    if (!spec.enabled || anchors.isEmpty()) return emptyList()
    val charW = charWidthPx.coerceAtLeast(1.0)
    val lineH = lineHeightPx.coerceAtLeast(1.0)
    val placed = ArrayList<RectD>(anchors.size)
    val out = ArrayList<DrawCommand.Text>(anchors.size)
    for (a in anchors) {
        if (a.text.isEmpty() || !a.mark.x.isFinite() || !a.mark.y.isFinite()) continue
        val w = charW * a.text.length
        val box = labelBox(a.mark, spec.placement, spec.offsetPx + a.markHalfPx.coerceAtLeast(0.0), w, lineH)
        if (spec.avoidOverlap) {
            val insidePlot =
                box.left >= plotBounds.left - 1.0 &&
                    box.right <= plotBounds.right + 1.0 &&
                    box.top >= plotBounds.top - 1.0 &&
                    box.bottom <= plotBounds.bottom + 1.0
            if (!insidePlot) continue
            if (placed.any { it.intersects(box) }) continue
            placed.add(box)
        }
        out.add(DrawCommand.Text(text = a.text, position = OffsetD(box.left, box.top), color = spec.color))
    }
    return out
}

private fun labelBox(
    mark: OffsetD,
    placement: DataLabelPlacement,
    gap: Double,
    w: Double,
    h: Double,
): RectD =
    when (placement) {
        DataLabelPlacement.Above -> {
            val bottom = mark.y - gap
            RectD(left = mark.x - w / 2.0, top = bottom - h, right = mark.x + w / 2.0, bottom = bottom)
        }
        DataLabelPlacement.Below -> {
            val top = mark.y + gap
            RectD(left = mark.x - w / 2.0, top = top, right = mark.x + w / 2.0, bottom = top + h)
        }
        DataLabelPlacement.Left -> {
            val right = mark.x - gap
            RectD(left = right - w, top = mark.y - h / 2.0, right = right, bottom = mark.y + h / 2.0)
        }
        DataLabelPlacement.Right -> {
            val left = mark.x + gap
            RectD(left = left, top = mark.y - h / 2.0, right = left + w, bottom = mark.y + h / 2.0)
        }
        DataLabelPlacement.Center ->
            RectD(left = mark.x - w / 2.0, top = mark.y - h / 2.0, right = mark.x + w / 2.0, bottom = mark.y + h / 2.0)
    }

/** Estimated `(avgCharWidthPx, lineHeightPx)` for label boxes, sampled from measured tick labels or scaled defaults. */
internal fun estimateLabelCharBox(input: ChartLayoutInput): Pair<Double, Double> {
    for ((label, m) in input.textMeasurements.tickLabelMetrics) {
        if (label.isNotEmpty() && m.width > 0.0 && m.height > 0.0) {
            return (m.width / label.length) to m.height
        }
    }
    val s = input.labelLayoutScale.coerceIn(0.5, 4.0)
    return (6.5 * s) to (15.0 * s)
}
