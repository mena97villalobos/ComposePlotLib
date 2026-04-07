package com.mena97villalobos.composeplotlib.chart

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.abs

private const val SEP_EPS = 1e-6

/**
 * Pure layout helpers for apps that draw **multiple** marker-like overlays (e.g. annotations) in the same
 * pixel space. The stock chart selection path still uses a single tooltip; these utilities help avoid
 * overlapping axis-aligned boxes when building custom overlays.
 *
 * If [moving] does not intersect [obstacle] inflated by [gap] on all sides, returns [moving] unchanged.
 * Otherwise returns a copy of [moving] shifted by the smallest translation among moving it to the
 * right, left, above, or below the padded obstacle (whichever first clears intersection).
 */
fun nudgeAxisAlignedRectToAvoidOverlap(
    moving: RectD,
    obstacle: RectD,
    gap: Double,
): RectD {
    val g = gap.coerceAtLeast(0.0)
    val pad =
        RectD(
            left = obstacle.left - g,
            top = obstacle.top - g,
            right = obstacle.right + g,
            bottom = obstacle.bottom + g,
        )
    if (!moving.intersects(pad)) {
        return moving
    }
    val w = moving.width
    val h = moving.height
    if (w <= 0.0 || h <= 0.0) {
        return moving
    }
    val candidates =
        listOf(
            // right: fully to the right of padded obstacle (tiny gap so [RectD.intersects] does not treat edges as overlap)
            RectD(pad.right + SEP_EPS, moving.top, pad.right + SEP_EPS + w, moving.bottom),
            // left
            RectD(pad.left - w - SEP_EPS, moving.top, pad.left - SEP_EPS, moving.bottom),
            // below
            RectD(moving.left, pad.bottom + SEP_EPS, moving.right, pad.bottom + SEP_EPS + h),
            // above
            RectD(moving.left, pad.top - h - SEP_EPS, moving.right, pad.top - SEP_EPS),
        )
    return candidates.firstOrNull { !it.intersects(pad) }
        ?: candidates.minBy { rect ->
            val dx = (rect.left + rect.right) / 2.0 - (moving.left + moving.right) / 2.0
            val dy = (rect.top + rect.bottom) / 2.0 - (moving.top + moving.bottom) / 2.0
            abs(dx) + abs(dy)
        }
}

/**
 * Places a second axis-aligned box of size [width] x [height] directly under [upper], separated by [gap],
 * centered horizontally on [upper]'s center.
 */
fun stackAxisAlignedRectBelow(
    upper: RectD,
    width: Double,
    height: Double,
    gap: Double,
): RectD {
    val g = gap.coerceAtLeast(0.0)
    val w = width.coerceAtLeast(0.0)
    val h = height.coerceAtLeast(0.0)
    val cx = upper.left + upper.width / 2.0
    val left = cx - w / 2.0
    val top = upper.bottom + g
    return RectD(left, top, left + w, top + h)
}
