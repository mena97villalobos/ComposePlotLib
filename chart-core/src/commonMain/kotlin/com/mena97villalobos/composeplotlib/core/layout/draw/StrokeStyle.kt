package com.mena97villalobos.composeplotlib.core.layout.draw

/**
 * Optional stroke appearance; `null` on a [com.mena97villalobos.composeplotlib.core.layout.DrawCommand] means solid stroke.
 *
 * @param dashIntervals Alternating on/off lengths in **pixels** along the stroke (Compose `PathEffect` style).
 */
data class StrokeStyle(
    val dashIntervals: List<Double>? = null,
    val dashPhase: Double = 0.0,
    val cap: StrokeCap = StrokeCap.Butt,
    val join: StrokeJoin = StrokeJoin.Miter,
)
