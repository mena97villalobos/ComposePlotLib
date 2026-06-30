package com.mena97villalobos.composeplotlib.core.range

import com.mena97villalobos.composeplotlib.core.math.FpMath
import kotlin.math.max
import kotlin.math.min

/**
 * Inclusive numeric interval [start, endInclusive] with helpers used by viewport and transforms.
 */
data class ClosedRangeD(
    val start: Double,
    val endInclusive: Double,
) {
    init {
        // NOTE: non-finite bounds (±Infinity) are intentionally permitted here. ViewportState builds
        // ranges from possibly-corrupted input and repairs them to a canonical domain downstream, so this
        // value type must stay permissive. Non-finite *data* coordinates are filtered upstream in
        // `defaultViewport(...)` (chart-compose) instead of being rejected here.
        require(start <= endInclusive) {
            "ClosedRangeD start must be <= endInclusive; got [start=$start, endInclusive=$endInclusive]."
        }
    }

    val span: Double get() = endInclusive - start
    val midpoint: Double get() = start + span / 2.0

    fun clamp(value: Double): Double = value.coerceIn(start, endInclusive)

    fun intersects(other: ClosedRangeD): Boolean = start <= other.endInclusive && other.start <= endInclusive

    /** Inclusive intersection, or `null` if ranges do not overlap. */
    fun intersection(other: ClosedRangeD): ClosedRangeD? {
        val s = max(start, other.start)
        val e = min(endInclusive, other.endInclusive)
        if (s > e + FpMath.EPS_SPAN) return null
        val lo = min(s, e)
        val hi = max(s, e)
        return ClosedRangeD(lo, hi)
    }

    fun translate(delta: Double): ClosedRangeD =
        ClosedRangeD(
            start = start + delta,
            endInclusive = endInclusive + delta,
        )

    fun scaleAround(
        pivot: Double,
        scale: Double,
    ): ClosedRangeD {
        val safeScale = FpMath.sanitizeZoomScale(scale)
        val newStart = pivot + (start - pivot) / safeScale
        val newEnd = pivot + (endInclusive - pivot) / safeScale
        return ClosedRangeD(
            start = min(newStart, newEnd),
            endInclusive = max(newStart, newEnd),
        )
    }

    /**
     * If this interval is (near) a single point, expands it symmetrically so [span] is at least [pad].
     * Otherwise returns `this`.
     */
    fun expandedIfDegenerate(pad: Double = FpMath.DEFAULT_DEGENERATE_PAD): ClosedRangeD {
        if (!FpMath.isDegenerateSpan(span)) return this
        val c = midpoint
        val half = max(pad / 2.0, FpMath.EPS_SPAN)
        return ClosedRangeD(c - half, c + half)
    }
}
