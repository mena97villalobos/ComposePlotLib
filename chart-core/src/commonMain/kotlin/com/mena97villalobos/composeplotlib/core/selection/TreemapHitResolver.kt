package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.layout.PlannedTreemapTile
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan

/**
 * Maps a point in chart pixels to a tile of a [TreemapHierarchyPlan].
 *
 * A point resolves to the **deepest** drawn tile whose bounds contain it (edges inclusive). A point in the gap
 * between two children, in a parent's header band or padding, or in a parent's undrawn remainder therefore resolves
 * to the **parent**; a point in the gap between two top-level tiles, or outside the tiling, resolves to nothing.
 */
object TreemapHitResolver {
    /** The deepest tile containing [point], or `null`. */
    fun resolve(
        point: OffsetD,
        plan: TreemapHierarchyPlan,
    ): PlannedTreemapTile? {
        if (!point.x.isFinite() || !point.y.isFinite()) return null
        // Tiles are pre-order, so the last match is the deepest.
        return plan.tiles.lastOrNull { it.bounds.contains(point) }
    }

    /**
     * The tile containing [point] at exactly [depth] (`1` = the top drawn level), or `null` when [point] is not on a
     * tile at that depth — for example to always act on top-level folders even when their children are drawn.
     */
    fun resolveAtDepth(
        point: OffsetD,
        plan: TreemapHierarchyPlan,
        depth: Int,
    ): PlannedTreemapTile? {
        if (!point.x.isFinite() || !point.y.isFinite()) return null
        return plan.tiles.firstOrNull { it.depth == depth && it.bounds.contains(point) }
    }

    /** The callback payload for [tile]. */
    fun hitFor(tile: PlannedTreemapTile): HierarchyNodeHit =
        HierarchyNodeHit(
            nodeId = tile.nodeId,
            path = tile.path,
            label = tile.label,
            value = tile.value,
            valueText = tile.valueText,
            depth = tile.depth,
            hasChildren = tile.hasChildren,
            isAggregate = tile.isAggregate,
            memberIds = tile.memberIds,
            bounds = tile.bounds,
        )
}
