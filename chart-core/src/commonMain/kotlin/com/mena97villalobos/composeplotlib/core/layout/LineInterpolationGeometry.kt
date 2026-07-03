package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.spec.LineInterpolation
import kotlin.math.sqrt

/**
 * Inserts step vertices into [points] for [LineInterpolation.StepBefore] / [LineInterpolation.StepAfter] /
 * [LineInterpolation.StepMiddle]. Returns [points] unchanged for any non-step mode or fewer than 2 points. The
 * result is a polyline (usable for both stroke and area fill).
 */
internal fun steppedLinePoints(
    points: List<OffsetD>,
    mode: LineInterpolation,
): List<OffsetD> {
    if (points.size < 2) return points
    val out = ArrayList<OffsetD>(points.size * 3)
    out += points[0]
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        when (mode) {
            LineInterpolation.StepBefore -> {
                out += OffsetD(a.x, b.y)
                out += b
            }
            LineInterpolation.StepAfter -> {
                out += OffsetD(b.x, a.y)
                out += b
            }
            LineInterpolation.StepMiddle -> {
                val midX = (a.x + b.x) / 2.0
                out += OffsetD(midX, a.y)
                out += OffsetD(midX, b.y)
                out += b
            }
            else -> out += b
        }
    }
    return out
}

/**
 * Builds a Fritsch–Carlson monotone cubic Hermite path through [points] (assumed ascending in x): a [PathSegment.MoveTo]
 * followed by [PathSegment.CubicTo] segments. Falls back to a straight [PathSegment.LineTo] across any non-increasing x
 * pair. The curve preserves monotonicity between samples (no overshoot).
 */
internal fun monotoneCubicSegments(points: List<OffsetD>): List<PathSegment> {
    val n = points.size
    if (n == 0) return emptyList()
    if (n == 1) return listOf(PathSegment.MoveTo(points[0]))
    val tangents = monotoneTangents(points)
    val segments = ArrayList<PathSegment>(n)
    segments += PathSegment.MoveTo(points[0])
    for (i in 0 until n - 1) {
        val p0 = points[i]
        val p1 = points[i + 1]
        val dx = p1.x - p0.x
        if (dx <= 0.0) {
            segments += PathSegment.LineTo(p1)
            continue
        }
        val control1 = OffsetD(p0.x + dx / 3.0, p0.y + tangents[i] * dx / 3.0)
        val control2 = OffsetD(p1.x - dx / 3.0, p1.y - tangents[i + 1] * dx / 3.0)
        segments += PathSegment.CubicTo(control1, control2, p1)
    }
    return segments
}

/**
 * Wraps [topEdge] (a monotone-cubic top edge, `MoveTo` + `CubicTo`s) into a closed area-fill path down to
 * [baselineY], spanning the first and last point x. Returns null if fewer than 2 points.
 */
internal fun monotoneCubicAreaSegments(
    points: List<OffsetD>,
    topEdge: List<PathSegment>,
    baselineY: Double,
): List<PathSegment>? {
    if (points.size < 2 || topEdge.isEmpty()) return null
    val segments = ArrayList<PathSegment>(topEdge.size + 3)
    segments += PathSegment.MoveTo(OffsetD(points.first().x, baselineY))
    segments += PathSegment.LineTo(points.first())
    for (i in 1 until topEdge.size) segments += topEdge[i] // skip the top edge's leading MoveTo
    segments += PathSegment.LineTo(OffsetD(points.last().x, baselineY))
    segments += PathSegment.Close
    return segments
}

/** Fritsch–Carlson monotone tangents (slope per point) for a monotone cubic Hermite fit over [points]. */
private fun monotoneTangents(points: List<OffsetD>): DoubleArray {
    val n = points.size
    val tangents = DoubleArray(n)
    if (n < 2) return tangents
    val secants = DoubleArray(n - 1)
    for (i in 0 until n - 1) {
        val dx = points[i + 1].x - points[i].x
        secants[i] = if (dx != 0.0) (points[i + 1].y - points[i].y) / dx else 0.0
    }
    tangents[0] = secants[0]
    tangents[n - 1] = secants[n - 2]
    for (i in 1 until n - 1) {
        tangents[i] =
            if (secants[i - 1] * secants[i] <= 0.0) 0.0 else (secants[i - 1] + secants[i]) / 2.0
    }
    // Fritsch–Carlson clamp to preserve monotonicity (no overshoot).
    for (i in 0 until n - 1) {
        val d = secants[i]
        if (d == 0.0) {
            tangents[i] = 0.0
            tangents[i + 1] = 0.0
        } else {
            val a = tangents[i] / d
            val b = tangents[i + 1] / d
            val h = a * a + b * b
            if (h > 9.0) {
                val t = 3.0 / sqrt(h)
                tangents[i] = t * a * d
                tangents[i + 1] = t * b * d
            }
        }
    }
    return tangents
}
