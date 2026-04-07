package com.mena97villalobos.composeplotlib.core.viewport

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import kotlin.math.max
import kotlin.math.min

/**
 * Viewport-only range hygiene: finite, ordered axes and safe fallbacks for clamping.
 * Keeps degenerate (point) domains after normalization.
 */
internal fun ClosedRangeD.canonicalViewportAxis(fallback: ClosedRangeD): ClosedRangeD {
    if (!start.isFinite() || !endInclusive.isFinite()) {
        if (!fallback.start.isFinite() || !fallback.endInclusive.isFinite()) {
            return ClosedRangeD(0.0, 1.0)
        }
        val lo = min(fallback.start, fallback.endInclusive)
        val hi = max(fallback.start, fallback.endInclusive)
        return ClosedRangeD(lo, hi)
    }
    val lo = min(start, endInclusive)
    val hi = max(start, endInclusive)
    return ClosedRangeD(lo, hi)
}

internal fun ChartViewport.canonicalDomain(): ChartViewport =
    ChartViewport(
        xRange = xRange.canonicalViewportAxis(ClosedRangeD(0.0, 1.0)),
        yRange = yRange.canonicalViewportAxis(ClosedRangeD(0.0, 1.0)),
    )

/** Replaces invalid axes with the corresponding [domain] axis (after [canonicalDomain]). */
internal fun ChartViewport.withSanitizedAxes(domain: ChartViewport): ChartViewport {
    val domX = domain.xRange
    val domY = domain.yRange
    return ChartViewport(
        xRange = xRange.canonicalViewportAxis(domX),
        yRange = yRange.canonicalViewportAxis(domY),
    )
}
