package com.mena97villalobos.composeplotlib.core.model

/**
 * One segment of a pie or donut chart.
 *
 * Only strictly positive [value] entries contribute to the angular span; zero or negative values are skipped.
 *
 * @property value Magnitude of the slice; its angular span is this value divided by the sum of all positive values.
 * @property label Human-readable name shown in the legend / slice label.
 * @property color Fill color for this slice; see [ChartColor].
 */
data class PieChartSlice(
    val value: Double,
    val label: String,
    val color: ChartColor,
)
