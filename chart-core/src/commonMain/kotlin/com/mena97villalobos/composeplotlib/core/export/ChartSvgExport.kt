package com.mena97villalobos.composeplotlib.core.export

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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

/**
 * Serializes a planned [ChartLayoutPlan] to a standalone **SVG** document — uniquely cheap here because the planner
 * already emits a resolution-independent [DrawCommand] list, so no rasterization or DOM is involved. Vector output
 * scales crisply and embeds anywhere SVG is accepted.
 *
 * [ChartLayoutPlan.plotDrawCommands] are clipped to [ChartLayoutPlan.plotBounds] (matching the on-screen renderer);
 * [ChartLayoutPlan.overlayDrawCommands] (axes, labels) draw on top, unclipped.
 *
 * @param plan the layout to serialize (from any `ChartPlanner.plan*` call).
 * @param background optional page background painted behind the chart.
 * @return a complete `<svg>…</svg>` document string.
 */
fun chartLayoutPlanToSvg(
    plan: ChartLayoutPlan,
    background: ChartColor? = null,
): String {
    val width = plan.canvasBounds.width
    val height = plan.canvasBounds.height
    val defs = StringBuilder()
    val body = StringBuilder()
    val ids = SvgIdGenerator()

    if (background != null) {
        body.append("<rect x=\"0\" y=\"0\" width=\"${svgNum(width)}\" height=\"${svgNum(height)}\" ")
        body.append(fillAttrs(background)).append("/>")
    }

    val clipId = ids.next("clip")
    defs.append("<clipPath id=\"$clipId\"><rect ").append(rectAttrs(plan.plotBounds)).append("/></clipPath>")
    body.append("<g clip-path=\"url(#$clipId)\">")
    plan.plotDrawCommands.forEach { appendCommand(it, body, defs, ids) }
    body.append("</g>")
    plan.overlayDrawCommands.forEach { appendCommand(it, body, defs, ids) }

    return buildString {
        append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        append("width=\"${svgNum(width)}\" height=\"${svgNum(height)}\" ")
        append("viewBox=\"0 0 ${svgNum(width)} ${svgNum(height)}\">")
        if (defs.isNotEmpty()) append("<defs>").append(defs).append("</defs>")
        append(body)
        append("</svg>")
    }
}

private class SvgIdGenerator {
    private val counters = HashMap<String, Int>()

    fun next(prefix: String): String {
        val n = counters.getOrElse(prefix) { 0 }
        counters[prefix] = n + 1
        return "$prefix$n"
    }
}

private fun appendCommand(
    command: DrawCommand,
    body: StringBuilder,
    defs: StringBuilder,
    ids: SvgIdGenerator,
) {
    when (command) {
        is DrawCommand.Line ->
            body
                .append("<line x1=\"${svgNum(command.start.x)}\" y1=\"${svgNum(command.start.y)}\" ")
                .append("x2=\"${svgNum(command.end.x)}\" y2=\"${svgNum(command.end.y)}\" ")
                .append(strokeAttrs(command.color, command.strokeWidth, command.strokeStyle))
                .append("/>")
        is DrawCommand.Polyline ->
            body
                .append("<polyline points=\"${pointsAttr(command.points)}\" fill=\"none\" ")
                .append(strokeAttrs(command.color, command.strokeWidth, command.strokeStyle))
                .append("/>")
        is DrawCommand.Rect ->
            body
                .append("<rect ")
                .append(rectAttrs(command.bounds))
                .append(' ')
                .append(fillAttrs(command.color))
                .append("/>")
        is DrawCommand.RoundRect -> {
            body.append("<rect ").append(rectAttrs(command.bounds))
            body.append(" rx=\"${svgNum(command.cornerRadius)}\" ").append(shapeStyleAttrs(command.style)).append("/>")
        }
        is DrawCommand.Circle ->
            body
                .append("<circle cx=\"${svgNum(command.center.x)}\" cy=\"${svgNum(command.center.y)}\" ")
                .append("r=\"${svgNum(command.radius)}\" ")
                .append(fillAttrs(command.color))
                .append("/>")
        is DrawCommand.Text -> appendText(command, body)
        is DrawCommand.Path -> appendPath(command, body, defs, ids)
        is DrawCommand.Arc -> appendArc(command, body)
        is DrawCommand.ClipRect -> {
            val id = ids.next("clip")
            defs.append("<clipPath id=\"$id\"><rect ").append(rectAttrs(command.bounds)).append("/></clipPath>")
            body.append("<g clip-path=\"url(#$id)\">")
            command.children.forEach { appendCommand(it, body, defs, ids) }
            body.append("</g>")
        }
        is DrawCommand.Group -> {
            body.append("<g opacity=\"${svgNum(command.alpha)}\">")
            command.children.forEach { appendCommand(it, body, defs, ids) }
            body.append("</g>")
        }
    }
}

private fun appendText(
    command: DrawCommand.Text,
    body: StringBuilder,
) {
    val baselineY = command.position.y + (command.firstBaselineFromTop ?: DEFAULT_BASELINE_FROM_TOP)
    body.append("<text x=\"${svgNum(command.position.x)}\" y=\"${svgNum(baselineY)}\" ")
    body.append(fillAttrs(command.color))
    if (command.rotationDegrees != 0.0) {
        val pivot = command.rotationPivot ?: OffsetD(command.position.x, baselineY)
        body.append(" transform=\"rotate(${svgNum(command.rotationDegrees)} ${svgNum(pivot.x)} ${svgNum(pivot.y)})\"")
    }
    body.append('>').append(xmlEscape(command.text)).append("</text>")
}

private fun appendPath(
    command: DrawCommand.Path,
    body: StringBuilder,
    defs: StringBuilder,
    ids: SvgIdGenerator,
) {
    val d = pathData(command.segments)
    if (d.isEmpty()) return
    when (val style = command.style) {
        is PathDrawStyle.Fill ->
            body.append("<path d=\"$d\" ").append(fillAttrs(style.color, style.fillRule)).append("/>")
        is PathDrawStyle.Stroke ->
            body.append("<path d=\"$d\" fill=\"none\" ").append(strokeAttrs(style.color, style.strokeWidth, style.strokeStyle)).append("/>")
        is PathDrawStyle.VerticalGradientFill -> {
            val id = ids.next("grad")
            defs.append("<linearGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" ")
            defs.append("x1=\"0\" y1=\"${svgNum(style.topY)}\" x2=\"0\" y2=\"${svgNum(style.bottomY)}\">")
            defs.append(gradientStops(style.topColor, style.bottomColor)).append("</linearGradient>")
            body.append("<path d=\"$d\" fill=\"url(#$id)\" ").append(fillRuleAttr(style.fillRule)).append("/>")
        }
        is PathDrawStyle.LinearGradientFill -> {
            val id = ids.next("grad")
            defs.append("<linearGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" ")
            defs.append("x1=\"${svgNum(style.startX)}\" y1=\"${svgNum(style.startY)}\" ")
            defs.append("x2=\"${svgNum(style.endX)}\" y2=\"${svgNum(style.endY)}\">")
            defs.append(gradientStops(style.startColor, style.endColor)).append("</linearGradient>")
            body.append("<path d=\"$d\" fill=\"url(#$id)\" ").append(fillRuleAttr(style.fillRule)).append("/>")
        }
        is PathDrawStyle.RadialGradientFill -> {
            val id = ids.next("grad")
            defs.append("<radialGradient id=\"$id\" gradientUnits=\"userSpaceOnUse\" ")
            defs.append("cx=\"${svgNum(style.centerX)}\" cy=\"${svgNum(style.centerY)}\" r=\"${svgNum(style.radius)}\">")
            style.stops.forEach { stop ->
                defs.append("<stop offset=\"${svgNum(stop.offset)}\" stop-color=\"${stop.color.svgHex()}\" stop-opacity=\"${svgNum(stop.color.alpha / 255.0)}\"/>")
            }
            defs.append("</radialGradient>")
            body.append("<path d=\"$d\" fill=\"url(#$id)\" ").append(fillRuleAttr(style.fillRule)).append("/>")
        }
    }
}

private fun appendArc(
    command: DrawCommand.Arc,
    body: StringBuilder,
) {
    val d = arcPathData(command)
    if (d.isEmpty()) return
    body.append("<path d=\"$d\" ").append(shapeStyleAttrs(command.style)).append("/>")
}

private fun arcPathData(command: DrawCommand.Arc): String {
    val cx = command.center.x
    val cy = command.center.y
    val r = command.radius
    val start = command.startAngleDegrees
    val sweep = command.sweepAngleDegrees
    val end = start + sweep
    val outerStart = polar(cx, cy, r, start)
    val outerEnd = polar(cx, cy, r, end)
    val largeArc = if (abs(sweep) > 180.0) 1 else 0
    val sweepFlag = if (sweep >= 0.0) 1 else 0
    val inner = command.innerRadius
    return when {
        inner != null && inner > 0.0 && inner < r -> {
            // Annulus sector: outer arc forward, inner arc back.
            val innerEnd = polar(cx, cy, inner, end)
            val innerStart = polar(cx, cy, inner, start)
            val reverseSweep = if (sweepFlag == 1) 0 else 1
            "M ${svgNum(outerStart.x)} ${svgNum(outerStart.y)} " +
                "A ${svgNum(r)} ${svgNum(r)} 0 $largeArc $sweepFlag ${svgNum(outerEnd.x)} ${svgNum(outerEnd.y)} " +
                "L ${svgNum(innerEnd.x)} ${svgNum(innerEnd.y)} " +
                "A ${svgNum(inner)} ${svgNum(inner)} 0 $largeArc $reverseSweep ${svgNum(innerStart.x)} ${svgNum(innerStart.y)} Z"
        }
        command.useCenter ->
            "M ${svgNum(cx)} ${svgNum(cy)} L ${svgNum(outerStart.x)} ${svgNum(outerStart.y)} " +
                "A ${svgNum(r)} ${svgNum(r)} 0 $largeArc $sweepFlag ${svgNum(outerEnd.x)} ${svgNum(outerEnd.y)} Z"
        else ->
            "M ${svgNum(outerStart.x)} ${svgNum(outerStart.y)} " +
                "A ${svgNum(r)} ${svgNum(r)} 0 $largeArc $sweepFlag ${svgNum(outerEnd.x)} ${svgNum(outerEnd.y)}"
    }
}

private fun polar(
    cx: Double,
    cy: Double,
    r: Double,
    angleDegrees: Double,
): OffsetD {
    val radians = angleDegrees * PI_OVER_180
    return OffsetD(cx + r * cos(radians), cy + r * sin(radians))
}

private fun pathData(segments: List<PathSegment>): String {
    if (segments.isEmpty()) return ""
    val builder = StringBuilder()
    segments.forEachIndexed { index, segment ->
        if (index > 0) builder.append(' ')
        when (segment) {
            is PathSegment.MoveTo -> builder.append("M ${svgNum(segment.point.x)} ${svgNum(segment.point.y)}")
            is PathSegment.LineTo -> builder.append("L ${svgNum(segment.point.x)} ${svgNum(segment.point.y)}")
            is PathSegment.QuadraticTo ->
                builder.append("Q ${svgNum(segment.control.x)} ${svgNum(segment.control.y)} ${svgNum(segment.end.x)} ${svgNum(segment.end.y)}")
            is PathSegment.CubicTo ->
                builder.append(
                    "C ${svgNum(segment.control1.x)} ${svgNum(segment.control1.y)} " +
                        "${svgNum(segment.control2.x)} ${svgNum(segment.control2.y)} " +
                        "${svgNum(segment.end.x)} ${svgNum(segment.end.y)}",
                )
            is PathSegment.Close -> builder.append('Z')
        }
    }
    return builder.toString()
}

private fun gradientStops(
    from: ChartColor,
    to: ChartColor,
): String =
    "<stop offset=\"0\" stop-color=\"${from.svgHex()}\" stop-opacity=\"${svgNum(from.alpha / 255.0)}\"/>" +
        "<stop offset=\"1\" stop-color=\"${to.svgHex()}\" stop-opacity=\"${svgNum(to.alpha / 255.0)}\"/>"

private fun rectAttrs(bounds: RectD): String = "x=\"${svgNum(bounds.left)}\" y=\"${svgNum(bounds.top)}\" width=\"${svgNum(bounds.width)}\" height=\"${svgNum(bounds.height)}\""

private fun pointsAttr(points: List<OffsetD>): String = points.joinToString(" ") { "${svgNum(it.x)},${svgNum(it.y)}" }

private fun fillAttrs(
    color: ChartColor,
    fillRule: FillRule = FillRule.NonZero,
): String = "fill=\"${color.svgHex()}\" fill-opacity=\"${svgNum(color.alpha / 255.0)}\" ${fillRuleAttr(fillRule)}"

private fun fillRuleAttr(fillRule: FillRule): String = "fill-rule=\"${if (fillRule == FillRule.EvenOdd) "evenodd" else "nonzero"}\""

private fun strokeAttrs(
    color: ChartColor,
    strokeWidth: Double,
    strokeStyle: StrokeStyle?,
): String {
    val builder = StringBuilder()
    builder.append("stroke=\"${color.svgHex()}\" stroke-opacity=\"${svgNum(color.alpha / 255.0)}\" ")
    builder.append("stroke-width=\"${svgNum(strokeWidth)}\"")
    if (strokeStyle != null) {
        val dashes = strokeStyle.dashIntervals
        if (!dashes.isNullOrEmpty()) {
            builder.append(" stroke-dasharray=\"${dashes.joinToString(" ") { svgNum(it) }}\"")
            if (strokeStyle.dashPhase != 0.0) builder.append(" stroke-dashoffset=\"${svgNum(strokeStyle.dashPhase)}\"")
        }
        builder.append(" stroke-linecap=\"${strokeStyle.cap.svgName()}\" stroke-linejoin=\"${strokeStyle.join.svgName()}\"")
    }
    return builder.toString()
}

private fun shapeStyleAttrs(style: ShapeDrawStyle): String =
    when (style) {
        is ShapeDrawStyle.Fill -> fillAttrs(style.color)
        is ShapeDrawStyle.Stroke -> "fill=\"none\" " + strokeAttrs(style.color, style.strokeWidth, style.strokeStyle)
    }

private fun StrokeCap.svgName(): String =
    when (this) {
        StrokeCap.Butt -> "butt"
        StrokeCap.Round -> "round"
        StrokeCap.Square -> "square"
    }

private fun StrokeJoin.svgName(): String =
    when (this) {
        StrokeJoin.Miter -> "miter"
        StrokeJoin.Round -> "round"
        StrokeJoin.Bevel -> "bevel"
    }

private fun ChartColor.svgHex(): String = "#" + hex2(red) + hex2(green) + hex2(blue)

private fun hex2(channel: Int): String = channel.toString(16).padStart(2, '0')

private fun xmlEscape(text: String): String =
    buildString {
        for (ch in text) {
            when (ch) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&apos;")
                else -> append(ch)
            }
        }
    }

private fun svgNum(value: Double): String {
    if (!value.isFinite()) return "0"
    val rounded = round(value * 1000.0) / 1000.0
    val asLong = rounded.toLong()
    return if (rounded == asLong.toDouble()) asLong.toString() else rounded.toString()
}

private const val PI_OVER_180 = 3.141592653589793 / 180.0
private const val DEFAULT_BASELINE_FROM_TOP = 11.0
