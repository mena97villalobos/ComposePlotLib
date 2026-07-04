package com.mena97villalobos.composeplotlib.core.indicators

import kotlin.math.sqrt

/** MACD output: the MACD line, its signal line, and the histogram (`macd - signal`). Values are `null` where undefined. */
data class MacdSeries(
    val macd: List<Double?>,
    val signal: List<Double?>,
    val histogram: List<Double?>,
)

/** Bollinger Bands: the middle SMA plus the upper/lower bands at `± stdDevMultiplier · σ`. Values are `null` where undefined. */
data class BollingerBands(
    val middle: List<Double?>,
    val upper: List<Double?>,
    val lower: List<Double?>,
)

/**
 * Standard financial technical indicators over a series of values (typically closing prices). Each function
 * returns a list **aligned to the input** — element `i` corresponds to input `i`, and is `null` until enough
 * prior samples exist to define the indicator. Pure and KMP-safe; map the results to `LineSeries` to overlay
 * them on a line or candlestick chart.
 */
object TechnicalIndicators {
    /** Simple moving average over [period] samples. */
    fun sma(
        values: List<Double>,
        period: Int,
    ): List<Double?> {
        if (period <= 0) return List(values.size) { null }
        val out = arrayOfNulls<Double>(values.size)
        var sum = 0.0
        for (i in values.indices) {
            sum += values[i]
            if (i >= period) sum -= values[i - period]
            if (i >= period - 1) out[i] = sum / period
        }
        return out.toList()
    }

    /**
     * Exponential moving average over [period] samples, seeded with the SMA of the first [period] values (so the
     * first non-null value is at index `period - 1`), then `ema[i] = value·k + ema[i-1]·(1-k)`, `k = 2/(period+1)`.
     */
    fun ema(
        values: List<Double>,
        period: Int,
    ): List<Double?> {
        if (period <= 0 || values.size < period) return List(values.size) { null }
        val out = arrayOfNulls<Double>(values.size)
        val k = 2.0 / (period + 1)
        var seed = 0.0
        for (i in 0 until period) seed += values[i]
        var prev = seed / period
        out[period - 1] = prev
        for (i in period until values.size) {
            prev = values[i] * k + prev * (1 - k)
            out[i] = prev
        }
        return out.toList()
    }

    /**
     * Relative Strength Index over [period] samples (Wilder's smoothing). The first value is at index [period];
     * `rsi = 100 - 100 / (1 + avgGain / avgLoss)`, with `100` when there are no losses.
     */
    fun rsi(
        values: List<Double>,
        period: Int = 14,
    ): List<Double?> {
        val out = arrayOfNulls<Double>(values.size)
        if (period <= 0 || values.size <= period) return out.toList()
        var gainSum = 0.0
        var lossSum = 0.0
        for (i in 1..period) {
            val change = values[i] - values[i - 1]
            if (change >= 0) gainSum += change else lossSum -= change
        }
        var avgGain = gainSum / period
        var avgLoss = lossSum / period
        out[period] = rsiFrom(avgGain, avgLoss)
        for (i in period + 1 until values.size) {
            val change = values[i] - values[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
            out[i] = rsiFrom(avgGain, avgLoss)
        }
        return out.toList()
    }

    /** MACD: `ema(fast) - ema(slow)`, its [signalPeriod] EMA signal line, and the histogram. */
    fun macd(
        values: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
    ): MacdSeries {
        val fast = ema(values, fastPeriod)
        val slow = ema(values, slowPeriod)
        val macdLine =
            values.indices.map { i ->
                val f = fast[i]
                val s = slow[i]
                if (f != null && s != null) f - s else null
            }
        // Signal = EMA of the contiguous non-null MACD tail, realigned to the original indices.
        val firstNonNull = macdLine.indexOfFirst { it != null }
        val signal = arrayOfNulls<Double>(values.size)
        if (firstNonNull >= 0) {
            val dense = macdLine.drop(firstNonNull).map { it ?: 0.0 }
            val denseSignal = ema(dense, signalPeriod)
            denseSignal.forEachIndexed { j, v -> if (v != null) signal[firstNonNull + j] = v }
        }
        val histogram =
            values.indices.map { i ->
                val m = macdLine[i]
                val sg = signal[i]
                if (m != null && sg != null) m - sg else null
            }
        return MacdSeries(macd = macdLine, signal = signal.toList(), histogram = histogram)
    }

    /** Bollinger Bands: middle = [period] SMA; upper/lower = middle ± [stdDevMultiplier] · population σ over the window. */
    fun bollingerBands(
        values: List<Double>,
        period: Int = 20,
        stdDevMultiplier: Double = 2.0,
    ): BollingerBands {
        val middle = sma(values, period)
        val upper = arrayOfNulls<Double>(values.size)
        val lower = arrayOfNulls<Double>(values.size)
        if (period > 0) {
            for (i in values.indices) {
                val mean = middle[i] ?: continue
                var variance = 0.0
                for (j in i - period + 1..i) {
                    val d = values[j] - mean
                    variance += d * d
                }
                val sd = sqrt(variance / period)
                upper[i] = mean + stdDevMultiplier * sd
                lower[i] = mean - stdDevMultiplier * sd
            }
        }
        return BollingerBands(middle = middle, upper = upper.toList(), lower = lower.toList())
    }

    private fun rsiFrom(
        avgGain: Double,
        avgLoss: Double,
    ): Double {
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - 100.0 / (1.0 + rs)
    }
}
