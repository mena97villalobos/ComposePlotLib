package com.mena97villalobos.composeplotlib.core.model

import kotlin.math.roundToInt

/**
 * Blends each RGB channel toward white by [fraction] (`0..1`), preserving alpha — a **tint**. Useful for adapting
 * a categorical palette to a dark background where the original colors read too dark.
 */
fun ChartColor.lighten(fraction: Double): ChartColor = blendTowards(target = 255, fraction = fraction)

/** Blends each RGB channel toward black by [fraction] (`0..1`), preserving alpha — a **shade**. */
fun ChartColor.darken(fraction: Double): ChartColor = blendTowards(target = 0, fraction = fraction)

private fun ChartColor.blendTowards(
    target: Int,
    fraction: Double,
): ChartColor {
    val f = fraction.coerceIn(0.0, 1.0)

    fun mix(channel: Int): Int = (channel + (target - channel) * f).roundToInt().coerceIn(0, 255)
    return ChartColor.argb(alpha, mix(red), mix(green), mix(blue))
}
