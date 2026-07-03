package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** The curve a [TrendlineSpec] fits over a series' points. */
sealed interface TrendlineType {
    /** Ordinary least-squares straight line (`y = slope·x + intercept`). Supports a confidence band. */
    data object Linear : TrendlineType

    /**
     * Simple trailing moving average over [window] consecutive samples (ordered by x). [window] is coerced to at
     * least 1; a window larger than the sample count produces no line. No confidence band.
     *
     * @property window Number of consecutive samples averaged.
     */
    data class MovingAverage(
        val window: Int,
    ) : TrendlineType
}

/**
 * A fitted overlay drawn over a line/scatter chart, computed in `chart-core` from a series' points. Attach via
 * `trendlines` on `LineChartSpec` / `ScatterChartSpec`. Trendlines render above the series (and error bars) and
 * below the selection overlay, clipped to the plot.
 *
 * @property seriesId Id of the series to fit, or `null` to fit **every** visible series independently.
 * @property type The curve to fit; see [TrendlineType].
 * @property color Stroke color of the fitted line; defaults to [ChartColor.Red].
 * @property strokeWidth Stroke width in pixels.
 * @property dashIntervals Optional on/off dash lengths; `null` = solid.
 * @property confidenceBand When true and [type] is [TrendlineType.Linear], draws a shaded mean-response confidence
 * band around the line (ignored for moving averages, and requires at least 3 points).
 * @property confidenceLevel Confidence level in `(0, 1)` for the band (e.g. `0.95` for 95%).
 * @property bandColor Fill color of the confidence band; `null` derives a translucent tint of [color].
 */
data class TrendlineSpec(
    val seriesId: String? = null,
    val type: TrendlineType = TrendlineType.Linear,
    val color: ChartColor = ChartColor.Red,
    val strokeWidth: Double = 2.0,
    val dashIntervals: List<Double>? = null,
    val confidenceBand: Boolean = false,
    val confidenceLevel: Double = 0.95,
    val bandColor: ChartColor? = null,
)
