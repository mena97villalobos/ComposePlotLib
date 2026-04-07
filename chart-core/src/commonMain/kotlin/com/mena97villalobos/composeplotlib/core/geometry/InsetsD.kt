package com.mena97villalobos.composeplotlib.core.geometry

/**
 * Padding/insets on four sides in the same units as layout rects (typically pixels).
 *
 * @property left Left inset.
 * @property top Top inset.
 * @property right Right inset.
 * @property bottom Bottom inset.
 */
data class InsetsD(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
) {
    companion object {
        /** Zero insets on all sides. */
        val Zero = InsetsD(left = 0.0, top = 0.0, right = 0.0, bottom = 0.0)
    }
}
