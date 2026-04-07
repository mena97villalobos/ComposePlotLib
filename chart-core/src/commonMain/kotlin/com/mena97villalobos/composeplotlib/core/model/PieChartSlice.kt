package com.mena97villalobos.composeplotlib.core.model

/**
 * One segment of a pie or donut chart.
 *
 * Only strictly positive [value] entries contribute to the angular span; zero or negative values are skipped.
 */
data class PieChartSlice(
    val value: Double,
    val label: String,
    val color: ChartColor,
)
