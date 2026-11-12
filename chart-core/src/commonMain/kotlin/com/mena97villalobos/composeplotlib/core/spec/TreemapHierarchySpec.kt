package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One node of a hierarchical treemap ([TreemapHierarchySpec]).
 *
 * **Size.** A leaf's size is its [value]. A node with [children] is as large as the larger of its own [value] and
 * the sum of its children's sizes:
 * - leave [value] at `0` (the default) and a parent is exactly the sum of its children;
 * - set [value] to a total that is **larger** than the children's sum and the difference is a **remainder area**
 *   inside the parent that belongs to no child (for example, the bytes of files directly inside a folder when only
 *   sub-folders are listed). The remainder is laid out like a child, is not drawn as a tile, and resolves to the
 *   parent on hit-testing. A [value] smaller than the children's sum is ignored.
 *
 * Non-finite, zero and negative sizes are omitted from the layout.
 *
 * @property id identity, unique across the whole tree. Hit-testing, drill-in, highlighting and accessibility all
 *   refer to nodes by id, so keep it stable across data updates.
 * @property label text drawn in the tile (or in the parent's header band) when it fits.
 * @property value magnitude; see **Size** above.
 * @property color explicit fill; when `null`, top-level nodes take [TreemapHierarchySpec.palette] colors by index
 *   and descendants inherit a progressively lighter shade of their top-level ancestor.
 * @property children child nodes; empty for a leaf.
 */
data class TreemapHierarchyNode(
    val id: String,
    val label: String,
    val value: Double = 0.0,
    val color: ChartColor? = null,
    val children: List<TreemapHierarchyNode> = emptyList(),
)

/**
 * Size used for layout: a leaf's own [TreemapHierarchyNode.value], or for a parent the larger of its value and the
 * sum of its children's sizes. Never negative; non-finite values count as `0`.
 */
fun TreemapHierarchyNode.layoutSize(): Double {
    val own = if (value.isFinite() && value > 0.0) value else 0.0
    if (children.isEmpty()) return own
    val sum = children.sumOf { it.layoutSize() }
    return if (own > sum) own else sum
}

/** Formats a node's value for the second label line and for accessibility. Pure Kotlin — keep UI types out. */
typealias TreemapValueFormatter = (TreemapHierarchyNode) -> String

/** Text for an aggregate ("smaller items") tile, given how many nodes it merges. */
typealias TreemapAggregateLabel = (memberCount: Int) -> String

/** Default aggregate label: `"N smaller items"`. Supply your own for localization. */
val DefaultTreemapAggregateLabel: TreemapAggregateLabel = { count -> "$count smaller items" }

/**
 * Outline and optional dimming for highlighted nodes on hierarchical charts (treemap, sunburst).
 *
 * @property outlineColor stroke color; `null` picks black or white per node, whichever contrasts more with its fill.
 * @property outlineWidthPx stroke width in pixels, drawn inside the node's shape.
 * @property dimOthersAlpha when non-null and something is highlighted, every node that is neither highlighted nor
 *   inside a highlighted node is drawn at this opacity (`0..1`); `null` (default) leaves them as they are.
 */
data class HierarchyHighlightStyle(
    val outlineColor: ChartColor? = null,
    val outlineWidthPx: Double = 3.0,
    val dimOthersAlpha: Double? = null,
)

/**
 * Immutable configuration for a **hierarchical treemap**: [nodes] (the top level) and their descendants laid out as
 * nested squarified rectangles. Each parent's children fill the parent's tile below a header band that carries the
 * parent's label; tile areas are proportional to [layoutSize] within each parent.
 *
 * A flat list (nodes without children) is a one-level treemap. To start from a single root, pass `listOf(root)` (the
 * root is drawn as one tile containing its children) or `root.children`.
 *
 * All sizes are in **pixels**, like the rest of the library's specs; convert from `dp` with the host's density.
 *
 * @property nodes top-level nodes.
 * @property maxDepth how many levels are drawn below the displayed root: `1` draws only its children, `2` (default)
 *   also draws their children inside them, and so on. Deeper nodes still count toward their ancestors' sizes.
 * @property id chart identity string; also the prefix of aggregate tile ids at the top level.
 * @property palette colors for top-level nodes without an explicit [TreemapHierarchyNode.color], by index.
 * @property tileGapPx gap between adjacent tiles.
 * @property tileCornerRadiusPx tile corner radius.
 * @property nodePaddingPx inset between a parent's edges and its children.
 * @property headerHeightPx minimum height of the band above a parent's children that holds the parent's label. The
 *   band grows to fit one line of measured header text; it is only reserved while [showLabels] is true.
 * @property showLabels draw tile labels (two lines: [TreemapHierarchyNode.label] and the [valueFormatter] text) when
 *   they fit — both lines, the name only, or nothing. Names that are too long are shortened with `…`.
 * @property labelColor label color; `null` (default) picks black or white per tile, whichever contrasts more with the
 *   tile fill (always at least 4.5:1 against an opaque fill).
 * @property valueFormatter text for the second label line, given the node; `null` draws the name only.
 * @property minTileSidePx minimum width and height of every drawn tile. Within each parent, the smallest children
 *   that would come out narrower or shorter than this are merged into one **aggregate** tile whose value is their
 *   sum (a single such child is drawn as itself). If that tile is still too small it is given more area, up to half
 *   of the parent, at the expense of its siblings; only if that fails is the next-smallest sibling merged into it.
 *   Values are never changed, so the drawn tiles' values still add up to their parent, but the aggregate's area can
 *   be larger than its share. A parent whose children would end up as one aggregate of all of them draws no children,
 *   and a level where not even that fits draws nothing. `0` (default) disables all of this.
 * @property aggregateLabel label of an aggregate tile, given its member count.
 * @property aggregateColor fill of aggregate tiles; `null` uses a neutral gray.
 * @property highlight how the composable's `highlightedNodeIds` are drawn.
 * @property accessibility host-agnostic accessibility strings; [ChartAccessibilitySpec.contentDescription] overrides
 *   the generated chart summary.
 * @property maxAccessibleTiles cap on per-tile accessibility nodes. When more tiles are drawn, only the top level is
 *   exposed tile by tile (at most this many, largest first) and the chart summary says how many are left out; offer
 *   another accessible path to the rest, such as a list of the same data.
 * @property longClickActionLabel label of the accessibility action that performs a tile's long-press.
 * @property contentPadding padding around the tiling in pixels.
 */
data class TreemapHierarchySpec(
    val nodes: List<TreemapHierarchyNode>,
    val maxDepth: Int = 2,
    val id: String = "treemap",
    val palette: List<ChartColor> = ChartPalettes.Tableau10,
    val tileGapPx: Double = 2.0,
    val tileCornerRadiusPx: Double = 2.0,
    val nodePaddingPx: Double = 3.0,
    val headerHeightPx: Double = 18.0,
    val showLabels: Boolean = true,
    val labelColor: ChartColor? = null,
    val valueFormatter: TreemapValueFormatter? = null,
    val minTileSidePx: Double = 0.0,
    val aggregateLabel: TreemapAggregateLabel = DefaultTreemapAggregateLabel,
    val aggregateColor: ChartColor? = null,
    val highlight: HierarchyHighlightStyle = HierarchyHighlightStyle(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val maxAccessibleTiles: Int = 100,
    val longClickActionLabel: String = "More options",
    val contentPadding: InsetsD = InsetsD(left = 8.0, top = 8.0, right = 8.0, bottom = 8.0),
)

/**
 * Finds the node with [id] anywhere under [TreemapHierarchySpec.nodes], or `null`. Depth-first, in list order.
 */
fun TreemapHierarchySpec.findNode(id: String): TreemapHierarchyNode? = findPath(id)?.lastOrNull()

/**
 * The chain of nodes from the top level down to the node with [id] (inclusive), or `null` when no node has that id.
 * Useful for building a breadcrumb from a drill-in root.
 */
fun TreemapHierarchySpec.findPath(id: String): List<TreemapHierarchyNode>? {
    val stack = ArrayList<TreemapHierarchyNode>()

    fun visit(node: TreemapHierarchyNode): Boolean {
        stack.add(node)
        if (node.id == id) return true
        for (child in node.children) if (visit(child)) return true
        stack.removeAt(stack.size - 1)
        return false
    }
    for (node in nodes) if (visit(node)) return stack
    return null
}
