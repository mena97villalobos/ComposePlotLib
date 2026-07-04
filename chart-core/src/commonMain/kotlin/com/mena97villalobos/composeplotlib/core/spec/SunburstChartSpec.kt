package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One node of a [SunburstChartSpec] hierarchy.
 *
 * @property label node label.
 * @property value magnitude for a **leaf**; ignored for internal nodes (whose value is the sum of children).
 * @property color explicit fill; when `null`, top-level branches take a palette color and descendants inherit a
 *   progressively lighter shade of their branch.
 * @property children child nodes; empty for a leaf.
 */
data class SunburstNode(
    val label: String,
    val value: Double = 0.0,
    val color: ChartColor? = null,
    val children: List<SunburstNode> = emptyList(),
)

/** Total (leaf) value of this subtree: the node's own value if a leaf, else the sum of its children's totals. */
fun SunburstNode.totalValue(): Double = if (children.isEmpty()) value.coerceAtLeast(0.0) else children.sumOf { it.totalValue() }

/**
 * Immutable configuration for a **sunburst** (hierarchical radial) chart: a [root] tree drawn as concentric rings,
 * one ring per depth, where each node's angular span is proportional to its value within its parent. The root is an
 * optional center disk. Read-only (no pan/zoom/selection).
 *
 * @property root the hierarchy to draw.
 * @property showRoot draw the root as a center disk (depth 0) when true; otherwise the innermost ring is depth 1.
 * @property startAngleDegrees angle of the first slice; default `-90` (12 o'clock).
 * @property sweepDegrees total angular extent; `360` is a full sunburst.
 * @property ringGapPx radial gap between rings in pixels.
 * @property id chart identity string.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding around the chart in pixels.
 */
data class SunburstChartSpec(
    val root: SunburstNode,
    val showRoot: Boolean = true,
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    val sweepDegrees: Double = 360.0,
    val ringGapPx: Double = 1.0,
    val id: String = "sunburst",
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 16.0, top = 16.0, right = 16.0, bottom = 16.0),
)
