package com.mena97villalobos.composeplotlib.core.viewport

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * Visible data window: independent [ClosedRangeD] for x and y in **domain** coordinates.
 */
data class ChartViewport(
    val xRange: ClosedRangeD,
    val yRange: ClosedRangeD,
)

/** Expands near–zero-span axes so transforms and ticks behave sensibly. */
fun ChartViewport.normalizedNonDegenerate(pad: Double = FpMath.DEFAULT_DEGENERATE_PAD): ChartViewport =
    ChartViewport(
        xRange = xRange.expandedIfDegenerate(pad),
        yRange = yRange.expandedIfDegenerate(pad),
    )

/** Applies [ViewportAxisPolicy] by copying ranges from [domain] for locked axes. */
fun ChartViewport.withAxisPolicy(
    domain: ChartViewport,
    policy: ViewportAxisPolicy,
): ChartViewport =
    copy(
        xRange = if (policy.lockXToDomain) domain.xRange else xRange,
        yRange = if (policy.lockYToDomain) domain.yRange else yRange,
    )
