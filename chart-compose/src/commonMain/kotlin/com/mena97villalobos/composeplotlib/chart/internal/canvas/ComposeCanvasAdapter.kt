package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import com.mena97villalobos.composeplotlib.core.layout.DrawCommand
import com.mena97villalobos.composeplotlib.core.layout.draw.FillRule
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeCap
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeJoin
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.ui.graphics.StrokeCap as ComposeStrokeCap
import androidx.compose.ui.graphics.StrokeJoin as ComposeStrokeJoin

// Reused across every draw call to avoid per-frame GC pressure.
// Safe because Compose Canvas rendering is synchronous on the main thread:
// each command is fully drawn before the next iteration resets these objects.
private val scratchPath = Path()
private val scratchPaint = Paint()

// Caches PathEffect conversions from StrokeStyle — dash pattern objects are constant
// per chart config, so this map stays small (typically < 5 entries per chart).
private val dashPathEffectCache = HashMap<StrokeStyle, PathEffect?>(8)

/** Maps core [DrawCommand] values to [DrawScope] and Compose [Color]/geometry. */
internal object ComposeCanvasAdapter {
    @OptIn(ExperimentalTextApi::class)
    fun DrawScope.drawPlan(
        plan: ChartLayoutPlan,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    ) {
        val chrome = renderStyles.chrome
        val drawChartContent: DrawScope.() -> Unit = {
            clipRect(
                left = plan.plotBounds.left.toFloat(),
                top = plan.plotBounds.top.toFloat(),
                right = plan.plotBounds.right.toFloat(),
                bottom = plan.plotBounds.bottom.toFloat(),
            ) {
                drawDrawCommands(plan.plotDrawCommands, textMeasurer, renderStyles)
            }

            drawDrawCommands(plan.overlayDrawCommands, textMeasurer, renderStyles)

            if (chrome.plotBorderStrokeWidthPx > 0f) {
                drawRect(
                    color = chrome.plotBorderColor,
                    topLeft = plan.plotBounds.topLeft.toComposeOffset(),
                    size =
                        Size(
                            plan.plotBounds.width.toFloat(),
                            plan.plotBounds.height.toFloat(),
                        ),
                    style = Stroke(width = chrome.plotBorderStrokeWidthPx),
                )
            }
        }

        if (chrome.clipCanvasToCanvasBounds) {
            clipRect(
                left = plan.canvasBounds.left.toFloat(),
                top = plan.canvasBounds.top.toFloat(),
                right = plan.canvasBounds.right.toFloat(),
                bottom = plan.canvasBounds.bottom.toFloat(),
            ) {
                drawChartContent()
            }
        } else {
            drawChartContent()
        }
    }

    @OptIn(ExperimentalTextApi::class)
    fun DrawScope.drawLegend(
        plan: ChartLayoutPlan,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    ) {
        if (plan.legendEntries.isEmpty()) return
        val chrome = renderStyles.chrome
        val labels = renderStyles.labels

        val drawLegendInner: DrawScope.() -> Unit = {
            val drawEntries: DrawScope.() -> Unit = {
                plan.legendEntries.forEach { entry ->
                    val r = entry.indicatorRadius.toFloat().coerceAtLeast(0.5f)
                    drawCircle(
                        color = entry.color.toComposeColor(),
                        radius = r,
                        center = entry.indicatorCenter.toComposeOffset(),
                    )
                    val textStyle = labels.legend.copy(color = entry.color.toComposeColor())
                    val layout = textMeasurer.measureChartText(entry.label, textStyle)
                    val topLeft =
                        baselineAlignedTopLeft(
                            boxTopLeft = entry.labelPosition,
                            plannerBaselineFromTop = entry.labelMetrics.firstBaselineFromTop,
                            layout = layout,
                        )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = topLeft,
                    )
                }
            }

            val bounds = plan.reservedRegions.legendBounds
            if (bounds == null) {
                drawEntries()
            } else {
                val left = bounds.left.toFloat()
                val top = bounds.top.toFloat()
                val w = bounds.width.toFloat()
                val h = bounds.height.toFloat()
                val rPx = chrome.legendCornerRadiusPx
                val legendCorner = if (rPx > 0f) CornerRadius(rPx, rPx) else CornerRadius.Zero

                val panel: DrawScope.() -> Unit = {
                    val bg = chrome.legendBackgroundColor
                    if (bg != null && bg.alpha > 0f) {
                        drawRoundRect(
                            color = bg,
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = legendCorner,
                        )
                    }
                    val borderColor = chrome.legendBorderColor
                    if (chrome.legendBorderStrokeWidthPx > 0f && borderColor != null && borderColor.alpha > 0f) {
                        drawRoundRect(
                            color = borderColor,
                            topLeft = Offset(left, top),
                            size = Size(w, h),
                            cornerRadius = legendCorner,
                            style = Stroke(width = chrome.legendBorderStrokeWidthPx),
                        )
                    }
                    drawEntries()
                }

                if (chrome.clipLegendToLegendBounds) {
                    clipRect(left = left, top = top, right = left + w, bottom = top + h, block = panel)
                } else {
                    panel()
                }
            }
        }

        if (chrome.clipCanvasToCanvasBounds) {
            clipRect(
                left = plan.canvasBounds.left.toFloat(),
                top = plan.canvasBounds.top.toFloat(),
                right = plan.canvasBounds.right.toFloat(),
                bottom = plan.canvasBounds.bottom.toFloat(),
            ) {
                drawLegendInner()
            }
        } else {
            drawLegendInner()
        }
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDrawCommands(
    commands: List<DrawCommand>,
    textMeasurer: TextMeasurer,
    renderStyles: ChartRenderStyles,
    colorFactor: Double = 1.0,
) {
    val drawScope = this
    val labels = renderStyles.labels
    val chrome = renderStyles.chrome
    val cf = colorFactor.coerceIn(0.0, 1.0)
    if (cf <= 0.0) return
    commands.forEach { command ->
        when (command) {
            is DrawCommand.Line ->
                drawLine(
                    color = command.color.multiplyAlpha(cf).toComposeColor(),
                    start = command.start.toComposeOffset(),
                    end = command.end.toComposeOffset(),
                    strokeWidth = command.strokeWidth.toFloat(),
                    cap = command.strokeStyle.toComposeStrokeCap(),
                    pathEffect = command.strokeStyle.toDashPathEffect(),
                )

            is DrawCommand.Polyline -> {
                if (command.points.size < 2) return@forEach
                val path =
                    scratchPath.apply {
                        reset()
                        val first = command.points.first()
                        moveTo(first.x.toFloat(), first.y.toFloat())
                        for (i in 1 until command.points.size) {
                            val p = command.points[i]
                            lineTo(p.x.toFloat(), p.y.toFloat())
                        }
                    }
                drawPath(
                    path = path,
                    color = command.color.multiplyAlpha(cf).toComposeColor(),
                    style =
                        Stroke(
                            width = command.strokeWidth.toFloat(),
                            cap = command.strokeStyle.toComposeStrokeCap(),
                            join = command.strokeStyle.toComposeStrokeJoin(),
                            pathEffect = command.strokeStyle.toDashPathEffect(),
                        ),
                )
            }

            is DrawCommand.Rect ->
                drawRect(
                    color = command.color.multiplyAlpha(cf).toComposeColor(),
                    topLeft = command.bounds.topLeft.toComposeOffset(),
                    size =
                        Size(
                            command.bounds.width.toFloat(),
                            command.bounds.height.toFloat(),
                        ),
                )

            is DrawCommand.Circle ->
                drawCircle(
                    color = command.color.multiplyAlpha(cf).toComposeColor(),
                    radius = command.radius.toFloat(),
                    center = command.center.toComposeOffset(),
                )

            is DrawCommand.Text -> {
                val base =
                    when {
                        command.isPieSliceLabel -> labels.pieSliceLabel
                        command.isSecondaryYAxisTick -> labels.yAxisSecondaryTick
                        command.isYAxisTick -> labels.yAxis
                        else -> labels.xAxis
                    }
                val style = base.copy(color = command.color.multiplyAlpha(cf).toComposeColor())
                val layout = textMeasurer.measureChartText(command.text, style)
                val topLeft =
                    when (val b = command.firstBaselineFromTop) {
                        null -> command.position.toComposeOffset()
                        else -> baselineAlignedTopLeft(command.position, b, layout)
                    }
                val rotationDegrees = command.rotationDegrees
                if (rotationDegrees == 0.0) {
                    drawText(
                        textLayoutResult = layout,
                        topLeft = topLeft,
                    )
                } else {
                    val pivot =
                        command.rotationPivot?.toComposeOffset()
                            ?: Offset(
                                x = topLeft.x + layout.size.width / 2f,
                                y = topLeft.y + layout.size.height / 2f,
                            )
                    rotate(
                        degrees = rotationDegrees.toFloat(),
                        pivot = pivot,
                    ) {
                        drawText(
                            textLayoutResult = layout,
                            topLeft = topLeft,
                        )
                    }
                }
            }

            is DrawCommand.Path -> {
                val path = command.segments.populatePath(scratchPath)
                when (val style = command.style) {
                    is PathDrawStyle.Stroke ->
                        drawPath(
                            path = path,
                            color = style.color.multiplyAlpha(cf).toComposeColor(),
                            style =
                                strokeStyleFrom(
                                    strokeWidth = style.strokeWidth.toFloat(),
                                    strokeStyle = style.strokeStyle,
                                ),
                        )
                    is PathDrawStyle.Fill -> {
                        path.fillType =
                            when (style.fillRule) {
                                FillRule.NonZero -> PathFillType.NonZero
                                FillRule.EvenOdd -> PathFillType.EvenOdd
                            }
                        drawPath(
                            path = path,
                            color = style.color.multiplyAlpha(cf).toComposeColor(),
                            style = Fill,
                        )
                    }
                    is PathDrawStyle.VerticalGradientFill -> {
                        path.fillType =
                            when (style.fillRule) {
                                FillRule.NonZero -> PathFillType.NonZero
                                FillRule.EvenOdd -> PathFillType.EvenOdd
                            }
                        drawPath(
                            path = path,
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            style.topColor.multiplyAlpha(cf).toComposeColor(),
                                            style.bottomColor.multiplyAlpha(cf).toComposeColor(),
                                        ),
                                    startY = style.topY.toFloat(),
                                    endY = style.bottomY.toFloat(),
                                ),
                            style = Fill,
                        )
                    }
                }
            }

            is DrawCommand.ClipRect ->
                clipRect(
                    left = command.bounds.left.toFloat(),
                    top = command.bounds.top.toFloat(),
                    right = command.bounds.right.toFloat(),
                    bottom = command.bounds.bottom.toFloat(),
                ) {
                    drawDrawCommands(command.children, textMeasurer, renderStyles, cf)
                }

            is DrawCommand.Group -> {
                val a = command.alpha.coerceIn(0.0, 1.0)
                val effective = cf * a
                if (effective <= 0.0) return@forEach
                if (a < 1.0 && a > 0.0 && chrome.layerCompositingForTranslucentGroups) {
                    drawIntoCanvas { canvas ->
                        val paint = scratchPaint.apply { alpha = a.toFloat() }
                        val bounds =
                            Rect(
                                0f,
                                0f,
                                drawContext.size.width,
                                drawContext.size.height,
                            )
                        canvas.saveLayer(bounds, paint)
                        try {
                            drawScope.drawDrawCommands(
                                command.children,
                                textMeasurer,
                                renderStyles,
                                colorFactor = cf,
                            )
                        } finally {
                            canvas.restore()
                        }
                    }
                } else {
                    drawDrawCommands(
                        command.children,
                        textMeasurer,
                        renderStyles,
                        colorFactor = effective,
                    )
                }
            }

            is DrawCommand.RoundRect -> {
                val topLeft = command.bounds.topLeft.toComposeOffset()
                val size = Size(command.bounds.width.toFloat(), command.bounds.height.toFloat())
                val radius = CornerRadius(command.cornerRadius.toFloat(), command.cornerRadius.toFloat())
                when (val style = command.style) {
                    is ShapeDrawStyle.Fill ->
                        drawRoundRect(
                            color = style.color.multiplyAlpha(cf).toComposeColor(),
                            topLeft = topLeft,
                            size = size,
                            cornerRadius = radius,
                        )
                    is ShapeDrawStyle.Stroke ->
                        drawRoundRect(
                            color = style.color.multiplyAlpha(cf).toComposeColor(),
                            topLeft = topLeft,
                            size = size,
                            cornerRadius = radius,
                            style =
                                strokeStyleFrom(
                                    strokeWidth = style.strokeWidth.toFloat(),
                                    strokeStyle = style.strokeStyle,
                                ),
                        )
                }
            }

            is DrawCommand.Arc -> {
                val inner = command.innerRadius
                val useAnnulus = inner != null && inner > 0.0 && inner < command.radius
                if (useAnnulus) {
                    val path =
                        annulusSectorPath(
                            scratchPath,
                            center = command.center,
                            outerR = command.radius,
                            innerR = inner,
                            startAngleDegrees = command.startAngleDegrees,
                            sweepAngleDegrees = command.sweepAngleDegrees,
                        )
                    when (val style = command.style) {
                        is ShapeDrawStyle.Fill ->
                            drawPath(
                                path = path,
                                color = style.color.multiplyAlpha(cf).toComposeColor(),
                                style = Fill,
                            )
                        is ShapeDrawStyle.Stroke ->
                            drawPath(
                                path = path,
                                color = style.color.multiplyAlpha(cf).toComposeColor(),
                                style =
                                    strokeStyleFrom(
                                        strokeWidth = style.strokeWidth.toFloat(),
                                        strokeStyle = style.strokeStyle,
                                    ),
                            )
                    }
                } else {
                    val r = command.radius.toFloat()
                    val topLeft =
                        Offset(
                            (command.center.x - command.radius).toFloat(),
                            (command.center.y - command.radius).toFloat(),
                        )
                    val size = Size(r * 2f, r * 2f)
                    when (val style = command.style) {
                        is ShapeDrawStyle.Fill ->
                            drawArc(
                                color = style.color.multiplyAlpha(cf).toComposeColor(),
                                startAngle = command.startAngleDegrees.toFloat(),
                                sweepAngle = command.sweepAngleDegrees.toFloat(),
                                useCenter = command.useCenter,
                                topLeft = topLeft,
                                size = size,
                            )
                        is ShapeDrawStyle.Stroke ->
                            drawArc(
                                color = style.color.multiplyAlpha(cf).toComposeColor(),
                                startAngle = command.startAngleDegrees.toFloat(),
                                sweepAngle = command.sweepAngleDegrees.toFloat(),
                                useCenter = command.useCenter,
                                topLeft = topLeft,
                                size = size,
                                style =
                                    strokeStyleFrom(
                                        strokeWidth = style.strokeWidth.toFloat(),
                                        strokeStyle = style.strokeStyle,
                                    ),
                            )
                    }
                }
            }
        }
    }
}

/** Donut slice: outer elliptical arc, radial segment, inner arc (reverse sweep), close. Angles match [DrawScope.drawArc]. */
private fun annulusSectorPath(
    path: Path,
    center: OffsetD,
    outerR: Double,
    innerR: Double,
    startAngleDegrees: Double,
    sweepAngleDegrees: Double,
): Path {
    path.reset()
    val cx = center.x.toFloat()
    val cy = center.y.toFloat()
    val ro = outerR.toFloat().coerceAtLeast(0.0001f)
    val ri = min(innerR.toFloat().coerceAtLeast(0f), ro - 0.0001f)
    val startRad = startAngleDegrees * PI / 180.0
    val endRad = (startAngleDegrees + sweepAngleDegrees) * PI / 180.0
    val x0 = cx + ro * cos(startRad).toFloat()
    val y0 = cy + ro * sin(startRad).toFloat()
    path.moveTo(x0, y0)
    val ovalOuter = Rect(cx - ro, cy - ro, cx + ro, cy + ro)
    path.arcTo(
        ovalOuter,
        startAngleDegrees.toFloat(),
        sweepAngleDegrees.toFloat(),
        forceMoveTo = false,
    )
    path.lineTo(
        cx + ri * cos(endRad).toFloat(),
        cy + ri * sin(endRad).toFloat(),
    )
    val ovalInner = Rect(cx - ri, cy - ri, cx + ri, cy + ri)
    path.arcTo(
        ovalInner,
        (startAngleDegrees + sweepAngleDegrees).toFloat(),
        (-sweepAngleDegrees).toFloat(),
        forceMoveTo = false,
    )
    path.close()
    return path
}

private fun strokeStyleFrom(
    strokeWidth: Float,
    strokeStyle: StrokeStyle?,
): Stroke =
    Stroke(
        width = strokeWidth,
        cap = strokeStyle.toComposeStrokeCap(),
        join = strokeStyle.toComposeStrokeJoin(),
        pathEffect = strokeStyle.toDashPathEffect(),
    )

private fun StrokeStyle?.toComposeStrokeCap(): ComposeStrokeCap =
    when (this?.cap) {
        null, StrokeCap.Butt -> ComposeStrokeCap.Butt
        StrokeCap.Round -> ComposeStrokeCap.Round
        StrokeCap.Square -> ComposeStrokeCap.Square
    }

private fun StrokeStyle?.toComposeStrokeJoin(): ComposeStrokeJoin =
    when (this?.join) {
        null, StrokeJoin.Miter -> ComposeStrokeJoin.Miter
        StrokeJoin.Round -> ComposeStrokeJoin.Round
        StrokeJoin.Bevel -> ComposeStrokeJoin.Bevel
    }

private fun StrokeStyle?.toDashPathEffect(): PathEffect? {
    val style = this ?: return null
    if (dashPathEffectCache.containsKey(style)) return dashPathEffectCache[style]
    val intervals = style.dashIntervals
    val effect =
        if (intervals.isNullOrEmpty()) {
            null
        } else {
            PathEffect.dashPathEffect(
                intervals = FloatArray(intervals.size) { intervals[it].toFloat() },
                phase = style.dashPhase.toFloat(),
            )
        }
    dashPathEffectCache[style] = effect
    return effect
}

private fun List<PathSegment>.populatePath(path: Path): Path {
    path.reset()
    for (segment in this) {
        when (segment) {
            is PathSegment.MoveTo -> path.moveTo(segment.point.x.toFloat(), segment.point.y.toFloat())
            is PathSegment.LineTo -> path.lineTo(segment.point.x.toFloat(), segment.point.y.toFloat())
            is PathSegment.QuadraticTo ->
                path.quadraticTo(
                    segment.control.x.toFloat(),
                    segment.control.y.toFloat(),
                    segment.end.x.toFloat(),
                    segment.end.y.toFloat(),
                )
            is PathSegment.CubicTo ->
                path.cubicTo(
                    segment.control1.x.toFloat(),
                    segment.control1.y.toFloat(),
                    segment.control2.x.toFloat(),
                    segment.control2.y.toFloat(),
                    segment.end.x.toFloat(),
                    segment.end.y.toFloat(),
                )
            PathSegment.Close -> path.close()
        }
    }
    return path
}

/** Multiplies ARGB alpha channel by [factor] (0..1). */
private fun ChartColor.multiplyAlpha(factor: Double): ChartColor {
    val f = factor.coerceIn(0.0, 1.0)
    if (f >= 1.0) return this
    val a = ((argb shr 24) and 0xFF) / 255.0
    val newA = (a * f).coerceIn(0.0, 1.0)
    val newAlphaByte = (newA * 255).toInt().coerceIn(0, 255)
    return ChartColor((argb and 0x00FFFFFF) or (newAlphaByte.toLong() shl 24))
}

internal fun OffsetD.toComposeOffset(): Offset = Offset(x.toFloat(), y.toFloat())

internal fun RectD.toComposeRect(): Rect =
    Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat(),
    )

internal fun ChartColor.toComposeColor(): Color = Color(argb)

@OptIn(ExperimentalTextApi::class)
private fun baselineAlignedTopLeft(
    boxTopLeft: OffsetD,
    plannerBaselineFromTop: Double,
    layout: TextLayoutResult,
): Offset {
    val composeBaseline =
        if (layout.lineCount > 0) {
            layout.getLineBaseline(0).toDouble()
        } else {
            layout.size.height.toDouble()
        }
    val y = boxTopLeft.y + plannerBaselineFromTop - composeBaseline
    return Offset(boxTopLeft.x.toFloat(), y.toFloat())
}
