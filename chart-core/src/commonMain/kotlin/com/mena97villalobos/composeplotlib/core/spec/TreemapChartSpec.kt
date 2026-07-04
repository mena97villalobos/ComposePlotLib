package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One leaf of a [TreemapChartSpec]: a labeled value whose rectangle area is proportional to [value].
 *
 * @property id identity string (for selection matching / stable keys).
 * @property label text drawn inside the tile when it fits.
 * @property value magnitude; non-positive values are omitted from the layout.
 * @property color tile fill color.
 */
data class TreemapNode(
    val id: String,
    val label: String,
    val value: Double,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **treemap**: a set of [nodes] laid out as area-proportional rectangles via a
 * squarified layout (aspect ratios kept near 1). Single level (flat) — read-only (no pan/zoom/selection).
 *
 * @property nodes the leaves to tile.
 * @property id chart identity string.
 * @property cellGapPx gap between adjacent tiles in pixels.
 * @property cellCornerRadiusPx tile corner radius in pixels.
 * @property showLabels draw each tile's label when the tile is large enough.
 * @property labelColor label text color.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding around the tiling in pixels.
 */
data class TreemapChartSpec(
    val nodes: List<TreemapNode>,
    val id: String = "treemap",
    val cellGapPx: Double = 2.0,
    val cellCornerRadiusPx: Double = 2.0,
    val showLabels: Boolean = true,
    val labelColor: ChartColor = ChartColor.White,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 8.0, top = 8.0, right = 8.0, bottom = 8.0),
)
