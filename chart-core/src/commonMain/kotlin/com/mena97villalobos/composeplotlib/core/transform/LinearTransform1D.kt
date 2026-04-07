package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/** Linear map between [domain] and the pixel segment from [pixelStart] to [pixelEnd] (proportional rescale). */
data class LinearTransform1D(
    val domain: ClosedRangeD,
    val pixelStart: Double,
    val pixelEnd: Double,
) {
    fun toPixel(value: Double): Double {
        if (FpMath.isDegenerateSpan(domain.span)) {
            return pixelStart
        }

        val fraction = (value - domain.start) / domain.span
        return pixelStart + ((pixelEnd - pixelStart) * fraction)
    }

    fun toDomain(pixel: Double): Double {
        val pixelSpan = pixelEnd - pixelStart
        if (FpMath.isDegenerateSpan(pixelSpan)) {
            return domain.start
        }

        val fraction = (pixel - pixelStart) / pixelSpan
        return domain.start + (domain.span * fraction)
    }
}
