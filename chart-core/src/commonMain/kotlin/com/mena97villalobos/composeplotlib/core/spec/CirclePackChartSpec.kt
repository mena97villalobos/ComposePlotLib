package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One circle of a [CirclePackChartSpec]: a labeled value whose circle **area** is proportional to [value].
 *
 * @property id identity string.
 * @property label text drawn inside the circle when it fits.
 * @property value magnitude; non-positive values collapse to a zero-radius circle.
 * @property color circle fill color.
 */
data class CirclePackNode(
    val id: String,
    val label: String,
    val value: Double,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **circle-packing / bubble-pack** chart: a set of [nodes] packed as non-overlapping
 * circles (area ∝ value) via a greedy tangent packing, then fit to the plot. Single level (flat), read-only.
 *
 * @property nodes the circles to pack.
 * @property id chart identity string.
 * @property showLabels draw each circle's label when it fits.
 * @property labelColor label text color.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding around the packed group in pixels.
 */
data class CirclePackChartSpec(
    val nodes: List<CirclePackNode>,
    val id: String = "circlePack",
    val showLabels: Boolean = true,
    val labelColor: ChartColor = ChartColor.White,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 8.0, top = 8.0, right = 8.0, bottom = 8.0),
)
