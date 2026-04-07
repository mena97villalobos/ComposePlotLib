package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Sankey chart specification (explicit columns).
 *
 * The caller provides [nodes] with an explicit [SankeyNode.column]. The planner lays out nodes within each column
 * and routes weighted [links] between node rectangles.
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
)

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

enum class SankeyColumnVerticalAlignment {
    Top,
    Center,
    Bottom,

    /** Distributes extra vertical space into gaps (when a column has ≥2 nodes). */
    Justify,
}

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

enum class SankeyNodeLabelOrientation {
    Horizontal,
    Vertical,
}

data class SankeyNodeLabelsSpec(
    /**
     * When false, no node label [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Text] is emitted.
     *
     * Note: [SankeyNode.label] is still used for marker/accessibility content.
     */
    val enabled: Boolean = true,
    val orientation: SankeyNodeLabelOrientation = SankeyNodeLabelOrientation.Vertical,
    /** Padding inside the node rectangle as a fraction of the node width. */
    val paddingFractionOfNodeWidth: Double = 0.12,
    val color: ChartColor = ChartColor.White,
) {
    companion object {
        val DefaultInside = SankeyNodeLabelsSpec()
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
    /** Format a link label from source/target/value. */
    val formatter: (sourceLabel: String, targetLabel: String, value: Double) -> String =
        { s, t, v -> "$s → $t: $v" },
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
 */
data class SankeyLayoutSpec(
    /** Fractional inset applied inside plot bounds before node/link layout. */
    val plotPaddingFraction: Double = 0.06,
    /** Node width in normalized domain units. */
    val nodeWidth: Double = 0.10,
    /** Minimum vertical gap between nodes in a column, in normalized domain units. */
    val nodeGap: Double = 0.03,
    /** Rounded corner radius for nodes, in normalized domain units. */
    val nodeCornerRadius: Double = 0.02,
    /** Node ordering within each column. Default preserves input order. */
    val nodeOrder: SankeyNodeOrder = SankeyNodeOrder.Input,
    /** Per-column vertical alignment of the packed node stack. */
    val columnVerticalAlignment: SankeyColumnVerticalAlignment = SankeyColumnVerticalAlignment.Top,
    /** Link curvature factor in 0..1, controlling Bézier control point placement. */
    val linkCurvature: Double = 0.55,
    /** Minimum rendered link thickness in domain units (pre-viewport transform). */
    val minLinkThickness: Double = 0.0025,
    /** Per-node outgoing link ordering used for stacking/link routing. */
    val outgoingLinkOrder: SankeyLinkOrder = SankeyLinkOrder.TargetTopToBottom,
    /** Per-node incoming link ordering used for stacking/link routing. */
    val incomingLinkOrder: SankeyLinkOrder = SankeyLinkOrder.SourceTopToBottom,
    /** When true, links are rendered behind nodes (recommended). */
    val linksBehindNodes: Boolean = true,
    /** Node label drawing configuration (inside node rectangles). */
    val nodeLabels: SankeyNodeLabelsSpec = SankeyNodeLabelsSpec.DefaultInside,
    /** Link label drawing configuration (near mid-ribbon). */
    val linkLabels: SankeyLinkLabelsSpec = SankeyLinkLabelsSpec.DefaultEnabled,
    /** Default node color when [SankeyNode.color] is null. */
    val defaultNodeColor: ChartColor = ChartColor(0xFF455A64),
    /** Default link color when [SankeyLink.color] is null. */
    val defaultLinkColor: ChartColor = ChartColor(0x80455A64),
)
