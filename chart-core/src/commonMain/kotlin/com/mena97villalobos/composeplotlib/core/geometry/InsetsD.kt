package com.mena97villalobos.composeplotlib.core.geometry

/** Padding/insets on four sides in the same units as layout rects (typically pixels). */
data class InsetsD(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    companion object {
        val Zero = InsetsD(left = 0.0, top = 0.0, right = 0.0, bottom = 0.0)
    }
}
