package com.mena97villalobos.composeplotlib.core.geometry

/**
 * Non-negative width/height in layout units (text measurement, canvas size).
 *
 * @property width Width in layout/pixel units.
 * @property height Height in layout/pixel units.
 */
data class SizeD(
    val width: Double,
    val height: Double,
) {
    companion object {
        /** Zero size (width and height are `0`). */
        val Zero = SizeD(width = 0.0, height = 0.0)
    }
}
