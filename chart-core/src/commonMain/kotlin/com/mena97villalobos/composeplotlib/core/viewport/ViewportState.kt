package com.mena97villalobos.composeplotlib.core.viewport

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * Pure viewport transform state: pan, zoom, and clamping against a **domain** [ChartViewport].
 *
 * The UI layer typically mirrors this model with a fixed domain captured at construction. Every transform takes the
 * `domain` explicitly rather than storing it, so callers stay the single source of truth for the data extent.
 *
 * @property viewport The current visible window in domain coordinates.
 * @property constraints Per-axis span limits applied on every transform; defaults to an unconstrained
 * [ViewportConstraints].
 */
data class ViewportState(
    val viewport: ChartViewport,
    val constraints: ViewportConstraints = ViewportConstraints(),
) {
    /**
     * Clamps [nextViewport] into [domain] and applies [constraints].
     *
     * @param nextViewport Requested visible window; sanitized, span-limited, and confined to [domain].
     * @param domain Full data extent that bounds the result.
     * @return A copy with [viewport] set to the clamped window.
     */
    fun withViewport(
        nextViewport: ChartViewport,
        domain: ChartViewport,
    ): ViewportState {
        val dom = domain.canonicalDomain()
        val next = nextViewport.withSanitizedAxes(dom).coerceToDomain(dom, constraints)
        return copy(viewport = next)
    }

    /**
     * Pans in domain units, then clamps to [domain].
     *
     * @param deltaX Horizontal shift in domain units; non-finite values are treated as `0`.
     * @param deltaY Vertical shift in domain units; non-finite values are treated as `0`.
     * @param domain Full data extent that bounds the result.
     * @return A copy with [viewport] translated and clamped.
     */
    fun pan(
        deltaX: Double,
        deltaY: Double,
        domain: ChartViewport,
    ): ViewportState {
        val dom = domain.canonicalDomain()
        val v = viewport.withSanitizedAxes(dom)
        val dx = if (deltaX.isFinite()) deltaX else 0.0
        val dy = if (deltaY.isFinite()) deltaY else 0.0
        val next =
            ChartViewport(
                xRange = v.xRange.translate(dx),
                yRange = v.yRange.translate(dy),
            ).coerceToDomain(dom, constraints)

        return copy(viewport = next)
    }

    /**
     * Zooms each axis around a domain-space pivot. [scaleX]/[scaleY] greater than `1` zoom **in**
     * (smaller visible span). Results are span-clamped and fit inside [domain].
     *
     * @param scaleX X-axis zoom factor; > `1` zooms in, < `1` zooms out. Sanitized against non-finite/zero values.
     * @param scaleY Y-axis zoom factor; > `1` zooms in, < `1` zooms out. Sanitized against non-finite/zero values.
     * @param pivotX Domain-space x the zoom is anchored on; non-finite falls back to the current x midpoint.
     * @param pivotY Domain-space y the zoom is anchored on; non-finite falls back to the current y midpoint.
     * @param domain Full data extent that bounds the result.
     * @return A copy with [viewport] scaled around the pivot, span-clamped, and clamped to [domain].
     */
    fun zoom(
        scaleX: Double,
        scaleY: Double,
        pivotX: Double,
        pivotY: Double,
        domain: ChartViewport,
    ): ViewportState {
        val dom = domain.canonicalDomain()
        val v = viewport.withSanitizedAxes(dom)
        val eff = constraints.effectiveAgainst(dom)
        val sx = FpMath.sanitizeZoomScale(scaleX)
        val sy = FpMath.sanitizeZoomScale(scaleY)
        val px = if (pivotX.isFinite()) pivotX else v.xRange.midpoint
        val py = if (pivotY.isFinite()) pivotY else v.yRange.midpoint
        val nextX =
            v.xRange.scaleAround(px, sx).coerceSpan(
                minSpan = eff.minXSpan,
                maxSpan = eff.maxXSpan,
                pivot = px,
            )
        val nextY =
            v.yRange.scaleAround(py, sy).coerceSpan(
                minSpan = eff.minYSpan,
                maxSpan = eff.maxYSpan,
                pivot = py,
            )

        return copy(
            viewport = ChartViewport(nextX, nextY).coerceToDomain(dom, constraints),
        )
    }
}

/** Per-axis span limits that are simultaneously satisfiable with `domain` (cannot zoom wider than an axis domain). */
private data class EffectiveViewportConstraints(
    val minXSpan: Double,
    val maxXSpan: Double,
    val minYSpan: Double,
    val maxYSpan: Double,
)

private fun ViewportConstraints.effectiveAgainst(domain: ChartViewport): EffectiveViewportConstraints {
    val dx = domain.xRange.span
    val dy = domain.yRange.span
    val minX = effectiveMinSpan(minXSpan, dx)
    val maxX = effectiveMaxSpan(maxXSpan, dx, minX)
    val minY = effectiveMinSpan(minYSpan, dy)
    val maxY = effectiveMaxSpan(maxYSpan, dy, minY)
    return EffectiveViewportConstraints(minX, maxX, minY, maxY)
}

private fun effectiveMinSpan(
    requested: Double,
    domainSpan: Double,
): Double {
    val minS =
        if (requested.isFinite() && requested > 0.0) {
            requested.coerceAtLeast(FpMath.EPS_SPAN)
        } else {
            FpMath.EPS_SPAN
        }
    if (!domainSpan.isFinite() || domainSpan <= FpMath.EPS_SPAN) {
        return minS.coerceAtMost(FpMath.EPS_SPAN)
    }
    return minOf(minS, domainSpan)
}

private fun effectiveMaxSpan(
    requested: Double,
    domainSpan: Double,
    effectiveMin: Double,
): Double {
    val maxS =
        when {
            !requested.isFinite() -> Double.POSITIVE_INFINITY
            requested < effectiveMin -> effectiveMin
            else -> requested
        }
    if (!domainSpan.isFinite() || domainSpan <= FpMath.EPS_SPAN) {
        return maxS
    }
    return minOf(maxS, domainSpan)
}

private fun ChartViewport.coerceToDomain(
    domain: ChartViewport,
    constraints: ViewportConstraints,
): ChartViewport {
    val dom = domain.canonicalDomain()
    val eff = constraints.effectiveAgainst(dom)
    val xRange = xRange.coerceSanitizedSpan(eff.minXSpan, eff.maxXSpan, dom.xRange)
    val yRange = yRange.coerceSanitizedSpan(eff.minYSpan, eff.maxYSpan, dom.yRange)

    return ChartViewport(
        xRange = xRange.coerceWithin(dom.xRange),
        yRange = yRange.coerceWithin(dom.yRange),
    )
}

private fun ClosedRangeD.coerceSpan(
    minSpan: Double,
    maxSpan: Double,
    pivot: Double,
): ClosedRangeD {
    if (!pivot.isFinite()) return this
    val minS =
        if (minSpan.isFinite() && minSpan > 0.0) {
            minSpan.coerceAtLeast(FpMath.EPS_SPAN)
        } else {
            FpMath.EPS_SPAN
        }
    val maxS =
        when {
            !maxSpan.isFinite() -> Double.POSITIVE_INFINITY
            maxSpan < minS -> minS
            else -> maxSpan
        }
    val boundedSpan = span.coerceIn(minS, maxS)
    if (!boundedSpan.isFinite() || boundedSpan <= 0.0) {
        return ClosedRangeD(pivot - minS / 2.0, pivot + minS / 2.0)
    }
    val halfSpan = boundedSpan / 2.0
    return ClosedRangeD(
        start = pivot - halfSpan,
        endInclusive = pivot + halfSpan,
    )
}

private fun ClosedRangeD.coerceSanitizedSpan(
    minSpan: Double,
    maxSpan: Double,
    domainAxis: ClosedRangeD,
): ClosedRangeD {
    val rawPivot = if (midpoint.isFinite()) midpoint else domainAxis.midpoint
    val safePivot = if (rawPivot.isFinite()) rawPivot else 0.0
    return coerceSpan(minSpan, maxSpan, safePivot)
}

private fun ClosedRangeD.coerceWithin(bounds: ClosedRangeD): ClosedRangeD {
    val b = bounds.canonicalViewportAxis(ClosedRangeD(0.0, 1.0))
    val bSpan = b.span
    if (!bSpan.isFinite() || bSpan <= FpMath.EPS_SPAN) {
        return ClosedRangeD(b.start, b.endInclusive)
    }
    val s = span
    if (!s.isFinite()) {
        return ClosedRangeD(b.start, b.endInclusive)
    }
    if (s >= bSpan) {
        return b
    }

    return when {
        start < b.start -> ClosedRangeD(b.start, b.start + s)
        endInclusive > b.endInclusive -> ClosedRangeD(b.endInclusive - s, b.endInclusive)
        else -> this
    }
}
