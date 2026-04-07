package com.mena97villalobos.composeplotlib.core.geometry

import com.mena97villalobos.composeplotlib.core.math.FpMath
import kotlin.math.max
import kotlin.math.min

/** Axis-aligned rectangle in double pixel or layout units ([left] ≤ [right], [top] ≤ [bottom]). */
data class RectD(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    val width: Double get() = (right - left).coerceAtLeast(0.0)
    val height: Double get() = (bottom - top).coerceAtLeast(0.0)
    val topLeft: OffsetD get() = OffsetD(left, top)
    val center: OffsetD get() = OffsetD(left + width / 2.0, top + height / 2.0)

    fun contains(offset: OffsetD): Boolean {
        return offset.x in left..right && offset.y in top..bottom
    }

    /**
     * True if the closed disc centered at [center] with radius [radius] intersects this rectangle
     * (including discs whose center lies outside but overlap an edge or corner).
     */
    fun intersectsDisc(
        center: OffsetD,
        radius: Double,
    ): Boolean {
        val r = radius.coerceAtLeast(0.0)
        if (!center.x.isFinite() || !center.y.isFinite() || !r.isFinite()) {
            return false
        }
        val x0 = min(left, right)
        val x1 = max(left, right)
        val y0 = min(top, bottom)
        val y1 = max(top, bottom)
        if (r <= FpMath.EPS) {
            return center.x >= x0 - FpMath.EPS && center.x <= x1 + FpMath.EPS &&
                center.y >= y0 - FpMath.EPS && center.y <= y1 + FpMath.EPS
        }
        val closestX = center.x.coerceIn(x0, x1)
        val closestY = center.y.coerceIn(y0, y1)
        val dx = center.x - closestX
        val dy = center.y - closestY
        return dx * dx + dy * dy <= r * r + FpMath.EPS
    }

    fun intersects(other: RectD): Boolean = left <= other.right && right >= other.left && top <= other.bottom && bottom >= other.top

    fun inset(insets: InsetsD): RectD =
        RectD(
            left = left + insets.left,
            top = top + insets.top,
            right = right - insets.right,
            bottom = bottom - insets.bottom,
        )

    companion object {
        fun fromSize(size: SizeD): RectD =
            RectD(
                left = 0.0,
                top = 0.0,
                right = size.width,
                bottom = size.height,
            )
    }
}
