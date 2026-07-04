package com.mena97villalobos.composeplotlib.core.stats

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/** One histogram bin: half-open interval `[start, end)` (the last bin includes its `end`) and its [count]. */
data class HistogramBin(
    val start: Double,
    val end: Double,
    val count: Int,
) {
    val center: Double get() = (start + end) / 2.0
}

/**
 * Bins [values] into [binCount] equal-width [HistogramBin]s spanning the data range. Non-finite values are dropped;
 * the last bin includes the maximum. Returns an empty list for no finite data or `binCount <= 0`. Render the result
 * as a `BarChart` (bin center → count).
 */
fun histogramBins(
    values: List<Double>,
    binCount: Int,
): List<HistogramBin> {
    if (binCount <= 0) return emptyList()
    val finite = values.filter { it.isFinite() }
    if (finite.isEmpty()) return emptyList()
    val min = finite.min()
    val max = finite.max()
    if (min == max) return listOf(HistogramBin(min, min + 1.0, finite.size))
    val width = (max - min) / binCount
    val counts = IntArray(binCount)
    for (v in finite) {
        val index = (((v - min) / width).toInt()).coerceIn(0, binCount - 1)
        counts[index]++
    }
    return (0 until binCount).map { i ->
        HistogramBin(start = min + i * width, end = min + (i + 1) * width, count = counts[i])
    }
}

/** Silverman's rule-of-thumb kernel bandwidth: `1.06 · σ · n^(−1/5)` (population σ). Falls back to `1.0` for tiny/flat data. */
fun silvermanBandwidth(values: List<Double>): Double {
    val finite = values.filter { it.isFinite() }
    val n = finite.size
    if (n < 2) return 1.0
    val mean = finite.sum() / n
    val variance = finite.sumOf { (it - mean) * (it - mean) } / n
    val sd = sqrt(variance)
    if (sd <= 0.0) return 1.0
    return 1.06 * sd * n.toDouble().pow(-1.0 / 5.0)
}

/**
 * Gaussian **kernel density estimate** of [values] evaluated at each of [evalPoints]:
 * `f(x) = (1 / (n·h)) · Σ K((x − xᵢ)/h)` with the standard-normal kernel `K`. [bandwidth] defaults to
 * [silvermanBandwidth]. Returns a density per eval point (same order); empty data yields all zeros.
 */
fun kernelDensity(
    values: List<Double>,
    evalPoints: List<Double>,
    bandwidth: Double? = null,
): List<Double> {
    val finite = values.filter { it.isFinite() }
    if (finite.isEmpty()) return evalPoints.map { 0.0 }
    val h = (bandwidth ?: silvermanBandwidth(finite)).takeIf { it > 0.0 } ?: 1.0
    val norm = 1.0 / (finite.size * h)
    val gaussianScale = 1.0 / sqrt(2.0 * PI)
    return evalPoints.map { x ->
        var sum = 0.0
        for (v in finite) {
            val u = (x - v) / h
            sum += gaussianScale * exp(-0.5 * u * u)
        }
        sum * norm
    }
}
