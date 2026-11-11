package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD

/**
 * A node of a hierarchical chart (treemap, sunburst) that the user tapped or long-pressed, as reported to
 * `onNodeClick` / `onNodeLongClick`.
 *
 * @property nodeId the node's id; for an aggregate tile, its synthetic id.
 * @property path ids from the top of the hierarchy down to this node (inclusive), so a caller can build a breadcrumb
 *   or drill to any ancestor.
 * @property label the node's label.
 * @property value the node's size (for a parent, the larger of its value and its children's sum).
 * @property valueText the chart's value-formatter text for the node, or `null` without a formatter.
 * @property depth `1` for the top drawn level below the displayed root, `2` for the next, and so on.
 * @property hasChildren true when the node has children, i.e. it can be drilled into.
 * @property isAggregate true for a tile that stands for several nodes too small to draw on their own.
 * @property memberIds for an aggregate, the ids of the nodes it contains (largest first); empty otherwise.
 * @property bounds the node's bounding box in chart pixels, for anchoring a menu or tooltip.
 */
data class HierarchyNodeHit(
    val nodeId: String,
    val path: List<String>,
    val label: String,
    val value: Double,
    val valueText: String?,
    val depth: Int,
    val hasChildren: Boolean,
    val isAggregate: Boolean,
    val memberIds: List<String>,
    val bounds: RectD,
) {
    /** Center of [bounds]. */
    val anchor: OffsetD get() = bounds.center
}
