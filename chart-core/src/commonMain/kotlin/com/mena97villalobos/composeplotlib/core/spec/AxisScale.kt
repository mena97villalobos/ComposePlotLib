package com.mena97villalobos.composeplotlib.core.spec

/**
 * How [AxisSpec] tick **values** are chosen before [AxisSpec.labelFormatter] runs.
 *
 * [Linear] uses “nice” numeric steps. [Category] uses integer indices `0 until categories.size` aligned to the viewport.
 * [TimeEpochMillis] uses nice steps in milliseconds (values are Unix epoch ms as [Double]); pair with a time-aware
 * [AxisLabelFormatter] for human-readable labels. Optional [AxisSpec.epochMillisTickPolicy] can drop UTC weekend ticks.
 */
sealed interface AxisScale {
    data object Linear : AxisScale

    /** One tick per category at index `0 .. categories.lastIndex`. */
    data class Category(val categories: List<String>) : AxisScale

    /** Nice tick steps in milliseconds; domain values are `Double` epoch milliseconds. */
    data object TimeEpochMillis : AxisScale
}
