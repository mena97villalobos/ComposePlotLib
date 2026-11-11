package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.lighten
import com.mena97villalobos.composeplotlib.core.model.readableContentColor
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode
import com.mena97villalobos.composeplotlib.core.spec.nodeId
import com.mena97villalobos.composeplotlib.core.spec.totalValue

/**
 * Sunburst planning with node identity: the same rings, angles, colors and draw commands as
 * [ChartPlanner.planSunburstChart] (see [sunburstLayout]), plus a [PlannedSunburstSegment] per drawn node, an
 * optional displayed root other than the spec's root, and highlighting.
 */
internal object SunburstHierarchyPlanner {
    private val DefaultRootColor = ChartColor(0xFFBAB0AC)
    private const val SHADE_STEP = 0.14

    private class Placed(
        val node: SunburstNode,
        val path: List<String>,
        val depth: Int,
        val start: Double,
        val sweep: Double,
        val color: ChartColor,
    )

    fun plan(
        input: ChartLayoutInput,
        spec: SunburstChartSpec,
        rootNodeId: String?,
        highlightedNodeIds: Set<String>,
        highlight: HierarchyHighlightStyle,
    ): SunburstHierarchyPlan {
        val canvas = input.canvasBounds
        val plotBounds = canvas.inset(spec.contentPadding)
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)

        val chain = rootNodeId?.let { findChain(spec.root, it) }
        val rootEntry = chain?.lastOrNull()
        val displayed = rootEntry?.node ?: spec.root
        val displayedColor = rootEntry?.color ?: (spec.root.color ?: DefaultRootColor)
        val displayedAbsoluteDepth = (chain?.size ?: 1) - 1
        val placed = ArrayList<Placed>()
        layoutNode(
            node = displayed,
            path = chain?.map { it.node.nodeId } ?: listOf(spec.root.nodeId),
            depth = 0,
            absoluteDepth = displayedAbsoluteDepth,
            start = spec.startAngleDegrees,
            sweep = spec.sweepDegrees,
            color = displayedColor,
            includeSelf = spec.showRoot,
            palette = ChartPalettes.Tableau10,
            out = placed,
        )

        val maxDepth = placed.maxOfOrNull { it.depth } ?: 0
        val ringCount = maxDepth + 1
        val bandWidth = if (ringCount > 0) radial.outerRadius / ringCount else 0.0
        val gap = spec.ringGapPx.coerceAtLeast(0.0)
        val segments = ArrayList<PlannedSunburstSegment>()
        val commands = ArrayList<DrawCommand>()
        if (radial.outerRadius > 0.0 && bandWidth > 0.0) {
            for (p in placed) {
                if (p.sweep == 0.0) continue
                val outer = ((p.depth + 1) * bandWidth - gap / 2.0).coerceAtLeast(0.0)
                val inner = if (p.depth == 0) 0.0 else (p.depth * bandWidth + gap / 2.0).coerceIn(0.0, outer)
                commands += arc(radial, p.depth, outer, inner, p.start, p.sweep, ShapeDrawStyle.Fill(p.color))
                segments +=
                    PlannedSunburstSegment(
                        nodeId = p.node.nodeId,
                        path = p.path,
                        depth = p.depth,
                        startAngleDegrees = p.start,
                        sweepAngleDegrees = p.sweep,
                        innerRadius = inner,
                        outerRadius = outer,
                        value = p.node.totalValue(),
                        label = p.node.label,
                        color = p.color,
                        hasChildren = p.node.children.isNotEmpty(),
                    )
            }
        }
        val overlay = ArrayList<DrawCommand>()
        applyHighlight(radial, segments, commands, overlay, highlightedNodeIds, highlight)

        return SunburstHierarchyPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = canvas,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = commands,
                    overlayDrawCommands = overlay,
                ),
            segments = segments,
            center = radial.center,
            rootNodeId = rootEntry?.node?.nodeId,
            rootPath = chain?.map { it.node.nodeId } ?: emptyList(),
        )
    }

    private fun arc(
        radial: PieRadialLayout,
        depth: Int,
        outer: Double,
        inner: Double,
        start: Double,
        sweep: Double,
        style: ShapeDrawStyle,
    ): DrawCommand.Arc =
        if (depth == 0) {
            DrawCommand.Arc(radial.center, outer, start, sweep, useCenter = true, style = style, innerRadius = null)
        } else {
            DrawCommand.Arc(radial.center, outer, start, sweep, useCenter = false, style = style, innerRadius = inner)
        }

    /** Pre-order layout matching [sunburstLayout]'s angles and colors; [absoluteDepth] drives the color rule. */
    private fun layoutNode(
        node: SunburstNode,
        path: List<String>,
        depth: Int,
        absoluteDepth: Int,
        start: Double,
        sweep: Double,
        color: ChartColor,
        includeSelf: Boolean,
        palette: List<ChartColor>,
        out: MutableList<Placed>,
    ) {
        if (includeSelf) out += Placed(node, path, depth, start, sweep, color)
        val total = node.children.sumOf { it.totalValue() }
        if (total <= 0.0) return
        var angle = start
        node.children.forEachIndexed { index, child ->
            val childSweep = sweep * child.totalValue() / total
            val childColor =
                when {
                    child.color != null -> child.color
                    absoluteDepth == 0 -> ChartPalettes.cyclic(palette, index)
                    else -> color.lighten(SHADE_STEP)
                }
            layoutNode(child, path + child.nodeId, depth + 1, absoluteDepth + 1, angle, childSweep, childColor, true, palette, out)
            angle += childSweep
        }
    }

    private class ChainEntry(
        val node: SunburstNode,
        val color: ChartColor,
    )

    /** Path from [root] to the first node (pre-order) whose id is [id], with resolved colors; `null` if absent. */
    private fun findChain(
        root: SunburstNode,
        id: String,
    ): List<ChainEntry>? {
        val stack = ArrayList<ChainEntry>()

        fun visit(
            node: SunburstNode,
            color: ChartColor,
            depth: Int,
        ): Boolean {
            stack += ChainEntry(node, color)
            if (node.nodeId == id) return true
            node.children.forEachIndexed { index, child ->
                val childColor =
                    child.color ?: if (depth == 0) ChartPalettes.cyclic(ChartPalettes.Tableau10, index) else color.lighten(SHADE_STEP)
                if (visit(child, childColor, depth + 1)) return true
            }
            stack.removeAt(stack.size - 1)
            return false
        }
        return if (visit(root, root.color ?: DefaultRootColor, 0)) stack else null
    }

    /** Outlines highlighted segments and, with [HierarchyHighlightStyle.dimOthersAlpha], dims the rest. */
    private fun applyHighlight(
        radial: PieRadialLayout,
        segments: List<PlannedSunburstSegment>,
        fills: MutableList<DrawCommand>,
        overlay: MutableList<DrawCommand>,
        ids: Set<String>,
        style: HierarchyHighlightStyle,
    ) {
        if (ids.isEmpty()) return
        val highlighted = segments.filter { it.nodeId in ids }
        if (highlighted.isEmpty()) return
        val dim = style.dimOthersAlpha?.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0)
        if (dim != null) {
            val highlightedIds = highlighted.mapTo(HashSet()) { it.nodeId }
            segments.forEachIndexed { i, segment ->
                if (segment.path.none { it in highlightedIds }) fills[i] = DrawCommand.Group(dim, listOf(fills[i]))
            }
        }
        val width = style.outlineWidthPx.takeIf { it.isFinite() }?.coerceAtLeast(0.0) ?: 0.0
        if (width <= 0.0) return
        for (segment in highlighted) {
            val half = width / 2.0
            val outer = (segment.outerRadius - half).coerceAtLeast(0.0)
            val inner = if (segment.depth == 0) 0.0 else (segment.innerRadius + half).coerceAtMost(outer)
            val color = style.outlineColor ?: segment.color.readableContentColor()
            overlay += arc(radial, segment.depth, outer, inner, segment.startAngleDegrees, segment.sweepAngleDegrees, ShapeDrawStyle.Stroke(color, width))
        }
    }
}
