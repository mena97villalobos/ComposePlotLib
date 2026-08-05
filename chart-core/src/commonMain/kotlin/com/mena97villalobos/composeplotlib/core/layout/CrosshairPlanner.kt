package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelFormatter
import com.mena97villalobos.composeplotlib.core.spec.CrosshairSpec
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform

private const val CHIP_PAD_H = 5.0
private const val CHIP_PAD_V = 2.0
private const val CHIP_CORNER = 3.0
private const val CHIP_EDGE_GAP = 2.0

/**
 * Builds the crosshair / axis-pointer overlay for a line or scatter chart: vertical + horizontal guidelines through
 * [selection], optional axis-value chips at the plot's inner edges, and the selection knot ([SelectionStyle]).
 * Returned commands live in the plot layer, so guidelines and chips are clipped to [plotBounds]; chips are placed at
 * the inner edge for that reason. Label text is derived from the axis formatters via [ChartTransform.toDomain].
 */
internal fun buildCrosshairCommands(
    selection: ChartSelection,
    crosshair: CrosshairSpec,
    selectionStyle: SelectionStyle,
    transform: ChartTransform,
    plotBounds: RectD,
    xLabelFormatter: AxisLabelFormatter,
    yLabelFormatter: AxisLabelFormatter,
    charWidthPx: Double,
    lineHeightPx: Double,
): List<DrawCommand> {
    // Re-derive the anchor from the selection's *domain* point rather than trusting the pixel the
    // caller supplied. Tap/hover/roam already agree with this projection, but a programmatic
    // ChartSelectionState.select(...) cannot know the canvas size, and a stale pixel would otherwise
    // float the crosshair and knot off the series.
    val px = transform.toPixel(selection.target.point)
    if (!px.x.isFinite() || !px.y.isFinite()) return emptyList()
    val out = ArrayList<DrawCommand>()
    val stroke = crosshair.dashIntervals?.takeIf { it.isNotEmpty() }?.let { StrokeStyle(dashIntervals = it) }
    val xInside = px.x in plotBounds.left..plotBounds.right
    val yInside = px.y in plotBounds.top..plotBounds.bottom

    if (crosshair.showVerticalLine && xInside) {
        out +=
            DrawCommand.Line(
                start = OffsetD(px.x, plotBounds.top),
                end = OffsetD(px.x, plotBounds.bottom),
                color = crosshair.lineColor,
                strokeWidth = crosshair.strokeWidth,
                strokeStyle = stroke,
            )
    }
    if (crosshair.showHorizontalLine && yInside) {
        out +=
            DrawCommand.Line(
                start = OffsetD(plotBounds.left, px.y),
                end = OffsetD(plotBounds.right, px.y),
                color = crosshair.lineColor,
                strokeWidth = crosshair.strokeWidth,
                strokeStyle = stroke,
            )
    }
    if (crosshair.showAxisLabels) {
        val domain = selection.target.point
        val charW = charWidthPx.coerceAtLeast(1.0)
        val lineH = lineHeightPx.coerceAtLeast(1.0)
        if (xInside) {
            out += xAxisPointerChip(xLabelFormatter(domain.x), px.x, crosshair, plotBounds, charW, lineH)
        }
        if (yInside) {
            out += yAxisPointerChip(yLabelFormatter(domain.y), px.y, crosshair, plotBounds, charW, lineH)
        }
    }

    out +=
        DrawCommand.Circle(
            center = px,
            radius = selectionStyle.pointRadius,
            color = selectionStyle.pointColor,
        )
    return out
}

/** Chip centered on [centerX], sitting just inside the plot's bottom edge. */
private fun xAxisPointerChip(
    text: String,
    centerX: Double,
    crosshair: CrosshairSpec,
    plotBounds: RectD,
    charWidthPx: Double,
    lineHeightPx: Double,
): List<DrawCommand> {
    if (text.isEmpty()) return emptyList()
    val w = charWidthPx * text.length + 2 * CHIP_PAD_H
    val h = lineHeightPx + 2 * CHIP_PAD_V
    val left = (centerX - w / 2.0).coerceIn(plotBounds.left, (plotBounds.right - w).coerceAtLeast(plotBounds.left))
    val bottom = plotBounds.bottom - CHIP_EDGE_GAP
    val top = bottom - h
    return chip(RectD(left = left, top = top, right = left + w, bottom = bottom), text, crosshair)
}

/** Chip centered on [centerY], sitting just inside the plot's left edge. */
private fun yAxisPointerChip(
    text: String,
    centerY: Double,
    crosshair: CrosshairSpec,
    plotBounds: RectD,
    charWidthPx: Double,
    lineHeightPx: Double,
): List<DrawCommand> {
    if (text.isEmpty()) return emptyList()
    val w = charWidthPx * text.length + 2 * CHIP_PAD_H
    val h = lineHeightPx + 2 * CHIP_PAD_V
    val left = plotBounds.left + CHIP_EDGE_GAP
    val top = (centerY - h / 2.0).coerceIn(plotBounds.top, (plotBounds.bottom - h).coerceAtLeast(plotBounds.top))
    return chip(RectD(left = left, top = top, right = left + w, bottom = top + h), text, crosshair)
}

private fun chip(
    bounds: RectD,
    text: String,
    crosshair: CrosshairSpec,
): List<DrawCommand> {
    val out = ArrayList<DrawCommand>(2)
    crosshair.labelBackground?.let { bg ->
        out +=
            DrawCommand.RoundRect(
                bounds = bounds,
                cornerRadius = CHIP_CORNER,
                style = ShapeDrawStyle.Fill(color = bg),
            )
    }
    out +=
        DrawCommand.Text(
            text = text,
            position = OffsetD(bounds.left + CHIP_PAD_H, bounds.top + CHIP_PAD_V),
            color = crosshair.labelColor,
        )
    return out
}
