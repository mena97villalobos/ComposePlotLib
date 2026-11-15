package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One drawn tile of a hierarchical treemap.
 *
 * @property nodeId the node's [com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode.id], or a
 *   synthetic id ending in [TreemapHierarchyPlan.AGGREGATE_ID_SUFFIX] for an aggregate tile.
 * @property path ids from the top level of the spec down to this tile (inclusive). For an aggregate tile the last
 *   element is the aggregate's synthetic id.
 * @property depth `1` for the displayed root's children, `2` for their children, and so on.
 * @property bounds the drawn tile in canvas pixels (after the tile gap).
 * @property value the node's [com.mena97villalobos.composeplotlib.core.spec.layoutSize], or the sum of the
 *   members for an aggregate.
 * @property label node label (or the aggregate label).
 * @property valueText the spec's value-formatter text for this tile, or `null` without a formatter.
 * @property color resolved fill.
 * @property hasDrawnChildren true when this tile's children are drawn inside it (it then shows a header band).
 * @property hasChildren true when the node has children at all (drawn or not); aggregates never do.
 * @property isAggregate true for a tile that merges several nodes that were too small to draw on their own.
 * @property memberIds for an aggregate, the ids of the merged nodes (largest first); empty otherwise.
 * @property labelBounds the content rectangle the tile's label is laid out in, in canvas pixels: [bounds] less the
 *   spec's label inset ([com.mena97villalobos.composeplotlib.core.spec.labelInsetPx]) on every side, or, when
 *   [hasDrawnChildren], the header band less the inset on its left and right. It is the same whether or not the tile
 *   is highlighted. `null` when labels are off or the rectangle is empty. Overlays can keep their decorations out of
 *   it.
 * @property labels the label lines actually drawn for this tile, in drawing order (empty when none fits).
 */
data class PlannedTreemapTile(
    val nodeId: String,
    val path: List<String>,
    val depth: Int,
    val bounds: RectD,
    val value: Double,
    val label: String,
    val valueText: String?,
    val color: ChartColor,
    val hasDrawnChildren: Boolean,
    val hasChildren: Boolean,
    val isAggregate: Boolean = false,
    val memberIds: List<String> = emptyList(),
    val labelBounds: RectD? = null,
    val labels: List<PlannedTreemapLabel> = emptyList(),
) {
    /** The 1.4 constructor, kept so code compiled against it still links. */
    @Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
    constructor(
        nodeId: String,
        path: List<String>,
        depth: Int,
        bounds: RectD,
        value: Double,
        label: String,
        valueText: String?,
        color: ChartColor,
        hasDrawnChildren: Boolean,
        hasChildren: Boolean,
        isAggregate: Boolean = false,
        memberIds: List<String> = emptyList(),
    ) : this(
        nodeId = nodeId,
        path = path,
        depth = depth,
        bounds = bounds,
        value = value,
        label = label,
        valueText = valueText,
        color = color,
        hasDrawnChildren = hasDrawnChildren,
        hasChildren = hasChildren,
        isAggregate = isAggregate,
        memberIds = memberIds,
        labelBounds = null,
        labels = emptyList(),
    )

    /** The 1.4 `copy`, kept so code compiled against it still links; it keeps [labelBounds] and [labels]. */
    @Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
    fun copy(
        nodeId: String = this.nodeId,
        path: List<String> = this.path,
        depth: Int = this.depth,
        bounds: RectD = this.bounds,
        value: Double = this.value,
        label: String = this.label,
        valueText: String? = this.valueText,
        color: ChartColor = this.color,
        hasDrawnChildren: Boolean = this.hasDrawnChildren,
        hasChildren: Boolean = this.hasChildren,
        isAggregate: Boolean = this.isAggregate,
        memberIds: List<String> = this.memberIds,
    ): PlannedTreemapTile =
        copy(
            nodeId = nodeId,
            path = path,
            depth = depth,
            bounds = bounds,
            value = value,
            label = label,
            valueText = valueText,
            color = color,
            hasDrawnChildren = hasDrawnChildren,
            hasChildren = hasChildren,
            isAggregate = isAggregate,
            memberIds = memberIds,
            labelBounds = labelBounds,
            labels = labels,
        )
}

/**
 * One line of text the treemap draws for a tile.
 *
 * @property text the text as drawn (a name may be shortened with `…`).
 * @property line which line it is; [TreemapLabelLine.Header] for a parent's header band, whose value is a second
 *   `Header` line aligned to the band's right edge.
 * @property bounds the text's measured box in canvas pixels: its top-left corner is where the text is drawn and its
 *   size is what the planner's [TreemapLabelMeasurer] returned for [text].
 */
data class PlannedTreemapLabel(
    val text: String,
    val line: TreemapLabelLine,
    val bounds: RectD,
)

/**
 * Planner output for a hierarchical treemap.
 *
 * @property layout draw commands (tile fills in [ChartLayoutPlan.plotDrawCommands], labels in
 *   [ChartLayoutPlan.overlayDrawCommands]).
 * @property tiles every drawn tile in **pre-order**: a parent always precedes its children, so the last tile that
 *   contains a point is the deepest one.
 * @property rootNodeId the displayed root actually used, or `null` for the top level (an unknown requested root
 *   falls back to the top level).
 * @property rootPath ids from the top level down to [rootNodeId] (inclusive); empty at the top level.
 */
data class TreemapHierarchyPlan(
    val layout: ChartLayoutPlan,
    val tiles: List<PlannedTreemapTile>,
    val rootNodeId: String?,
    val rootPath: List<String>,
) {
    companion object {
        /** Suffix of the synthetic id of an aggregate tile: `"<parent id or spec id>" + AGGREGATE_ID_SUFFIX`. */
        const val AGGREGATE_ID_SUFFIX: String = "#aggregate"
    }
}

/** Which line of a treemap label a piece of text belongs to; each line may use a different text style. */
enum class TreemapLabelLine {
    /** First line inside a tile: the node label. */
    Name,

    /** Second line inside a tile: the value-formatter text. */
    Value,

    /** A parent's label, drawn in the header band above its children. */
    Header,
}

/**
 * Measures label text for the treemap planner so it can decide which lines fit and where to shorten names. Hosts
 * pass a measurer backed by their real text engine (the Compose `TreemapChart` does); [estimate] is a font-free
 * fallback for tests and headless planning.
 */
fun interface TreemapLabelMeasurer {
    /** Size in pixels of [text] rendered as [line]. */
    fun measure(
        text: String,
        line: TreemapLabelLine,
    ): SizeD

    companion object {
        /** Fixed-advance estimate: every character is [charWidthPx] wide and every line is [lineHeightPx] tall. */
        fun estimate(
            charWidthPx: Double = 7.0,
            lineHeightPx: Double = 16.0,
        ): TreemapLabelMeasurer = TreemapLabelMeasurer { text, _ -> SizeD(text.length * charWidthPx, lineHeightPx) }
    }
}
