package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One drawn segment of a sunburst: a ring sector (or the center disk for the displayed root).
 *
 * @property nodeId the node's [com.mena97villalobos.composeplotlib.core.spec.nodeId].
 * @property path ids from the spec's root down to this node (inclusive).
 * @property depth ring index relative to the displayed root: `0` is the center disk, `1` the first ring, …
 * @property startAngleDegrees start angle (0° = 3 o'clock, clockwise), as drawn.
 * @property sweepAngleDegrees angular extent.
 * @property innerRadius inner radius in pixels (`0` for the center disk).
 * @property outerRadius outer radius in pixels.
 * @property value the node's [com.mena97villalobos.composeplotlib.core.spec.totalValue].
 * @property label node label.
 * @property color resolved fill.
 * @property hasChildren true when the node has children.
 */
data class PlannedSunburstSegment(
    val nodeId: String,
    val path: List<String>,
    val depth: Int,
    val startAngleDegrees: Double,
    val sweepAngleDegrees: Double,
    val innerRadius: Double,
    val outerRadius: Double,
    val value: Double,
    val label: String,
    val color: ChartColor,
    val hasChildren: Boolean,
)

/**
 * Planner output for an interactive sunburst.
 *
 * @property layout draw commands; identical to [ChartPlanner.planSunburstChart]'s when nothing is drilled into or
 *   highlighted.
 * @property segments every drawn segment, in pre-order.
 * @property center center of the rings in canvas pixels.
 * @property rootNodeId the displayed root actually used, or `null` for the spec's root.
 * @property rootPath ids from the spec's root down to [rootNodeId] (inclusive); empty for the spec's root.
 */
data class SunburstHierarchyPlan(
    val layout: ChartLayoutPlan,
    val segments: List<PlannedSunburstSegment>,
    val center: OffsetD,
    val rootNodeId: String?,
    val rootPath: List<String>,
)
