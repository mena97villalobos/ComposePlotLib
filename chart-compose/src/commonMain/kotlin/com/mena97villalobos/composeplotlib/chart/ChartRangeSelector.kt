package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mena97villalobos.composeplotlib.chart.internal.canvas.toComposeColor
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Colors and metrics for [ChartRangeSelector]. */
data class ChartRangeSelectorStyle(
    val lineColor: ChartColor = ChartColor(0xFF8894A6),
    val lineWidthPx: Double = 1.5,
    val windowFill: ChartColor = ChartColor(0x224E79A7),
    val windowBorder: ChartColor = ChartColor(0xCC4E79A7),
    val windowBorderWidthPx: Double = 1.5,
    val dimColor: ChartColor = ChartColor(0x66202632),
    val handleWidthPx: Double = 10.0,
    val verticalInsetPx: Double = 6.0,
)

/**
 * Compact **range selector / minimap / brush navigator**: a full-width overview of [spec] over the whole
 * pan/zoom extent ([ChartViewportState.domain]) with a draggable **window** that mirrors — and drives — the
 * current viewport of [viewportState].
 *
 * Drag the window body to pan, drag either edge handle to zoom that side, or drag outside the window to jump
 * to that position. Pairs with a full chart sharing the same [viewportState] (place this below it), so the
 * navigator and the chart stay in lock-step.
 *
 * The overview is drawn edge-to-edge (no axes/insets), so its x maps linearly onto the domain — no dependency
 * on the main chart's internal layout.
 *
 * @param spec the series to preview (only point geometry is used; axes/legend are ignored).
 * @param viewportState the shared viewport this navigator reflects and controls.
 * @param modifier layout modifier (give it a small height, e.g. 56.dp).
 * @param style overview line, window, and handle appearance.
 */
@Composable
fun ChartRangeSelector(
    spec: LineChartSpec,
    viewportState: ChartViewportState,
    modifier: Modifier = Modifier,
    style: ChartRangeSelectorStyle = ChartRangeSelectorStyle(),
) {
    var dragZone by remember { mutableStateOf(ChartRangeSelectorZone.Body) }

    Canvas(
        modifier =
            modifier
                .semantics {
                    contentDescription = "Range selector. Drag the window to pan, drag its edges to zoom the chart."
                }.pointerInput(viewportState, style) {
                    detectDragGestures(
                        onDragStart = { start ->
                            val width = size.width.toDouble()
                            val domainX = viewportState.domain.xRange
                            val leftPx = domainToPx(viewportState.viewport.xRange.start, domainX, width)
                            val rightPx = domainToPx(viewportState.viewport.xRange.endInclusive, domainX, width)
                            dragZone = chartRangeSelectorZone(start.x.toDouble(), leftPx, rightPx, style.handleWidthPx)
                            if (dragZone == ChartRangeSelectorZone.Outside) {
                                val target = pxToDomain(start.x.toDouble(), domainX, width)
                                viewportState.panTo(centerX = target, centerY = viewportState.viewport.yRange.midpoint)
                                dragZone = ChartRangeSelectorZone.Body
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val width = max(size.width.toDouble(), 1.0)
                            val domainX = viewportState.domain.xRange
                            val deltaDomain = (dragAmount.x.toDouble() / width) * domainX.span
                            val current = viewportState.viewport
                            when (dragZone) {
                                ChartRangeSelectorZone.Body ->
                                    viewportState.panTo(
                                        centerX = current.xRange.midpoint + deltaDomain,
                                        centerY = current.yRange.midpoint,
                                    )
                                ChartRangeSelectorZone.LeftHandle -> {
                                    val newStart = min(current.xRange.start + deltaDomain, current.xRange.endInclusive)
                                    applyWindow(viewportState, newStart, current.xRange.endInclusive)
                                }
                                ChartRangeSelectorZone.RightHandle -> {
                                    val newEnd = max(current.xRange.endInclusive + deltaDomain, current.xRange.start)
                                    applyWindow(viewportState, current.xRange.start, newEnd)
                                }
                                ChartRangeSelectorZone.Outside -> Unit
                            }
                        },
                    )
                },
    ) {
        drawRangeSelector(spec, viewportState, style)
    }
}

/** Which part of the navigator window a drag started on. */
internal enum class ChartRangeSelectorZone { LeftHandle, RightHandle, Body, Outside }

/** Classifies a drag-start x against the window edges: near an edge → a handle, inside → body, else outside. */
internal fun chartRangeSelectorZone(
    downX: Double,
    windowLeftPx: Double,
    windowRightPx: Double,
    handlePx: Double,
): ChartRangeSelectorZone {
    val nearLeft = abs(downX - windowLeftPx) <= handlePx
    val nearRight = abs(downX - windowRightPx) <= handlePx
    return when {
        nearLeft && (!nearRight || abs(downX - windowLeftPx) <= abs(downX - windowRightPx)) -> ChartRangeSelectorZone.LeftHandle
        nearRight -> ChartRangeSelectorZone.RightHandle
        downX in windowLeftPx..windowRightPx -> ChartRangeSelectorZone.Body
        else -> ChartRangeSelectorZone.Outside
    }
}

private fun applyWindow(
    viewportState: ChartViewportState,
    startX: Double,
    endX: Double,
) {
    val lo = min(startX, endX)
    val hi = max(startX, endX)
    val current = viewportState.viewport
    viewportState.zoomTo(
        xSpan = hi - lo,
        ySpan = current.yRange.span,
        centerX = (lo + hi) / 2.0,
        centerY = current.yRange.midpoint,
    )
}

private fun domainToPx(
    value: Double,
    domainX: ClosedRangeD,
    widthPx: Double,
): Double {
    val span = domainX.span
    if (span <= 0.0) return 0.0
    return ((value - domainX.start) / span) * widthPx
}

private fun pxToDomain(
    px: Double,
    domainX: ClosedRangeD,
    widthPx: Double,
): Double {
    if (widthPx <= 0.0) return domainX.start
    return domainX.start + (px / widthPx) * domainX.span
}

private fun DrawScope.drawRangeSelector(
    spec: LineChartSpec,
    viewportState: ChartViewportState,
    style: ChartRangeSelectorStyle,
) {
    val width = size.width.toDouble()
    val height = size.height.toDouble()
    if (width <= 0.0 || height <= 0.0) return
    val domain = viewportState.domain
    val xSpan = domain.xRange.span
    val ySpan = domain.yRange.span
    val top = style.verticalInsetPx
    val bottom = height - style.verticalInsetPx
    val plotHeight = max(bottom - top, 1.0)

    if (xSpan > 0.0 && ySpan > 0.0) {
        spec.series.forEach { series ->
            val points = series.points.filter { it.x.isFinite() && it.y.isFinite() }
            if (points.size < 2) return@forEach
            val path = Path()
            points.forEachIndexed { index, point ->
                val px = ((point.x - domain.xRange.start) / xSpan) * width
                val py = bottom - ((point.y - domain.yRange.start) / ySpan) * plotHeight
                if (index == 0) path.moveTo(px.toFloat(), py.toFloat()) else path.lineTo(px.toFloat(), py.toFloat())
            }
            drawPath(path, color = style.lineColor.toComposeColor(), style = Stroke(width = style.lineWidthPx.toFloat()))
        }
    }

    val leftPx = domainToPx(viewportState.viewport.xRange.start, domain.xRange, width).coerceIn(0.0, width)
    val rightPx = domainToPx(viewportState.viewport.xRange.endInclusive, domain.xRange, width).coerceIn(0.0, width)

    // Dim the area outside the window.
    val dim = style.dimColor.toComposeColor()
    if (leftPx > 0.0) drawRect(color = dim, topLeft = Offset(0f, 0f), size = Size(leftPx.toFloat(), height.toFloat()))
    if (rightPx < width) {
        drawRect(color = dim, topLeft = Offset(rightPx.toFloat(), 0f), size = Size((width - rightPx).toFloat(), height.toFloat()))
    }

    // Window fill + border + edge handles.
    val windowWidth = max(rightPx - leftPx, 0.0)
    drawRect(color = style.windowFill.toComposeColor(), topLeft = Offset(leftPx.toFloat(), 0f), size = Size(windowWidth.toFloat(), height.toFloat()))
    drawRect(
        color = style.windowBorder.toComposeColor(),
        topLeft = Offset(leftPx.toFloat(), 0f),
        size = Size(windowWidth.toFloat(), height.toFloat()),
        style = Stroke(width = style.windowBorderWidthPx.toFloat()),
    )
    val handle = style.windowBorder.toComposeColor()
    val handleW = (style.windowBorderWidthPx * 2.0).toFloat()
    drawRect(color = handle, topLeft = Offset(leftPx.toFloat(), 0f), size = Size(handleW, height.toFloat()))
    drawRect(color = handle, topLeft = Offset((rightPx.toFloat() - handleW), 0f), size = Size(handleW, height.toFloat()))
}
