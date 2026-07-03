package com.mena97villalobos.composeplotlib.core.model

/**
 * ARGB color as `0xAARRGGBB`. The Compose layer maps this to platform colors when drawing.
 *
 * @property argb Packed ARGB value where the high byte is alpha and the low three bytes are red, green, blue.
 */
data class ChartColor(
    val argb: Long,
) {
    companion object {
        /** Opaque black (`0xFF000000`). */
        val Black = ChartColor(0xFF000000)

        /** Opaque white (`0xFFFFFFFF`). */
        val White = ChartColor(0xFFFFFFFF)

        /** Opaque medium gray (`0xFF9E9E9E`); default for axis/legend text. */
        val Gray = ChartColor(0xFF9E9E9E)

        /** Opaque light gray (`0xFFE0E0E0`); default for grid and axis lines. */
        val LightGray = ChartColor(0xFFE0E0E0)

        /** Opaque blue (`0xFF1565C0`); default line/point color. */
        val Blue = ChartColor(0xFF1565C0)

        /** Opaque green (`0xFF2E7D32`); default bar/bullish color. */
        val Green = ChartColor(0xFF2E7D32)

        /** Opaque red (`0xFFC62828`); default selection/bearish color. */
        val Red = ChartColor(0xFFC62828)
    }
}
