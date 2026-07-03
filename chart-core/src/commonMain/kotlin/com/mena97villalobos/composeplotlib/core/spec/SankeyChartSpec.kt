package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Sankey chart specification (explicit columns).
 *
 * The caller provides [nodes] with an explicit [SankeyNode.column]. The planner lays out nodes within each column
 * and routes weighted [links] between node rectangles. Column indices are normalized so the smallest used index
 * starts the flow; interior unused indices keep an empty slot.
 *
 * [SankeyLayoutSpec.orientation] selects the flow direction: [SankeyOrientation.Horizontal] (columns advance
 * left-to-right) or [SankeyOrientation.Vertical] (columns advance top-to-bottom).
 *
 * The Sankey layout is computed in normalized domain coordinates within the visible [com.mena97villalobos.composeplotlib.core.viewport.ChartViewport]
 * (commonly `x=0..1`, `y=0..1`) so pan/zoom can reuse the existing cartesian viewport state.
 */
data class SankeyChartSpec(
    val id: String,
    val nodes: List<SankeyNode>,
    val links: List<SankeyLink>,
    val layout: SankeyLayoutSpec = SankeyLayoutSpec(),
    val contentPadding: InsetsD = InsetsD.Zero,
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
) {
    init {
        requireUniqueIds(nodes.map { it.id }, "SankeyChartSpec.node ids")
        if (links.isNotEmpty()) {
            val nodeIds = nodes.mapTo(HashSet(nodes.size)) { it.id }
            links.forEach { link ->
                require(link.sourceNodeId in nodeIds) {
                    "SankeyLink.sourceNodeId '${link.sourceNodeId}' references no SankeyNode in this spec."
                }
                require(link.targetNodeId in nodeIds) {
                    "SankeyLink.targetNodeId '${link.targetNodeId}' references no SankeyNode in this spec."
                }
                require(link.value.isFinite()) {
                    "SankeyLink '${link.sourceNodeId}' → '${link.targetNodeId}' has a non-finite value (${link.value})."
                }
            }
        }
    }
}

data class SankeyNode(
    val id: String,
    val column: Int,
    val label: String = id,
    val color: ChartColor? = null,
)

data class SankeyLink(
    val sourceNodeId: String,
    val targetNodeId: String,
    val value: Double,
    val color: ChartColor? = null,
)

enum class SankeyNodeOrder {
    /** Preserve the order as provided in [SankeyChartSpec.nodes]. */
    Input,
    ValueDesc,
    IncomingDesc,
    OutgoingDesc,
    LabelAsc,
}

/**
 * Legacy cross-axis alignment names written for [SankeyOrientation.Horizontal] ("vertical" = the cross axis).
 * Prefer [SankeyColumnAlignment] via [SankeyLayoutSpec.columnAlignment], which is orientation-neutral.
 */
enum class SankeyColumnVerticalAlignment {
    Top,
    Center,
    Bottom,

    /** Distributes extra vertical space into gaps (when a column has ≥2 nodes). */
    Justify,
}

/**
 * Orientation-neutral alignment of each column's packed node stack along the **cross** axis.
 *
 * [Start] is the visual top in [SankeyOrientation.Horizontal] and the visual left in
 * [SankeyOrientation.Vertical]; [End] is the opposite edge.
 */
enum class SankeyColumnAlignment {
    Start,
    Center,
    End,

    /** Distributes extra cross-axis space into gaps (when a column has ≥2 nodes). */
    Justify,
}

/**
 * Per-node-end link stacking order. "Top/Bottom" name the **cross** axis: visual top-to-bottom in
 * [SankeyOrientation.Horizontal], left-to-right in [SankeyOrientation.Vertical].
 */
enum class SankeyLinkOrder {
    /** Preserve the order as provided in [SankeyChartSpec.links]. */
    SpecOrder,
    ValueDesc,
    ValueAsc,
    TargetTopToBottom,
    TargetBottomToTop,
    SourceTopToBottom,
    SourceBottomToTop,
}

/** Rotation of node label text for [SankeyNodeLabelPlacement.Inside]; [SankeyNodeLabelPlacement.Adjacent] labels are always horizontal. */
enum class SankeyNodeLabelOrientation {
    Horizontal,
    Vertical,
}

/**
 * Where node labels are drawn.
 */
enum class SankeyNodeLabelPlacement {
    /**
     * d3-style: beside the node, pointing inward — after the node for columns in the first half of the flow,
     * before the node for the second half. Text is horizontal; uses [SankeyNodeLabelsSpec.adjacentColor].
     */
    Adjacent,

    /**
     * Inside the node rectangle (legacy default look), honoring [SankeyNodeLabelsSpec.orientation] and
     * [SankeyNodeLabelsSpec.color]. Labels that cannot fit are ellipsized (`…`), then dropped when even a
     * single character with the ellipsis cannot fit.
     */
    Inside,
}

/**
 * How ribbon fills are colored when [SankeyLink.color] is `null` (an explicit link color always wins).
 */
enum class SankeyLinkColorMode {
    /** Every link uses [SankeyLayoutSpec.defaultLinkColor] (the pre-1.2 monochrome look). */
    Static,

    /** Each link inherits its **source** node's resolved fill color at ribbon alpha. */
    SourceNode,

    /** Each link inherits its **target** node's resolved fill color at ribbon alpha. */
    TargetNode,

    /** d3-style gradient from the source node's color to the target node's color at ribbon alpha. Default. */
    SourceToTargetGradient,
}

data class SankeyNodeLabelsSpec(
    /**
     * When false, no node label [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Text] is emitted.
     *
     * Note: [SankeyNode.label] is still used for marker/accessibility content.
     */
    val enabled: Boolean = true,
    /** Rotation for [SankeyNodeLabelPlacement.Inside] labels; ignored for [SankeyNodeLabelPlacement.Adjacent]. */
    val orientation: SankeyNodeLabelOrientation = SankeyNodeLabelOrientation.Vertical,
    /** Padding between the node rectangle and the label as a fraction of the node's flow-axis thickness. */
    val paddingFractionOfNodeWidth: Double = 0.12,
    /**
     * Text color for [SankeyNodeLabelPlacement.Inside] labels (drawn over the node fill). Pick a color that
     * contrasts with your node fills.
     */
    val color: ChartColor = ChartColor.White,
    /** Where labels are placed; see [SankeyNodeLabelPlacement]. */
    val placement: SankeyNodeLabelPlacement = SankeyNodeLabelPlacement.Adjacent,
    /** Text color for [SankeyNodeLabelPlacement.Adjacent] labels (drawn over the chart background). */
    val adjacentColor: ChartColor = ChartColor.Gray,
) {
    companion object {
        /** d3-style horizontal labels beside each node (default). */
        val DefaultAdjacent = SankeyNodeLabelsSpec()

        /** Pre-1.2 look: labels inside the node rectangle. */
        val DefaultInside = SankeyNodeLabelsSpec(placement = SankeyNodeLabelPlacement.Inside)
        val Disabled = SankeyNodeLabelsSpec(enabled = false)
    }
}

data class SankeyLinkLabelsSpec(
    /**
     * When false, no link label [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Text] is emitted.
     *
     * Note: the marker can still show link details when a link is selected.
     */
    val enabled: Boolean = true,
    /** Format a link label from source/target/value. Whole values render without a decimal part (`9.0` → `9`). */
    val formatter: (sourceLabel: String, targetLabel: String, value: Double) -> String =
        { s, t, v -> "$s → $t: ${formatChartValue(v)}" },
    val color: ChartColor = ChartColor.White,
    /** Extra alpha multiplier applied to link label colors (0..1). */
    val alpha: Double = 0.90,
) {
    companion object {
        val DefaultEnabled = SankeyLinkLabelsSpec()
        val Disabled = SankeyLinkLabelsSpec(enabled = false)
    }
}

/**
 * Layout values are expressed in **normalized domain units** (0..1), not pixels.
 *
 * Geometry is defined in flow/cross-axis terms so it works for both [orientation]s: the **flow** axis is the
 * direction columns advance (x when [SankeyOrientation.Horizontal], y when [SankeyOrientation.Vertical]); the
 * **cross** axis is where nodes within a column stack.
 */
data class SankeyLayoutSpec(
    /** Fractional inset applied inside plot bounds before node/link layout. */
    val plotPaddingFraction: Double = 0.06,
    /**
     * Node extent along the **flow** axis (width in [SankeyOrientation.Horizontal], height in
     * [SankeyOrientation.Vertical]). Automatically capped so adjacent columns can never overlap.
     */
    val nodeWidth: Double = 0.10,
    /** Minimum gap between nodes in a column along the **cross** axis. */
    val nodeGap: Double = 0.03,
    /**
     * Rounded corner radius for nodes. Defaults to `0` (square, d3-style) so ribbons attach seamlessly;
     * radii above ~2px show background notches where ribbons meet nodes.
     */
    val nodeCornerRadius: Double = 0.0,
    /** Node ordering within each column. Default preserves input order. */
    val nodeOrder: SankeyNodeOrder = SankeyNodeOrder.Input,
    /**
     * Legacy cross-axis alignment (naming assumes [SankeyOrientation.Horizontal]); used only when
     * [SankeyLayoutSpec.columnAlignment] is `null`. Prefer [SankeyLayoutSpec.columnAlignment].
     */
    val columnVerticalAlignment: SankeyColumnVerticalAlignment = SankeyColumnVerticalAlignment.Top,
    /** Link curvature factor in 0..1, controlling Bézier control point placement. */
    val linkCurvature: Double = 0.55,
    /** Minimum rendered link thickness in domain units (pre-viewport transform). */
    val minLinkThickness: Double = 0.0025,
    /** Per-node outgoing link ordering used for stacking/link routing. */
    val outgoingLinkOrder: SankeyLinkOrder = SankeyLinkOrder.TargetTopToBottom,
    /** Per-node incoming link ordering used for stacking/link routing. */
    val incomingLinkOrder: SankeyLinkOrder = SankeyLinkOrder.SourceTopToBottom,
    /** When true, links are rendered behind nodes (recommended). Also flips node/link tap priority to match. */
    val linksBehindNodes: Boolean = true,
    /** Node label drawing configuration; see [SankeyNodeLabelPlacement]. */
    val nodeLabels: SankeyNodeLabelsSpec = SankeyNodeLabelsSpec.DefaultAdjacent,
    /** Link label drawing configuration (near mid-ribbon; skipped when taller than the ribbon). */
    val linkLabels: SankeyLinkLabelsSpec = SankeyLinkLabelsSpec.DefaultEnabled,
    /** Node color when [SankeyNode.color] is null and [nodePalette] is empty. */
    val defaultNodeColor: ChartColor = ChartColor(0xFF455A64),
    /** Link color for [SankeyLinkColorMode.Static] (and legacy fallback) when [SankeyLink.color] is null. */
    val defaultLinkColor: ChartColor = ChartColor(0x80455A64),
    /** Flow direction; see [SankeyOrientation]. */
    val orientation: SankeyOrientation = SankeyOrientation.Horizontal,
    /**
     * Orientation-neutral cross-axis alignment of each column's node stack. When `null` (default), falls back
     * to the legacy [columnVerticalAlignment] mapping (Top→Start, Center→Center, Bottom→End, Justify→Justify).
     */
    val columnAlignment: SankeyColumnAlignment? = null,
    /**
     * Categorical palette for nodes without an explicit [SankeyNode.color], assigned by position in
     * [SankeyChartSpec.nodes] (stable under [nodeOrder]). Pass an empty list to restore the monochrome
     * [defaultNodeColor] look.
     */
    val nodePalette: List<ChartColor> = DefaultNodePalette,
    /** Ribbon coloring for links without an explicit [SankeyLink.color]; see [SankeyLinkColorMode]. */
    val linkColorMode: SankeyLinkColorMode = SankeyLinkColorMode.SourceToTargetGradient,
) {
    companion object {
        /** Tableau-10 categorical palette: professional, colorblind-considerate defaults for node fills. */
        val DefaultNodePalette: List<ChartColor> =
            listOf(
                ChartColor(0xFF4E79A7),
                ChartColor(0xFFF28E2B),
                ChartColor(0xFF59A14F),
                ChartColor(0xFFE15759),
                ChartColor(0xFF76B7B2),
                ChartColor(0xFFEDC948),
                ChartColor(0xFFB07AA1),
                ChartColor(0xFFFF9DA7),
                ChartColor(0xFF9C755F),
                ChartColor(0xFFBAB0AC),
            )
    }
}

/** [SankeyLayoutSpec.columnAlignment] when set, otherwise the legacy [SankeyColumnVerticalAlignment] mapped to cross-axis terms. */
internal fun SankeyLayoutSpec.resolvedColumnAlignment(): SankeyColumnAlignment =
    columnAlignment ?: when (columnVerticalAlignment) {
        SankeyColumnVerticalAlignment.Top -> SankeyColumnAlignment.Start
        SankeyColumnVerticalAlignment.Center -> SankeyColumnAlignment.Center
        SankeyColumnVerticalAlignment.Bottom -> SankeyColumnAlignment.End
        SankeyColumnVerticalAlignment.Justify -> SankeyColumnAlignment.Justify
    }
