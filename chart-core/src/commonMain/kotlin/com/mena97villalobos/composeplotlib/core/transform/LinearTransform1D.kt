package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * Linear map between [domain] and the pixel segment from [pixelStart] to [pixelEnd] (proportional rescale).
 *
 * @property domain Source interval in domain units.
 * @property pixelStart Pixel coordinate that [ClosedRangeD.start] of [domain] maps to.
 * @property pixelEnd Pixel coordinate that [ClosedRangeD.endInclusive] of [domain] maps to; may be less than
 * [pixelStart] to invert the axis.
 */
data class LinearTransform1D(
    val domain: ClosedRangeD,
    val pixelStart: Double,
    val pixelEnd: Double,
) {
    /**
     * Maps a domain [value] to a pixel coordinate. Returns [pixelStart] when [domain] has a degenerate (near-zero)
     * span; the result is not clamped to the pixel segment for values outside [domain].
     */
    fun toPixel(value: Double): Double {
        if (FpMath.isDegenerateSpan(domain.span)) {
            return pixelStart
        }

        val fraction = (value - domain.start) / domain.span
        return pixelStart + ((pixelEnd - pixelStart) * fraction)
    }

    /**
     * Inverse of [toPixel]: maps a [pixel] coordinate back to a domain value. Returns [ClosedRangeD.start] of [domain]
     * when the pixel segment has a degenerate (near-zero) span.
     */
    fun toDomain(pixel: Double): Double {
        val pixelSpan = pixelEnd - pixelStart
        if (FpMath.isDegenerateSpan(pixelSpan)) {
            return domain.start
        }

        val fraction = (pixel - pixelStart) / pixelSpan
        return domain.start + (domain.span * fraction)
    }
}
