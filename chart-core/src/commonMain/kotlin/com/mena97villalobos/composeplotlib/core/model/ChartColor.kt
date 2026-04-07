package com.mena97villalobos.composeplotlib.core.model

/**
 * ARGB color as `0xAARRGGBB`. The Compose layer maps this to platform colors when drawing.
 */
data class ChartColor(val argb: Long) {
    companion object {
        val Black = ChartColor(0xFF000000)
        val White = ChartColor(0xFFFFFFFF)
        val Gray = ChartColor(0xFF9E9E9E)
        val LightGray = ChartColor(0xFFE0E0E0)
        val Blue = ChartColor(0xFF1565C0)
        val Green = ChartColor(0xFF2E7D32)
        val Red = ChartColor(0xFFC62828)
    }
}
