package com.mena97villalobos.composeplotlib.core.spec

/**
 * How aggressively tick **values** are thinned after a plot span is known (smaller lists ⇒ fewer grid lines).
 * Label overlap is still handled separately via [AxisSpec.minHorizontalTickLabelGapPx] /
 * [AxisSpec.minVerticalTickLabelGapPx].
 */
sealed interface AxisTickDensity {
    /**
     * Target at most `floor(spanPx / [AxisSpec.defaultDensitySpacingPx])` ticks (minimum 2), bounded by [AxisSpec.tickCount].
     */
    data object Auto : AxisTickDensity

    /** Hard cap on the number of tick values (after niceness / category expansion). */
    data class MaxTicks(val max: Int) : AxisTickDensity

    /** Target tick count from plot span and a minimum pixel spacing between consecutive tick positions. */
    data class MinSpacingPx(val spacingPx: Double) : AxisTickDensity
}
