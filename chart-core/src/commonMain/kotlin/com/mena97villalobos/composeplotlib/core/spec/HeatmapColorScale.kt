package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.HeatmapCell

/**
 * Maps a numeric cell value to a [ChartColor] by linearly interpolating between [minColor] and [maxColor].
 *
 * Values at or below [minValue] map to [minColor]; values at or above [maxValue] map to [maxColor].
 * When [minValue] == [maxValue] the midpoint color is returned for all inputs.
 */
data class HeatmapColorScale(
    val minValue: Double = 0.0,
    val maxValue: Double = 1.0,
    val minColor: ChartColor = ChartColor.White,
    val maxColor: ChartColor = ChartColor.Blue,
) {
    /** Returns the interpolated color for [value]. */
    fun colorFor(value: Double): ChartColor {
        val span = maxValue - minValue
        val t = if (span == 0.0) 0.5 else ((value - minValue) / span).coerceIn(0.0, 1.0)
        return lerpColor(minColor, maxColor, t)
    }

    companion object {
        /**
         * Creates a scale whose min/max are derived from the actual [cells] range.
         * Falls back to `[0, 1]` when [cells] is empty.
         */
        fun auto(
            cells: List<HeatmapCell>,
            minColor: ChartColor = ChartColor.White,
            maxColor: ChartColor = ChartColor.Blue,
        ): HeatmapColorScale {
            val min = cells.minOfOrNull { it.value } ?: 0.0
            val max = cells.maxOfOrNull { it.value } ?: 1.0
            return HeatmapColorScale(min, max, minColor, maxColor)
        }
    }
}

private fun lerpColor(
    a: ChartColor,
    b: ChartColor,
    t: Double,
): ChartColor {
    fun channel(shift: Int): Long {
        val ca = (a.argb shr shift) and 0xFFL
        val cb = (b.argb shr shift) and 0xFFL
        return (ca + ((cb - ca) * t).toLong()).coerceIn(0L, 0xFFL)
    }
    return ChartColor(
        (channel(24) shl 24) or
            (channel(16) shl 16) or
            (channel(8) shl 8) or
            channel(0),
    )
}
