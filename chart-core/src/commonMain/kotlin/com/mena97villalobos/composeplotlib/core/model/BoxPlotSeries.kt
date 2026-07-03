package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.BoxPlotStyle
import kotlin.math.floor

/**
 * One box-and-whisker summary at category position [x] (plotted on chart X). Values are in **domain** space on the
 * value (Y) axis. Sensible geometry expects `min ≤ q1 ≤ median ≤ q3 ≤ max` but this is not enforced.
 *
 * Build directly, or from raw samples with [fromSamples] (Tukey quartiles + 1.5·IQR whiskers + outliers).
 *
 * @property x Category position along the X axis, in domain space.
 * @property min Lower whisker end (smallest non-outlier).
 * @property q1 First quartile — bottom of the box (25th percentile).
 * @property median Median line inside the box (50th percentile).
 * @property q3 Third quartile — top of the box (75th percentile).
 * @property max Upper whisker end (largest non-outlier).
 * @property outliers Individual outlier values drawn as points beyond the whiskers.
 */
data class BoxPlotEntry(
    val x: Double,
    val min: Double,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val max: Double,
    val outliers: List<Double> = emptyList(),
) {
    companion object {
        /**
         * Summarizes raw [values] into a [BoxPlotEntry] at [x] using linear-interpolation quartiles (the common
         * "type 7" / spreadsheet definition) and Tukey whiskers: whiskers reach the most extreme values still within
         * `[q1 − k·IQR, q3 + k·IQR]` (k = [whiskerMultiplier]); values beyond become [outliers]. Non-finite values are
         * ignored. Returns `null` when no finite values remain.
         */
        fun fromSamples(
            x: Double,
            values: List<Double>,
            whiskerMultiplier: Double = 1.5,
        ): BoxPlotEntry? {
            val sorted = values.filter { it.isFinite() }.sorted()
            if (sorted.isEmpty()) return null
            val q1 = percentile(sorted, 0.25)
            val median = percentile(sorted, 0.5)
            val q3 = percentile(sorted, 0.75)
            val iqr = q3 - q1
            val lowerFence = q1 - whiskerMultiplier * iqr
            val upperFence = q3 + whiskerMultiplier * iqr
            val whiskerLow = sorted.firstOrNull { it >= lowerFence } ?: sorted.first()
            val whiskerHigh = sorted.lastOrNull { it <= upperFence } ?: sorted.last()
            val outliers = sorted.filter { it < whiskerLow || it > whiskerHigh }
            return BoxPlotEntry(
                x = x,
                min = whiskerLow,
                q1 = q1,
                median = median,
                q3 = q3,
                max = whiskerHigh,
                outliers = outliers,
            )
        }

        /** Linear-interpolation percentile ([p] in `0..1`) over an ascending [sorted] non-empty list. */
        private fun percentile(
            sorted: List<Double>,
            p: Double,
        ): Double {
            if (sorted.size == 1) return sorted[0]
            val h = (sorted.size - 1) * p.coerceIn(0.0, 1.0)
            val lower = floor(h).toInt()
            val frac = h - lower
            if (lower + 1 >= sorted.size) return sorted[sorted.size - 1]
            return sorted[lower] + frac * (sorted[lower + 1] - sorted[lower])
        }
    }
}

/**
 * A box-and-whisker series (one row in the legend, one [BoxPlotStyle]).
 *
 * @property id Used for selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 * @property label Human-readable name shown in the legend for this series.
 * @property entries Ordered box summaries; see [BoxPlotEntry].
 * @property style Box / whisker / median / outlier appearance; defaults to [BoxPlotStyle].
 */
data class BoxPlotSeries(
    val id: String,
    val label: String,
    val entries: List<BoxPlotEntry>,
    val style: BoxPlotStyle = BoxPlotStyle(),
)
