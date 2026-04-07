package com.mena97villalobos.composeplotlib.core.geometry

/** Non-negative width/height in layout units (text measurement, canvas size). */
data class SizeD(
    val width: Double,
    val height: Double,
) {
    companion object {
        val Zero = SizeD(width = 0.0, height = 0.0)
    }
}
