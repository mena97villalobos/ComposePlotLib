package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.spec.ChartAnnotation
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import kotlin.math.max
import kotlin.math.min

/**
 * Projects [annotations] (data-space marks: points, text, boxes, segments) into [DrawCommand]s over [plotBounds].
 * Label box sizes use [charWidthPx] / [lineHeightPx] estimates. Non-finite anchors are skipped; boxes clamp to the plot.
 */
internal fun buildAnnotationCommands(
    annotations: List<ChartAnnotation>,
    transform: ChartTransform,
    plotBounds: RectD,
    charWidthPx: Double,
    lineHeightPx: Double,
): List<DrawCommand> {
    if (annotations.isEmpty()) return emptyList()
    val out = ArrayList<DrawCommand>()
    for (a in annotations) {
        when (a) {
            is ChartAnnotation.Point -> {
                val px = transform.toPixel(XyPoint(a.x, a.y))
                if (!px.x.isFinite() || !px.y.isFinite()) continue
                out += DrawCommand.Circle(center = px, radius = a.radius.coerceAtLeast(0.0), color = a.color)
                a.label?.let { lbl ->
                    out += textCommand(lbl, px, a.labelPlacement, a.radius.coerceAtLeast(0.0) + 6.0, a.labelColor, charWidthPx, lineHeightPx)
                }
            }
            is ChartAnnotation.Text -> {
                val px = transform.toPixel(XyPoint(a.x, a.y))
                if (!px.x.isFinite() || !px.y.isFinite()) continue
                out += textCommand(a.text, px, a.placement, 0.0, a.color, charWidthPx, lineHeightPx)
            }
            is ChartAnnotation.Box -> {
                val rect = boxRect(a, transform, plotBounds) ?: continue
                a.fillColor?.let { out += DrawCommand.Rect(bounds = rect, color = it) }
                a.borderColor?.let { border -> out += borderLines(rect, border, a.borderWidth) }
                a.label?.let { lbl ->
                    out += DrawCommand.Text(text = lbl, position = OffsetD(rect.left + 4.0, rect.top + 2.0), color = a.labelColor)
                }
            }
            is ChartAnnotation.Segment -> {
                val p1 = transform.toPixel(XyPoint(a.x1, a.y1))
                val p2 = transform.toPixel(XyPoint(a.x2, a.y2))
                if (!p1.x.isFinite() || !p1.y.isFinite() || !p2.x.isFinite() || !p2.y.isFinite()) continue
                val stroke = a.dashIntervals?.takeIf { it.isNotEmpty() }?.let { StrokeStyle(dashIntervals = it) }
                out += DrawCommand.Line(start = p1, end = p2, color = a.color, strokeWidth = a.strokeWidth, strokeStyle = stroke)
            }
        }
    }
    return out
}

private fun textCommand(
    text: String,
    anchor: OffsetD,
    placement: com.mena97villalobos.composeplotlib.core.spec.DataLabelPlacement,
    gap: Double,
    color: com.mena97villalobos.composeplotlib.core.model.ChartColor,
    charWidthPx: Double,
    lineHeightPx: Double,
): DrawCommand.Text {
    val w = charWidthPx.coerceAtLeast(1.0) * text.length
    val box = labelBox(anchor, placement, gap, w, lineHeightPx.coerceAtLeast(1.0))
    return DrawCommand.Text(text = text, position = OffsetD(box.left, box.top), color = color)
}

private fun boxRect(
    box: ChartAnnotation.Box,
    transform: ChartTransform,
    plotBounds: RectD,
): RectD? {
    val xr = box.xRange
    val yr = box.yRange
    var left = if (xr != null) transform.toPixel(XyPoint(xr.start, 0.0)).x else plotBounds.left
    var right = if (xr != null) transform.toPixel(XyPoint(xr.endInclusive, 0.0)).x else plotBounds.right
    var top = if (yr != null) transform.toPixel(XyPoint(0.0, yr.endInclusive)).y else plotBounds.top
    var bottom = if (yr != null) transform.toPixel(XyPoint(0.0, yr.start)).y else plotBounds.bottom
    if (left > right) {
        val t = left
        left = right
        right = t
    }
    if (top > bottom) {
        val t = top
        top = bottom
        bottom = t
    }
    left = max(left, plotBounds.left)
    right = min(right, plotBounds.right)
    top = max(top, plotBounds.top)
    bottom = min(bottom, plotBounds.bottom)
    if (!left.isFinite() || !right.isFinite() || !top.isFinite() || !bottom.isFinite()) return null
    if (right <= left || bottom <= top) return null
    return RectD(left = left, top = top, right = right, bottom = bottom)
}

private fun borderLines(
    rect: RectD,
    color: com.mena97villalobos.composeplotlib.core.model.ChartColor,
    width: Double,
): List<DrawCommand> {
    val w = width.coerceAtLeast(0.0)
    val tl = OffsetD(rect.left, rect.top)
    val tr = OffsetD(rect.right, rect.top)
    val br = OffsetD(rect.right, rect.bottom)
    val bl = OffsetD(rect.left, rect.bottom)
    return listOf(
        DrawCommand.Line(start = tl, end = tr, color = color, strokeWidth = w),
        DrawCommand.Line(start = tr, end = br, color = color, strokeWidth = w),
        DrawCommand.Line(start = br, end = bl, color = color, strokeWidth = w),
        DrawCommand.Line(start = bl, end = tl, color = color, strokeWidth = w),
    )
}
