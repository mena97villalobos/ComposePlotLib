package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import com.mena97villalobos.composeplotlib.core.layout.PlannedTreemapTile
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.selection.HierarchyNodeHit
import com.mena97villalobos.composeplotlib.core.selection.TreemapHitResolver
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import kotlin.math.roundToInt

/**
 * Which tiles get their own accessibility node: all of them up to [cap]; past it, only the top level, largest first,
 * at most [cap]. Plan order is pre-order with siblings largest first, so filtering keeps that order.
 */
internal fun treemapAccessibleTiles(
    plan: TreemapHierarchyPlan,
    cap: Int,
): List<PlannedTreemapTile> {
    val limit = cap.coerceAtLeast(0)
    if (plan.tiles.size <= limit) return plan.tiles
    return plan.tiles.filter { it.depth == 1 }.take(limit)
}

internal fun treemapAccessibleTileCount(
    plan: TreemapHierarchyPlan,
    cap: Int,
): Int = treemapAccessibleTiles(plan, cap).size

/**
 * One invisible accessibility node per exposed tile, laid over the canvas at the tile's bounds. Children are nested
 * inside their parent's node (a traversal group) so screen readers visit a folder and then its contents; siblings are
 * visited largest first. The nodes carry no pointer input, so touch handling stays with the chart.
 */
@Composable
internal fun TreemapTileSemantics(
    plan: TreemapHierarchyPlan,
    spec: TreemapHierarchySpec,
    highlightedNodeIds: Set<String>,
    onNodeClick: ((HierarchyNodeHit) -> Unit)?,
    onNodeLongClick: ((HierarchyNodeHit) -> Unit)?,
) {
    val exposed = treemapAccessibleTiles(plan, spec.maxAccessibleTiles)
    if (exposed.isEmpty()) return
    val exposedIds = exposed.mapTo(HashSet()) { it.nodeId }
    val childrenOf = HashMap<String, MutableList<PlannedTreemapTile>>()
    val roots = ArrayList<PlannedTreemapTile>()
    for (tile in exposed) {
        val parentId = tile.path.getOrNull(tile.path.size - 2)
        if (tile.depth > 1 && parentId != null && parentId in exposedIds) {
            childrenOf.getOrPut(parentId) { ArrayList() } += tile
        } else {
            roots += tile
        }
    }
    val context = TileSemanticsContext(spec, highlightedNodeIds, childrenOf, onNodeClick, onNodeLongClick)
    TileLevel(
        tiles = roots,
        originX = 0.0,
        originY = 0.0,
        context = context,
        modifier =
            Modifier
                .fillMaxSize()
                .semantics {
                    isTraversalGroup = true
                    collectionInfo = CollectionInfo(rowCount = roots.size, columnCount = 1)
                },
    )
}

private class TileSemanticsContext(
    val spec: TreemapHierarchySpec,
    val highlightedNodeIds: Set<String>,
    val childrenOf: Map<String, List<PlannedTreemapTile>>,
    val onNodeClick: ((HierarchyNodeHit) -> Unit)?,
    val onNodeLongClick: ((HierarchyNodeHit) -> Unit)?,
)

/** Places one node per tile at its bounds, relative to ([originX], [originY]), the parent tile's top-left. */
@Composable
private fun TileLevel(
    tiles: List<PlannedTreemapTile>,
    originX: Double,
    originY: Double,
    context: TileSemanticsContext,
    modifier: Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            tiles.forEachIndexed { index, tile -> TileNode(tile, index, context) }
        },
    ) { measurables, constraints ->
        val placeables =
            measurables.mapIndexed { i, measurable ->
                val b = tiles[i].bounds
                val width = ((b.right - originX).roundToInt() - (b.left - originX).roundToInt()).coerceAtLeast(0)
                val height = ((b.bottom - originY).roundToInt() - (b.top - originY).roundToInt()).coerceAtLeast(0)
                measurable.measure(Constraints.fixed(width, height))
            }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, placeable ->
                val b = tiles[i].bounds
                placeable.place((b.left - originX).roundToInt(), (b.top - originY).roundToInt())
            }
        }
    }
}

@Composable
private fun TileNode(
    tile: PlannedTreemapTile,
    index: Int,
    context: TileSemanticsContext,
) {
    val hit = TreemapHitResolver.hitFor(tile)
    val children = context.childrenOf[tile.nodeId].orEmpty()
    val highlighted =
        tile.nodeId in context.highlightedNodeIds ||
            (tile.isAggregate && tile.memberIds.any { it in context.highlightedNodeIds })
    val onClick = context.onNodeClick
    val onLongClick = context.onNodeLongClick
    Box(
        modifier =
            Modifier.semantics {
                contentDescription = "${tile.label}, ${tile.valueText ?: tile.value.toString()}"
                traversalIndex = index.toFloat()
                collectionItemInfo = CollectionItemInfo(rowIndex = index, rowSpan = 1, columnIndex = 0, columnSpan = 1)
                if (highlighted) selected = true
                if (children.isNotEmpty()) {
                    isTraversalGroup = true
                    collectionInfo = CollectionInfo(rowCount = children.size, columnCount = 1)
                }
                if (onClick != null) {
                    role = Role.Button
                    onClick {
                        onClick(hit)
                        true
                    }
                }
                if (onLongClick != null) {
                    customActions =
                        listOf(
                            CustomAccessibilityAction(context.spec.longClickActionLabel) {
                                onLongClick(hit)
                                true
                            },
                        )
                }
            },
    ) {
        if (children.isNotEmpty()) {
            TileLevel(
                tiles = children,
                originX = tile.bounds.left,
                originY = tile.bounds.top,
                context = context,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
