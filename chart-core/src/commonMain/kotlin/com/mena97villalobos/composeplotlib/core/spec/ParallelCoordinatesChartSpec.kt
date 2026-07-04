package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** One vertical axis (dimension) of a [ParallelCoordinatesChartSpec], with its own value range. */
data class ParallelAxis(
    val label: String,
    val min: Double,
    val max: Double,
)

/**
 * One record of a [ParallelCoordinatesChartSpec], drawn as a polyline crossing every axis at its value.
 *
 * @property values one value per axis (index-aligned to [ParallelCoordinatesChartSpec.axes]).
 */
data class ParallelSeries(
    val id: String,
    val label: String,
    val values: List<Double>,
    val color: ChartColor,
    val strokeWidth: Double = 1.5,
)

/**
 * Immutable configuration for a **parallel coordinates** chart: several parallel vertical [axes] (each with its own
 * range) and a set of [series], each drawn as a polyline connecting its value on every axis. Great for spotting
 * correlations and clusters across many dimensions. Read-only (v1 — per-axis brushing is a future extension).
 *
 * @property axes the ordered vertical axes (dimensions); at least one.
 * @property series records to draw across the axes.
 * @property id chart identity string.
 * @property axisColor color of the vertical axis lines.
 * @property axisLabelColor color of the axis labels.
 * @property legend legend configuration (series labels + colors).
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding around the plot (bottom reserves the axis-label row) in pixels.
 */
data class ParallelCoordinatesChartSpec(
    val axes: List<ParallelAxis>,
    val series: List<ParallelSeries>,
    val id: String = "parallelCoordinates",
    val axisColor: ChartColor = ChartColor(0xFFBDBDBD),
    val axisLabelColor: ChartColor = ChartColor(0xFF57606A),
    val legend: LegendSpec = LegendSpec(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 16.0, right = 24.0, bottom = 32.0),
)
