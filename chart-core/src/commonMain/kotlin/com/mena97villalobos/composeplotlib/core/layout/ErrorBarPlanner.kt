package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.spec.ErrorBarsSpec

/**
 * One error-bar request in pixel space. [crossPx] is the whisker's x position; [centerPx], [lowPx] and [highPx]
 * are value-axis (y) pixels. A null end is omitted (no cap on that side), and the whisker spans the present ends
 * plus [centerPx]. Recall pixel-y grows downward, so a higher domain value has a smaller [highPx].
 */
internal data class ErrorBarAnchor(
    val crossPx: Double,
    val centerPx: Double,
    val lowPx: Double?,
    val highPx: Double?,
)

/**
 * Projects [anchors] into vertical whisker [DrawCommand.Line]s per [spec]: one line spanning the datum's low/high
 * pixels and a horizontal cap at each supplied end. Whiskers clamp to [plotBounds] on the y axis; caps clamp on x.
 * Anchors whose [crossPx][ErrorBarAnchor.crossPx] falls outside the plot horizontally are skipped.
 */
internal fun buildErrorBarCommands(
    anchors: List<ErrorBarAnchor>,
    spec: ErrorBarsSpec,
    plotBounds: RectD,
): List<DrawCommand> {
    if (!spec.enabled || anchors.isEmpty()) return emptyList()
    val halfCap = spec.capWidthPx.coerceAtLeast(0.0) / 2.0
    val out = ArrayList<DrawCommand>(anchors.size * 3)
    for (a in anchors) {
        if (!a.crossPx.isFinite() || a.crossPx < plotBounds.left || a.crossPx > plotBounds.right) continue
        val ends = listOf(a.lowPx, a.highPx).filterNotNull().filter { it.isFinite() }
        if (ends.isEmpty()) continue
        val spanEnds = if (a.centerPx.isFinite()) ends + a.centerPx else ends
        val yTop = spanEnds.min().coerceIn(plotBounds.top, plotBounds.bottom)
        val yBottom = spanEnds.max().coerceIn(plotBounds.top, plotBounds.bottom)
        out +=
            DrawCommand.Line(
                start = OffsetD(a.crossPx, yTop),
                end = OffsetD(a.crossPx, yBottom),
                color = spec.color,
                strokeWidth = spec.strokeWidth,
            )
        val capLeft = (a.crossPx - halfCap).coerceAtLeast(plotBounds.left)
        val capRight = (a.crossPx + halfCap).coerceAtMost(plotBounds.right)
        for (endPx in ends) {
            val y = endPx.coerceIn(plotBounds.top, plotBounds.bottom)
            out +=
                DrawCommand.Line(
                    start = OffsetD(capLeft, y),
                    end = OffsetD(capRight, y),
                    color = spec.color,
                    strokeWidth = spec.strokeWidth,
                )
        }
    }
    return out
}
