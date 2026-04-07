package com.mena97villalobos.composeplotlib.core.math

import kotlin.math.abs
import kotlin.math.max

/**
 * Small numeric helpers for chart math: degenerate data, gesture scales, and range hygiene.
 *
 * **Tolerance choice**
 * - [EPS]: pointwise comparisons (clip edges, polygon tests, “same tick value”) where coordinates
 *   share a modest magnitude.
 * - [EPS_SPAN]: axis **span** length (viewport width/height in domain units, range length). Use
 *   [isDegenerateSpan] instead of comparing to [EPS_SPAN] directly so “zero-length axis” is
 *   consistent across [com.mena97villalobos.composeplotlib.core.range.ClosedRangeD], transforms, and viewport code.
 * - Custom `eps` parameters: reserve for algorithm-specific thresholds (e.g. pie angle bands).
 */
object FpMath {
    /** Generic tolerance for comparing domain values that should be “equal”. */
    const val EPS: Double = 1e-9

    /** Treat spans smaller than this as zero for viewport / transform decisions. */
    const val EPS_SPAN: Double = 1e-12

    /** Default relative tolerance for [nearlyEqualRel] when comparing large-magnitude values. */
    const val REL_EPS: Double = 1e-12

    /** Default padding when expanding a point-sized domain to a drawable range (matches historic +1 behavior). */
    const val DEFAULT_DEGENERATE_PAD: Double = 1.0

    /** Clamp pinch-zoom multipliers to avoid overflow or non-finite intermediate ranges. */
    const val MIN_ZOOM_SCALE: Double = 1e-9

    const val MAX_ZOOM_SCALE: Double = 1e9

    fun isFinite(v: Double): Boolean = v.isFinite()

    fun nearlyEqual(
        a: Double,
        b: Double,
        eps: Double = EPS,
    ): Boolean = a == b || abs(a - b) <= eps

    fun isNearlyZero(
        v: Double,
        eps: Double = EPS,
    ): Boolean = abs(v) <= eps

    /**
     * `true` if [span] should be treated as a zero-length axis (non-finite or within [EPS_SPAN] of zero).
     * Use for [com.mena97villalobos.composeplotlib.core.range.ClosedRangeD] spans and pixel extents, consistent with
     * [com.mena97villalobos.composeplotlib.core.range.ClosedRangeD.expandedIfDegenerate].
     */
    fun isDegenerateSpan(span: Double): Boolean = !span.isFinite() || abs(span) <= EPS_SPAN

    /**
     * Mixed relative/absolute equality: `|a - b| ≤ absTol + relTol * max(|a|, |b|)`.
     * Prefer over [nearlyEqual] when [a] and [b] may be large (tick endpoints, domain extents).
     */
    fun nearlyEqualRel(
        a: Double,
        b: Double,
        relTol: Double = REL_EPS,
        absTol: Double = EPS,
    ): Boolean {
        if (a == b) return true
        if (!a.isFinite() || !b.isFinite()) return false
        val diff = abs(a - b)
        return diff <= absTol + relTol * max(abs(a), abs(b))
    }

    /** Ensures a zoom scale is positive finite; invalid values become [fallback]. */
    fun sanitizeZoomScale(
        scale: Double,
        fallback: Double = 1.0,
    ): Double {
        if (!scale.isFinite() || scale <= 0.0) return fallback
        return scale.coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
    }
}
