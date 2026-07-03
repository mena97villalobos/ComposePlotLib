package com.mena97villalobos.composeplotlib.core.math

import kotlin.math.sqrt

/**
 * Ordinary least-squares linear regression: fits `y = slope · x + intercept` to paired samples. KMP-safe (pure
 * `kotlin.math`). Call [fit] to obtain a [Fit], then [Fit.predict] / [Fit.meanResponseStandardError].
 *
 * Used by declarative trendline overlays (`TrendlineSpec`) but also public for standalone use.
 */
object LinearRegression {
    /**
     * Result of an OLS fit over `n` finite samples.
     *
     * @property slope Best-fit slope.
     * @property intercept Best-fit y-intercept.
     * @property n Number of finite samples used.
     * @property meanX Mean of the x samples.
     * @property sumSquaredX Sum of squared x deviations, `Σ(x − meanX)²` (a.k.a. `Sxx`).
     * @property residualStandardDeviation `√(SSE / (n − 2))`; `NaN` when `n < 3` (not estimable).
     * @property rSquared Coefficient of determination in `0..1`, or `NaN` when the response has zero variance.
     */
    data class Fit(
        val slope: Double,
        val intercept: Double,
        val n: Int,
        val meanX: Double,
        val sumSquaredX: Double,
        val residualStandardDeviation: Double,
        val rSquared: Double,
    ) {
        /** Fitted value `ŷ` at [x]. */
        fun predict(x: Double): Double = slope * x + intercept

        /**
         * Standard error of the mean response at [x], `s · √(1/n + (x − meanX)² / Sxx)`. Returns `NaN` when the fit
         * cannot support inference (`n < 3` or degenerate x). Multiply by a z / t multiplier for a confidence band.
         */
        fun meanResponseStandardError(x: Double): Double {
            if (n < 3 || !residualStandardDeviation.isFinite() || sumSquaredX <= 0.0) return Double.NaN
            val dx = x - meanX
            return residualStandardDeviation * sqrt(1.0 / n + dx * dx / sumSquaredX)
        }
    }

    /**
     * Fits [xs] against [ys] (paired by index). Non-finite pairs are skipped. Returns `null` when fewer than two
     * finite pairs remain or all x are identical (no estimable slope). [xs] and [ys] must have equal length.
     */
    fun fit(
        xs: DoubleArray,
        ys: DoubleArray,
    ): Fit? {
        require(xs.size == ys.size) { "xs and ys must have equal length (${xs.size} != ${ys.size})" }
        var n = 0
        var sumX = 0.0
        var sumY = 0.0
        for (i in xs.indices) {
            val x = xs[i]
            val y = ys[i]
            if (!x.isFinite() || !y.isFinite()) continue
            n++
            sumX += x
            sumY += y
        }
        if (n < 2) return null
        val meanX = sumX / n
        val meanY = sumY / n
        var sxx = 0.0
        var sxy = 0.0
        var syy = 0.0
        for (i in xs.indices) {
            val x = xs[i]
            val y = ys[i]
            if (!x.isFinite() || !y.isFinite()) continue
            val dx = x - meanX
            val dy = y - meanY
            sxx += dx * dx
            sxy += dx * dy
            syy += dy * dy
        }
        if (sxx <= 0.0) return null
        val slope = sxy / sxx
        val intercept = meanY - slope * meanX
        val sse = (syy - slope * sxy).coerceAtLeast(0.0)
        val residualSd = if (n >= 3) sqrt(sse / (n - 2)) else Double.NaN
        val rSquared = if (syy > 0.0) (1.0 - sse / syy).coerceIn(0.0, 1.0) else Double.NaN
        return Fit(
            slope = slope,
            intercept = intercept,
            n = n,
            meanX = meanX,
            sumSquaredX = sxx,
            residualStandardDeviation = residualSd,
            rSquared = rSquared,
        )
    }
}
