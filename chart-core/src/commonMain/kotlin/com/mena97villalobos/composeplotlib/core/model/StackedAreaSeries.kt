package com.mena97villalobos.composeplotlib.core.model

/**
 * One band of a stacked-area chart. [values] are y magnitudes aligned by index to the chart's shared x values
 * (see [StackedAreaChartSpec][com.mena97villalobos.composeplotlib.core.spec.StackedAreaChartSpec.xValues]).
 *
 * @property id Stable identifier for the series.
 * @property label Human-readable name shown in the legend.
 * @property values Stacked magnitudes, index-aligned to the shared x values (non-finite treated as 0).
 * @property color Fill color for this band.
 */
data class StackedAreaSeries(
    val id: String,
    val label: String,
    val values: List<Double>,
    val color: ChartColor,
)
