package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.spec.AxisBreak
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sign

/**
 * Maps an axis value into the **scaled space** in which the [LinearTransform1D] pixel map is linear, and back.
 *
 * Linear / category / time axes use [Identity] (scaled space == domain space). [Log] plots `log(value)` so decades
 * are evenly spaced; [SymLog] is log away from zero but linear within a threshold so data spanning zero (or negatives)
 * still maps continuously. [ChartTransform] composes the forward map before its linear pixel projection.
 */
sealed interface AxisValueTransform {
    /** Maps a domain [value] into scaled space (where pixel projection is linear). May be non-finite when undefined. */
    fun forward(value: Double): Double

    /** Inverse of [forward]: maps a [scaled] coordinate back to domain space. */
    fun inverse(scaled: Double): Double

    /** True when [forward] / [inverse] are the identity (the linear/category/time fast path). */
    val isIdentity: Boolean get() = false

    /** Domain space and scaled space coincide (linear, category, and time axes). */
    data object Identity : AxisValueTransform {
        override fun forward(value: Double): Double = value

        override fun inverse(scaled: Double): Double = scaled

        override val isIdentity: Boolean get() = true
    }

    /**
     * Logarithmic mapping (`forward(v) = log_base(v)`), for **strictly positive** data. Non-positive values map to
     * `NaN` (treated as gaps / skipped by rendering, selection, and bounds).
     *
     * @property base Log base; defaults to `10.0`.
     */
    data class Log(
        val base: Double = 10.0,
    ) : AxisValueTransform {
        private val lnBase = ln(base)

        override fun forward(value: Double): Double {
            if (value <= 0.0) return Double.NaN
            return if (base == 10.0) log10(value) else ln(value) / lnBase
        }

        override fun inverse(scaled: Double): Double = base.pow(scaled)
    }

    /**
     * Symmetric-log mapping: linear within `[-linearThreshold, linearThreshold]` and logarithmic beyond, so data that
     * crosses zero (or is negative) maps continuously. `forward(v) = sign(v) * log_base(1 + |v| / linearThreshold)`.
     *
     * @property base Log base for the logarithmic region; defaults to `10.0`.
     * @property linearThreshold Half-width of the near-zero linear region; must be `> 0`. Defaults to `1.0`.
     */
    data class SymLog(
        val base: Double = 10.0,
        val linearThreshold: Double = 1.0,
    ) : AxisValueTransform {
        private val lnBase = ln(base)
        private val thr = if (linearThreshold > 0.0 && linearThreshold.isFinite()) linearThreshold else 1.0

        override fun forward(value: Double): Double {
            if (!value.isFinite()) return Double.NaN
            val m = abs(value) / thr
            return sign(value) * (ln(1.0 + m) / lnBase)
        }

        override fun inverse(scaled: Double): Double {
            if (!scaled.isFinite()) return Double.NaN
            return sign(scaled) * thr * (base.pow(abs(scaled)) - 1.0)
        }
    }

    /**
     * **Broken** mapping: identity outside [breaks]; within each break the slope drops to [compressionFactor] so the
     * collapsed interval occupies only that fraction of its real length in scaled space. Piecewise-linear and
     * invertible.
     */
    data class Broken(
        val breaks: List<AxisBreak>,
        val compressionFactor: Double = 0.06,
    ) : AxisValueTransform {
        private val f = compressionFactor.coerceIn(0.001, 1.0)
        private val origins: DoubleArray
        private val scaledOrigins: DoubleArray

        init {
            val sorted = breaks.filter { it.to > it.from && it.from.isFinite() && it.to.isFinite() }.sortedBy { it.from }
            val o = ArrayList<Double>(sorted.size * 2)
            val s = ArrayList<Double>(sorted.size * 2)
            var offset = 0.0
            for (b in sorted) {
                o.add(b.from)
                s.add(b.from - offset)
                offset += (b.to - b.from) * (1.0 - f)
                o.add(b.to)
                s.add(b.to - offset)
            }
            origins = o.toDoubleArray()
            scaledOrigins = s.toDoubleArray()
        }

        override fun forward(value: Double): Double = if (origins.isEmpty()) value else interpolate(value, origins, scaledOrigins)

        override fun inverse(scaled: Double): Double = if (scaledOrigins.isEmpty()) scaled else interpolate(scaled, scaledOrigins, origins)
    }

    companion object {
        /** The value transform implied by an [AxisScale] ([Identity] for linear / category / time). */
        fun forScale(scale: AxisScale): AxisValueTransform =
            when (scale) {
                is AxisScale.Log -> Log(scale.base)
                is AxisScale.SymLog -> SymLog(scale.base, scale.linearThreshold)
                is AxisScale.Broken -> Broken(scale.breaks, scale.compressionFactor)
                else -> Identity
            }
    }
}

/** Piecewise-linear interpolation of [x] over control points ([xs] → [ys]); slope-1 extrapolation outside the range. */
private fun interpolate(
    x: Double,
    xs: DoubleArray,
    ys: DoubleArray,
): Double {
    if (x <= xs.first()) return ys.first() + (x - xs.first())
    if (x >= xs.last()) return ys.last() + (x - xs.last())
    for (i in 0 until xs.size - 1) {
        if (x <= xs[i + 1]) {
            val span = xs[i + 1] - xs[i]
            if (span <= 0.0) return ys[i + 1]
            val t = (x - xs[i]) / span
            return ys[i] + t * (ys[i + 1] - ys[i])
        }
    }
    return ys.last()
}
