package com.mena97villalobos.composeplotlib.core.geometry

import com.mena97villalobos.composeplotlib.core.math.FpMath

/**
 * Clips line strips to an axis-aligned rectangle in pixel space (e.g. plot bounds).
 *
 * Output is one or more polylines when the strip leaves and re-enters the clip rect.
 */
object PolylineRectClip {
    /**
     * Clips [points] to [rect]. Returns only fragments with at least two distinct vertices
     * (after collapsing edge duplicates).
     */
    fun clipPolylineToRect(
        points: List<OffsetD>,
        rect: RectD,
    ): List<List<OffsetD>> {
        if (points.size < 2) return emptyList()
        val fragments = mutableListOf<List<OffsetD>>()
        var current: MutableList<OffsetD>? = null

        for (i in 0 until points.lastIndex) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val seg = clipSegmentToRect(p0, p1, rect) ?: continue
            val (a, b) = seg
            if (current == null) {
                current = mutableListOf(a, b)
            } else {
                val last = current.last()
                if (offsetNearlyEqual(last, a)) {
                    if (!offsetNearlyEqual(last, b)) {
                        current.add(b)
                    }
                } else {
                    finishFragment(current, fragments)
                    current = mutableListOf(a, b)
                }
            }
        }
        if (current != null) {
            finishFragment(current, fragments)
        }
        return fragments
    }

    internal fun clipSegmentToRect(
        p0: OffsetD,
        p1: OffsetD,
        rect: RectD,
    ): Pair<OffsetD, OffsetD>? {
        var u1 = 0.0
        var u2 = 1.0
        val dx = p1.x - p0.x
        val dy = p1.y - p0.y

        fun clipT(
            p: Double,
            q: Double,
        ): Boolean {
            if (FpMath.isNearlyZero(p)) {
                return q >= -FpMath.EPS
            }
            val r = q / p
            if (p < 0) {
                if (r > u2 + FpMath.EPS) return false
                if (r > u1) u1 = r
            } else {
                if (r < u1 - FpMath.EPS) return false
                if (r < u2) u2 = r
            }
            return true
        }

        if (!clipT(-dx, p0.x - rect.left)) return null
        if (!clipT(dx, rect.right - p0.x)) return null
        if (!clipT(-dy, p0.y - rect.top)) return null
        if (!clipT(dy, rect.bottom - p0.y)) return null

        if (u1 > u2 + FpMath.EPS) return null

        u1 = u1.coerceIn(0.0, 1.0)
        u2 = u2.coerceIn(0.0, 1.0)
        if (u1 > u2 + FpMath.EPS) return null

        val q0 =
            OffsetD(
                x = p0.x + u1 * dx,
                y = p0.y + u1 * dy,
            )
        val q1 =
            OffsetD(
                x = p0.x + u2 * dx,
                y = p0.y + u2 * dy,
            )
        if (offsetNearlyEqual(q0, q1)) {
            return null
        }
        return q0 to q1
    }

    private fun finishFragment(
        buf: MutableList<OffsetD>,
        out: MutableList<List<OffsetD>>,
    ) {
        val collapsed = collapseConsecutiveDuplicates(buf)
        if (collapsed.size >= 2) {
            out.add(collapsed)
        }
    }

    private fun collapseConsecutiveDuplicates(pts: List<OffsetD>): List<OffsetD> {
        if (pts.isEmpty()) return pts
        val out = ArrayList<OffsetD>(pts.size)
        out.add(pts.first())
        for (i in 1 until pts.size) {
            val p = pts[i]
            if (!offsetNearlyEqual(p, out.last())) {
                out.add(p)
            }
        }
        return out
    }

    private fun offsetNearlyEqual(
        a: OffsetD,
        b: OffsetD,
    ): Boolean = FpMath.nearlyEqual(a.x, b.x) && FpMath.nearlyEqual(a.y, b.y)
}
