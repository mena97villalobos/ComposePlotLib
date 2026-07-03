package com.mena97villalobos.composeplotlib.core.spec

import kotlin.math.sqrt

/**
 * Maps a bubble size value `z` to a pixel radius, **area-proportionally** — the bubble's *area* (not radius) scales
 * with the normalized value, which is the perceptually correct encoding for magnitude. Values at/below [minValue]
 * map to [minRadiusPx], at/above [maxValue] to [maxRadiusPx]. KMP-safe.
 *
 * @property minValue Size value mapped to [minRadiusPx].
 * @property maxValue Size value mapped to [maxRadiusPx].
 * @property minRadiusPx Smallest bubble radius in pixels.
 * @property maxRadiusPx Largest bubble radius in pixels.
 */
data class BubbleSizeScale(
    val minValue: Double,
    val maxValue: Double,
    val minRadiusPx: Double = 3.0,
    val maxRadiusPx: Double = 24.0,
) {
    /** Area-proportional radius (pixels) for size value [z]. Non-finite z maps to [minRadiusPx]. */
    fun radiusFor(z: Double): Double {
        if (!z.isFinite()) return minRadiusPx
        val span = maxValue - minValue
        val t = if (span <= 0.0) 0.0 else ((z - minValue) / span).coerceIn(0.0, 1.0)
        val minArea = minRadiusPx * minRadiusPx
        val maxArea = maxRadiusPx * maxRadiusPx
        return sqrt(minArea + t * (maxArea - minArea))
    }

    /**
     * Representative `(value, radiusPx)` pairs for a size legend: [count] evenly-spaced values across
     * `[minValue, maxValue]` with their radii. `count <= 0` returns empty; `count == 1` returns the max.
     */
    fun legendTicks(count: Int = 3): List<Pair<Double, Double>> {
        if (count <= 0) return emptyList()
        if (count == 1) return listOf(maxValue to radiusFor(maxValue))
        return (0 until count).map { i ->
            val v = minValue + (maxValue - minValue) * (i.toDouble() / (count - 1))
            v to radiusFor(v)
        }
    }

    companion object {
        /** Derives [minValue] / [maxValue] from the finite [values]; falls back to `[0, 1]` when empty. */
        fun auto(
            values: List<Double>,
            minRadiusPx: Double = 3.0,
            maxRadiusPx: Double = 24.0,
        ): BubbleSizeScale {
            val finite = values.filter { it.isFinite() }
            val min = finite.minOrNull() ?: 0.0
            val max = finite.maxOrNull() ?: 1.0
            return BubbleSizeScale(min, max, minRadiusPx, maxRadiusPx)
        }
    }
}
