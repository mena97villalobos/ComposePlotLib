package com.mena97villalobos.composeplotlib.core.model

/**
 * A single data point in a [com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec].
 *
 * @param col Zero-based column index (mapped to the X axis).
 * @param row Zero-based row index (mapped to the Y axis; row 0 appears at the bottom).
 * @param value Numeric intensity encoded as color via [com.mena97villalobos.composeplotlib.core.spec.HeatmapColorScale].
 */
data class HeatmapCell(
    val col: Int,
    val row: Int,
    val value: Double,
)
