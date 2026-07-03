package com.mena97villalobos.composeplotlib.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mena97villalobos.composeplotlib.chart.internal.canvas.toComposeColor
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.selection.ChartBrushAxis
import com.mena97villalobos.composeplotlib.core.selection.ChartRangeSelection
import com.mena97villalobos.composeplotlib.core.selection.chartRangeSelectionFromDomainCorners
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Opt-in **range brush / box selection** for a chart. When attached, a drag across the plot draws a selection
 * rectangle and reports the covered domain region as a [ChartRangeSelection] via [onBrush] (or `null` when the
 * drag is too small — a tap-to-clear). Pan / zoom / tap are suspended while a brush is attached.
 *
 * Typical uses: zoom-to-selection (`viewportState.zoomTo(...)`), linked filtering, or driving a detail chart.
 *
 * @param axis which axes the brush constrains (default [ChartBrushAxis.Horizontal] — an x-range band).
 * @param fill translucent fill of the selection rectangle.
 * @param stroke selection rectangle border color.
 * @param strokeWidthPx selection border width in pixels.
 * @param minDragPx minimum drag distance (px) before a selection is emitted; shorter drags clear it.
 * @param onBrush receives the committed [ChartRangeSelection], or `null` when cleared.
 */
data class ChartRangeBrush(
    val axis: ChartBrushAxis = ChartBrushAxis.Horizontal,
    val fill: ChartColor = ChartColor(0x334E79A7),
    val stroke: ChartColor = ChartColor(0xCC4E79A7),
    val strokeWidthPx: Double = 1.5,
    val minDragPx: Double = 6.0,
    val onBrush: (ChartRangeSelection?) -> Unit,
)

/** Converts a committed drag (start/end pixels) into a domain [ChartRangeSelection], or `null` if too small. */
internal fun resolveChartRangeBrush(
    startPx: Offset,
    endPx: Offset,
    plotBounds: RectD,
    transform: ChartTransform,
    viewport: ChartViewport,
    brush: ChartRangeBrush,
): ChartRangeSelection? {
    val dragPx = hypot((endPx.x - startPx.x).toDouble(), (endPx.y - startPx.y).toDouble())
    if (dragPx < brush.minDragPx) return null
    val a = clampOffsetToPlot(startPx, plotBounds)
    val b = clampOffsetToPlot(endPx, plotBounds)
    val da = transform.toDomain(OffsetD(a.x.toDouble(), a.y.toDouble()))
    val db = transform.toDomain(OffsetD(b.x.toDouble(), b.y.toDouble()))
    return chartRangeSelectionFromDomainCorners(da.x, da.y, db.x, db.y, brush.axis, viewport.xRange, viewport.yRange)
}

/** Pixel rectangle (clamped to [plotBounds]) for an in-progress drag from [startPx] to [endPx]. */
internal fun chartLiveBrushRect(
    startPx: Offset,
    endPx: Offset,
    plotBounds: RectD,
): Rect = plotClampedRect(startPx.x.toDouble(), startPx.y.toDouble(), endPx.x.toDouble(), endPx.y.toDouble(), plotBounds)

/** Draws the brush [rect] as a translucent fill plus border; a degenerate rectangle draws nothing. */
internal fun DrawScope.drawChartRangeBrush(
    rect: Rect,
    brush: ChartRangeBrush,
) {
    if (rect.width <= 0f || rect.height <= 0f) return
    drawRect(color = brush.fill.toComposeColor(), topLeft = rect.topLeft, size = rect.size)
    drawRect(
        color = brush.stroke.toComposeColor(),
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = brush.strokeWidthPx.toFloat()),
    )
}

private fun plotClampedRect(
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    plotBounds: RectD,
): Rect {
    val left = max(min(ax, bx), plotBounds.left)
    val right = min(max(ax, bx), plotBounds.right)
    val top = max(min(ay, by), plotBounds.top)
    val bottom = min(max(ay, by), plotBounds.bottom)
    return Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

private fun clampOffsetToPlot(
    offset: Offset,
    plotBounds: RectD,
): Offset =
    Offset(
        x =
            offset.x
                .toDouble()
                .coerceIn(plotBounds.left, plotBounds.right)
                .toFloat(),
        y =
            offset.y
                .toDouble()
                .coerceIn(plotBounds.top, plotBounds.bottom)
                .toFloat(),
    )
