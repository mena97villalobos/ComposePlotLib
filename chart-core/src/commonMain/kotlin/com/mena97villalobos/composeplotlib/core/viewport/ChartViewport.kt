package com.mena97villalobos.composeplotlib.core.viewport

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * Visible data window: independent [ClosedRangeD] for x and y in **domain** coordinates.
 *
 * @property xRange Visible interval on the x axis, in domain units.
 * @property yRange Visible interval on the y axis, in domain units.
 */
data class ChartViewport(
    val xRange: ClosedRangeD,
    val yRange: ClosedRangeD,
)

/**
 * Expands near–zero-span axes so transforms and ticks behave sensibly, via [ClosedRangeD.expandedIfDegenerate].
 *
 * @param pad Minimum span applied to a degenerate (point) axis; defaults to [FpMath.DEFAULT_DEGENERATE_PAD].
 * @return A copy with any degenerate axis widened; non-degenerate axes are returned unchanged.
 */
fun ChartViewport.normalizedNonDegenerate(pad: Double = FpMath.DEFAULT_DEGENERATE_PAD): ChartViewport =
    ChartViewport(
        xRange = xRange.expandedIfDegenerate(pad),
        yRange = yRange.expandedIfDegenerate(pad),
    )

/**
 * Applies [ViewportAxisPolicy] by copying ranges from [domain] for locked axes.
 *
 * @param domain Full-extent viewport whose axis ranges replace this viewport's ranges on locked axes.
 * @param policy Which axes are pinned to [domain]; unlocked axes keep this viewport's ranges.
 * @return A copy where each axis locked by [policy] is replaced with the matching [domain] axis.
 */
fun ChartViewport.withAxisPolicy(
    domain: ChartViewport,
    policy: ViewportAxisPolicy,
): ChartViewport =
    copy(
        xRange = if (policy.lockXToDomain) domain.xRange else xRange,
        yRange = if (policy.lockYToDomain) domain.yRange else yRange,
    )
