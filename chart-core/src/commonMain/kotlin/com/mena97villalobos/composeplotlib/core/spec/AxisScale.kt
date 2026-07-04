package com.mena97villalobos.composeplotlib.core.spec

/**
 * How [AxisSpec] tick **values** are chosen before [AxisSpec.labelFormatter] runs.
 *
 * [Linear] uses “nice” numeric steps. [Category] uses integer indices `0 until categories.size` aligned to the viewport.
 * [TimeEpochMillis] uses nice steps in milliseconds (values are Unix epoch ms as [Double]); pair with a time-aware
 * [AxisLabelFormatter] for human-readable labels. Optional [AxisSpec.epochMillisTickPolicy] can drop UTC weekend ticks.
 */
sealed interface AxisScale {
    /** "Nice" numeric steps chosen directly over the raw domain values (the default scale). */
    data object Linear : AxisScale

    /**
     * One tick per category at index `0 .. categories.lastIndex`.
     *
     * @property categories Ordered category labels; tick values are the integer indices into this list.
     */
    data class Category(
        val categories: List<String>,
    ) : AxisScale

    /** Nice tick steps in milliseconds; domain values are `Double` epoch milliseconds. */
    data object TimeEpochMillis : AxisScale

    /**
     * Logarithmic scale (evenly spaced decades) for **strictly positive** data spanning several orders of magnitude.
     * Ticks land on powers of [base] with optional 2..9 minor ticks. Non-positive values are treated as gaps.
     * Use [SymLog] instead when data crosses zero.
     *
     * @property base Log base; defaults to `10.0`.
     */
    data class Log(
        val base: Double = 10.0,
    ) : AxisScale

    /**
     * Symmetric-log scale: linear within `±[linearThreshold]` and logarithmic beyond, so data that crosses zero (or is
     * negative) still maps continuously. Ticks include `0`, `±linearThreshold`, and symmetric decades outward.
     *
     * @property base Log base for the outer (logarithmic) region; defaults to `10.0`.
     * @property linearThreshold Half-width of the near-zero linear region; must be `> 0`. Defaults to `1.0`.
     */
    data class SymLog(
        val base: Double = 10.0,
        val linearThreshold: Double = 1.0,
    ) : AxisScale

    /**
     * **Broken** axis: a linear scale whose [breaks] (empty domain intervals) are compressed to a small fraction
     * ([compressionFactor]) of their real length, so widely separated data clusters share one plot without a huge
     * empty gap. Ticks inside a break are dropped. Breaks should be non-overlapping and within the visible range.
     *
     * @property breaks the domain intervals to collapse.
     * @property compressionFactor how much of each break's real length to keep (`0..1`); default `0.06`.
     */
    data class Broken(
        val breaks: List<AxisBreak>,
        val compressionFactor: Double = 0.06,
    ) : AxisScale
}

/** A collapsed interval `[from, to]` of a [AxisScale.Broken] axis. */
data class AxisBreak(
    val from: Double,
    val to: Double,
)
