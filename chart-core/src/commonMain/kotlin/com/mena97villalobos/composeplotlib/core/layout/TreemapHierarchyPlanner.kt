package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.lighten
import com.mena97villalobos.composeplotlib.core.model.readableContentColor
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.spec.layoutSize
import kotlin.math.max
import kotlin.math.min

/**
 * Nested squarified layout for [TreemapHierarchySpec]. Each level is laid out with the same squarified algorithm as
 * the flat treemap ([squarifiedTreemap]); children are placed inside their parent's tile below a header band.
 */
internal object TreemapHierarchyPlanner {
    /** Inset between a tile's edge and its label text. */
    const val LABEL_PADDING_PX: Double = 4.0

    /** Horizontal space kept between a header's name and its right-aligned value. */
    private const val HEADER_VALUE_SPACING_PX = 8.0

    /** How much lighter each level is than its parent when colors are inherited. */
    private const val SHADE_STEP = 0.14

    private val DefaultAggregateColor = ChartColor(0xFFBAB0AC)

    /** Steps of growing a too-small tail (x1, x1.5, … ≈ x7.6) before merging another sibling into it. */
    private const val TAIL_GROWTH_STEPS = 6

    /** A grown tail never takes more than this share of its parent's area. */
    private const val MAX_TAIL_AREA_FRACTION = 0.5

    fun plan(
        input: ChartLayoutInput,
        spec: TreemapHierarchySpec,
        rootNodeId: String?,
        highlightedNodeIds: Set<String>,
        measurer: TreemapLabelMeasurer,
    ): TreemapHierarchyPlan {
        val canvas = input.canvasBounds
        val plot = canvas.inset(spec.contentPadding)
        val run = Run(spec, measurer)

        val chain = rootNodeId?.let { resolveChain(spec, it) }
        val root = chain?.lastOrNull()
        if (plot.width > 0.0 && plot.height > 0.0 && spec.maxDepth >= 1) {
            if (root == null) {
                run.layoutLevel(
                    nodes = spec.nodes,
                    parentSize = 0.0,
                    area = plot,
                    depth = 1,
                    parentPath = emptyList(),
                    parentColor = null,
                    aggregateKey = spec.id,
                )
            } else {
                val hasDrawableChildren = root.node.children.any { it.layoutSize().isDrawableSize() }
                if (hasDrawableChildren) {
                    run.layoutLevel(
                        nodes = root.node.children,
                        parentSize = root.node.layoutSize(),
                        area = plot,
                        depth = 1,
                        parentPath = chain.map { it.node.id },
                        parentColor = root.color,
                        aggregateKey = root.node.id,
                    )
                } else {
                    // A leaf as the displayed root fills the chart on its own.
                    run.placeSingle(root.node, root.color, plot, chain.dropLast(1).map { it.node.id })
                }
            }
        }

        run.applyHighlight(highlightedNodeIds)

        return TreemapHierarchyPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = canvas,
                    plotBounds = plot,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = run.fills,
                    overlayDrawCommands = run.labels,
                ),
            tiles = run.tiles,
            rootNodeId = root?.node?.id,
            rootPath = chain?.map { it.node.id } ?: emptyList(),
        )
    }

    private class ChainEntry(
        val node: TreemapHierarchyNode,
        val color: ChartColor,
    )

    /** Path from the top level to [id] with the color each node resolves to, or `null` when [id] is unknown. */
    private fun resolveChain(
        spec: TreemapHierarchySpec,
        id: String,
    ): List<ChainEntry>? {
        val stack = ArrayList<ChainEntry>()

        fun visit(
            node: TreemapHierarchyNode,
            color: ChartColor,
        ): Boolean {
            stack.add(ChainEntry(node, color))
            if (node.id == id) return true
            for (child in node.children) {
                if (visit(child, child.color ?: color.lighten(SHADE_STEP))) return true
            }
            stack.removeAt(stack.size - 1)
            return false
        }
        spec.nodes.forEachIndexed { index, node ->
            if (visit(node, node.color ?: ChartPalettes.cyclic(spec.palette, index))) return stack
        }
        return null
    }

    private fun Double.isDrawableSize(): Boolean = this > 0.0 && isFinite()

    /** A child (or the merged small children) of one parent, sized for layout. */
    private sealed class Item {
        abstract val size: Double

        class Node(
            val node: TreemapHierarchyNode,
            val index: Int,
            override val size: Double,
        ) : Item()

        class Aggregate(
            val members: List<Node>,
        ) : Item() {
            override val size: Double = members.sumOf { it.size }
        }
    }

    private class Run(
        val spec: TreemapHierarchySpec,
        val measurer: TreemapLabelMeasurer,
    ) {
        /** One fill per tile, at the tile's index. */
        val fills = ArrayList<DrawCommand>()
        val labels = ArrayList<DrawCommand>()

        /** Index of the tile each entry of [labels] belongs to. */
        private val labelOwners = ArrayList<Int>()
        val tiles = ArrayList<PlannedTreemapTile>()

        /**
         * Outlines the highlighted tiles — a tile whose id is in [ids], or an aggregate containing one — and, with
         * [com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle.dimOthersAlpha], dims every tile
         * that is neither highlighted nor inside a highlighted tile. Unknown or hidden ids draw nothing.
         */
        fun applyHighlight(ids: Set<String>) {
            if (ids.isEmpty()) return
            val highlighted = tiles.filter { it.nodeId in ids || (it.isAggregate && it.memberIds.any { id -> id in ids }) }
            if (highlighted.isEmpty()) return
            val style = spec.highlight
            val dim = style.dimOthersAlpha?.takeIf { it.isFinite() }?.coerceIn(0.0, 1.0)
            if (dim != null) {
                val highlightedIds = highlighted.mapTo(HashSet()) { it.nodeId }
                val dimmed = BooleanArray(tiles.size) { i -> tiles[i].path.none { it in highlightedIds } }
                for (i in tiles.indices) if (dimmed[i]) fills[i] = DrawCommand.Group(dim, listOf(fills[i]))
                for (j in labels.indices) if (dimmed[labelOwners[j]]) labels[j] = DrawCommand.Group(dim, listOf(labels[j]))
            }
            val width = style.outlineWidthPx.takeIf { it.isFinite() }?.coerceAtLeast(0.0) ?: 0.0
            if (width <= 0.0) return
            for (tile in highlighted) {
                val b = tile.bounds
                val half = minOf(width / 2.0, b.width / 2.0, b.height / 2.0)
                val inset = RectD(b.left + half, b.top + half, b.right - half, b.bottom - half)
                val color = style.outlineColor ?: tile.color.readableContentColor()
                labels += DrawCommand.RoundRect(inset, spec.tileCornerRadiusPx, ShapeDrawStyle.Stroke(color, width))
                labelOwners += tiles.indexOf(tile)
            }
        }

        private val gap = spec.tileGapPx.coerceAtLeast(0.0)

        /**
         * Height of one name line, measured once from a sample string: tiles shorter than this cannot show a label,
         * so most of a large treemap's tiny tiles are rejected without measuring their text.
         */
        private val nameLineHeight: Double by lazy { measurer.measure("Ag", TreemapLabelLine.Name).height }
        private val minSide = spec.minTileSidePx.takeIf { it.isFinite() }?.coerceAtLeast(0.0) ?: 0.0

        /** Lays out [nodes] in [area]; returns how many tiles were placed at this level. */
        fun layoutLevel(
            nodes: List<TreemapHierarchyNode>,
            parentSize: Double,
            area: RectD,
            depth: Int,
            parentPath: List<String>,
            parentColor: ChartColor?,
            aggregateKey: String,
        ): Int {
            val entries =
                nodes.mapIndexedNotNull { index, node ->
                    val size = node.layoutSize()
                    if (size.isDrawableSize()) Item.Node(node, index, size) else null
                }
            if (entries.isEmpty()) return 0
            val childSum = entries.sumOf { it.size }
            val remainder = if (parentSize.isFinite() && parentSize > childSum) parentSize - childSum else 0.0
            val sorted = entries.sortedByDescending { it.size }
            val placed = place(sorted, remainder, area)
            // Inside a parent tile, one aggregate of every child adds nothing over the parent itself: draw none.
            if (depth > 1 && placed.size == 1 && (placed[0].first as? Item.Aggregate)?.members?.size == sorted.size) return 0
            for ((item, rect) in placed) {
                val tileRect = rect.shrink(gap / 2.0)
                if (tileRect.width <= 0.0 || tileRect.height <= 0.0) continue
                when (item) {
                    is Item.Node -> {
                        val color = item.node.color ?: parentColor?.lighten(SHADE_STEP) ?: ChartPalettes.cyclic(spec.palette, item.index)
                        placeNode(item.node, item.size, color, tileRect, depth, parentPath)
                    }
                    is Item.Aggregate -> placeAggregate(item, tileRect, depth, parentPath, aggregateKey)
                }
            }
            return placed.size
        }

        fun placeSingle(
            node: TreemapHierarchyNode,
            color: ChartColor,
            area: RectD,
            parentPath: List<String>,
        ) {
            val tileRect = area.shrink(gap / 2.0)
            if (tileRect.width <= 0.0 || tileRect.height <= 0.0) return
            if (minSide > 0.0 && (tileRect.width < minSide || tileRect.height < minSide)) return
            placeNode(node, node.layoutSize(), color, tileRect, depth = 1, parentPath = parentPath)
        }

        private fun placeNode(
            node: TreemapHierarchyNode,
            size: Double,
            color: ChartColor,
            tileRect: RectD,
            depth: Int,
            parentPath: List<String>,
        ) {
            val path = parentPath + node.id
            val valueText = spec.valueFormatter?.invoke(node)
            fills += DrawCommand.RoundRect(tileRect, spec.tileCornerRadiusPx, ShapeDrawStyle.Fill(color))
            val tileIndex = tiles.size
            tiles +=
                PlannedTreemapTile(
                    nodeId = node.id,
                    path = path,
                    depth = depth,
                    bounds = tileRect,
                    value = size,
                    label = node.label,
                    valueText = valueText,
                    color = color,
                    hasDrawnChildren = false,
                    hasChildren = node.children.isNotEmpty(),
                )

            var childrenDrawn = 0
            var headerHeight = 0.0
            if (node.children.isNotEmpty() && depth < spec.maxDepth) {
                val pad = spec.nodePaddingPx.coerceAtLeast(0.0)
                headerHeight =
                    if (spec.showLabels) {
                        val sample = node.label.ifEmpty { "Ag" }
                        max(spec.headerHeightPx, measurer.measure(sample, TreemapLabelLine.Header).height + 2.0)
                    } else {
                        pad
                    }
                val content =
                    RectD(
                        left = tileRect.left + pad,
                        top = tileRect.top + headerHeight,
                        right = tileRect.right - pad,
                        bottom = tileRect.bottom - pad,
                    )
                if (content.right > content.left && content.bottom > content.top) {
                    childrenDrawn =
                        layoutLevel(
                            nodes = node.children,
                            parentSize = size,
                            area = content,
                            depth = depth + 1,
                            parentPath = path,
                            parentColor = color,
                            aggregateKey = node.id,
                        )
                }
            }
            if (childrenDrawn > 0) {
                tiles[tileIndex] = tiles[tileIndex].copy(hasDrawnChildren = true)
                headerLabel(node.label, valueText, color, tileRect, headerHeight, owner = tileIndex)
            } else {
                tileLabel(node.label, valueText, color, tileRect, owner = tileIndex)
            }
        }

        private fun placeAggregate(
            aggregate: Item.Aggregate,
            tileRect: RectD,
            depth: Int,
            parentPath: List<String>,
            aggregateKey: String,
        ) {
            val id = aggregateKey + TreemapHierarchyPlan.AGGREGATE_ID_SUFFIX
            val label = spec.aggregateLabel(aggregate.members.size)
            val color = spec.aggregateColor ?: DefaultAggregateColor
            val synthetic =
                TreemapHierarchyNode(
                    id = id,
                    label = label,
                    value = aggregate.size,
                    color = color,
                    children = aggregate.members.map { it.node },
                )
            val valueText = spec.valueFormatter?.invoke(synthetic)
            fills += DrawCommand.RoundRect(tileRect, spec.tileCornerRadiusPx, ShapeDrawStyle.Fill(color))
            tiles +=
                PlannedTreemapTile(
                    nodeId = id,
                    path = parentPath + id,
                    depth = depth,
                    bounds = tileRect,
                    value = aggregate.size,
                    label = label,
                    valueText = valueText,
                    color = color,
                    hasDrawnChildren = false,
                    hasChildren = false,
                    isAggregate = true,
                    memberIds = aggregate.members.map { it.node.id },
                )
            tileLabel(label, valueText, color, tileRect, owner = tiles.size - 1)
        }

        /**
         * Squarified placement of [sorted] (largest first) plus an undrawn [remainder] in [area].
         *
         * With a minimum tile side, the smallest items that cannot be drawn at that size form a **tail**: merged into
         * one aggregate, or kept as itself when it is a single item. A tail that is still too small is first given
         * more area (up to a few times the minimum), shrinking its siblings proportionally; only if that fails is the
         * next-smallest sibling merged into it. Values are never changed, so rendered values still sum to the parent;
         * only the tail's *area* may be larger than its share. Returns an empty list when nothing can be drawn.
         */
        private fun place(
            sorted: List<Item.Node>,
            remainder: Double,
            area: RectD,
        ): List<Pair<Item, RectD>> {
            if (minSide <= 0.0) return layoutItems(sorted, remainder, area, tailLayoutSize = null)
            val total = sorted.sumOf { it.size } + remainder
            val areaPx = area.width * area.height
            if (total <= 0.0 || areaPx <= 0.0) return emptyList()
            // A tile needs at least (minSide + gap)² of area before its gap is removed: a cheap first cut.
            val needed = (minSide + gap) * (minSide + gap)
            val scale = areaPx / total
            var keep = sorted.count { it.size * scale >= needed }
            while (keep >= 0) {
                val items = itemsKeeping(sorted, keep)
                val hasTail = keep < sorted.size
                val placed = layoutItems(items, remainder, area, tailLayoutSize = null)
                val violations = placed.indices.filter { i -> !placed[i].second.fitsMinSide() }
                if (violations.isEmpty()) return placed
                if (hasTail && violations.all { it == items.size - 1 }) {
                    growTail(items, remainder, area, needed)?.let { return it }
                }
                if (keep == 0) return emptyList()
                keep = (keep - max(1, violations.count { it < keep })).coerceAtLeast(0)
            }
            return emptyList()
        }

        /**
         * Retries the layout with the last item (the tail) given more area, starting from the larger of its own area
         * and the minimum tile area and growing by half each step, up to [TAIL_GROWTH_STEPS] steps and never past
         * [MAX_TAIL_AREA_FRACTION] of [area]. Returns the first layout in which every tile fits, or `null`.
         */
        private fun growTail(
            items: List<Item>,
            remainder: Double,
            area: RectD,
            needed: Double,
        ): List<Pair<Item, RectD>>? {
            val areaPx = area.width * area.height
            val tail = items.last()
            val others = items.sumOf { it.size } - tail.size + remainder
            if (others <= 0.0) return null
            var tailArea = max(tail.size / (others + tail.size) * areaPx, needed)
            repeat(TAIL_GROWTH_STEPS) {
                if (tailArea >= areaPx * MAX_TAIL_AREA_FRACTION) return null
                val layoutSize = maxOf(tail.size, tailArea * others / (areaPx - tailArea))
                val placed = layoutItems(items, remainder, area, layoutSize)
                if (placed.all { it.second.fitsMinSide() }) return placed
                tailArea *= 1.5
            }
            return null
        }

        private fun RectD.fitsMinSide(): Boolean = width - gap >= minSide && height - gap >= minSide

        /** The first [keep] items as themselves, the rest merged (a single leftover stays itself). */
        private fun itemsKeeping(
            sorted: List<Item.Node>,
            keep: Int,
        ): List<Item> {
            val rest = sorted.size - keep
            return when {
                rest <= 0 -> sorted
                rest == 1 -> sorted
                else -> sorted.subList(0, keep) + Item.Aggregate(sorted.subList(keep, sorted.size))
            }
        }

        /** Squarifies [items] (and [remainder]); [tailLayoutSize], when set, replaces the last item's size for layout only. */
        private fun layoutItems(
            items: List<Item>,
            remainder: Double,
            area: RectD,
            tailLayoutSize: Double?,
        ): List<Pair<Item, RectD>> {
            val sizes = DoubleArray(items.size + if (remainder > 0.0) 1 else 0)
            items.forEachIndexed { i, item -> sizes[i] = item.size }
            if (tailLayoutSize != null && items.isNotEmpty()) sizes[items.size - 1] = tailLayoutSize
            if (remainder > 0.0) sizes[items.size] = remainder
            val rects = squarify(sizes, area)
            return items.mapIndexed { i, item -> item to rects[i] }
        }

        private fun tileLabel(
            name: String,
            valueText: String?,
            fill: ChartColor,
            tile: RectD,
            owner: Int,
        ) {
            if (!spec.showLabels || name.isEmpty()) return
            val availableWidth = tile.width - 2 * LABEL_PADDING_PX
            val availableHeight = tile.height - 2 * LABEL_PADDING_PX
            if (availableWidth <= 0.0 || availableHeight <= 0.0 || nameLineHeight > availableHeight) return
            val nameSize = measurer.measure(name, TreemapLabelLine.Name)
            if (nameSize.height > availableHeight) return
            val shownName = fitText(name, availableWidth, TreemapLabelLine.Name) ?: return
            val color = spec.labelColor ?: fill.readableContentColor()
            val left = tile.left + LABEL_PADDING_PX
            val top = tile.top + LABEL_PADDING_PX
            val children = ArrayList<DrawCommand>(2)
            children += DrawCommand.Text(text = shownName, position = OffsetD(left, top), color = color)
            if (valueText != null && valueText.isNotEmpty()) {
                val valueSize = measurer.measure(valueText, TreemapLabelLine.Value)
                if (nameSize.height + valueSize.height <= availableHeight && valueSize.width <= availableWidth) {
                    children +=
                        DrawCommand.Text(
                            text = valueText,
                            position = OffsetD(left, top + nameSize.height),
                            color = color,
                            isYAxisTick = true,
                        )
                }
            }
            labels += DrawCommand.ClipRect(tile, children)
            labelOwners += owner
        }

        private fun headerLabel(
            name: String,
            valueText: String?,
            fill: ChartColor,
            tile: RectD,
            headerHeight: Double,
            owner: Int,
        ) {
            if (!spec.showLabels || name.isEmpty()) return
            val band = RectD(tile.left, tile.top, tile.right, min(tile.bottom, tile.top + headerHeight))
            val availableWidth = band.width - 2 * LABEL_PADDING_PX
            if (availableWidth <= 0.0) return
            val nameSize = measurer.measure(name, TreemapLabelLine.Header)
            if (nameSize.height > band.height) return
            val color = spec.labelColor ?: fill.readableContentColor()
            val top = band.top + (band.height - nameSize.height) / 2.0
            val children = ArrayList<DrawCommand>(2)
            val valueSize =
                if (valueText != null && valueText.isNotEmpty()) measurer.measure(valueText, TreemapLabelLine.Header) else null
            if (valueSize != null && nameSize.width + HEADER_VALUE_SPACING_PX + valueSize.width <= availableWidth) {
                children += DrawCommand.Text(text = name, position = OffsetD(band.left + LABEL_PADDING_PX, top), color = color, isPieSliceLabel = true)
                children +=
                    DrawCommand.Text(
                        text = valueText!!,
                        position = OffsetD(band.right - LABEL_PADDING_PX - valueSize.width, top),
                        color = color,
                        isPieSliceLabel = true,
                    )
            } else {
                val shownName = fitText(name, availableWidth, TreemapLabelLine.Header) ?: return
                children += DrawCommand.Text(text = shownName, position = OffsetD(band.left + LABEL_PADDING_PX, top), color = color, isPieSliceLabel = true)
            }
            labels += DrawCommand.ClipRect(band, children)
            labelOwners += owner
        }

        /** [text] if it fits [maxWidth], else its longest prefix plus `…` that does, else `null`. */
        private fun fitText(
            text: String,
            maxWidth: Double,
            line: TreemapLabelLine,
        ): String? {
            if (measurer.measure(text, line).width <= maxWidth) return text
            var lo = 1
            var hi = text.length - 1
            var best: String? = null
            while (lo <= hi) {
                val mid = (lo + hi) ushr 1
                val candidate = ellipsized(text, mid)
                if (measurer.measure(candidate, line).width <= maxWidth) {
                    best = candidate
                    lo = mid + 1
                } else {
                    hi = mid - 1
                }
            }
            return best
        }
    }

    /** The first [length] characters of [text] (never splitting a surrogate pair) followed by `…`. */
    internal fun ellipsized(
        text: String,
        length: Int,
    ): String {
        var end = length.coerceIn(0, text.length)
        if (end > 0 && text[end - 1].isHighSurrogate()) end -= 1
        return text.substring(0, end).trimEnd() + "…"
    }

    private fun RectD.shrink(by: Double): RectD {
        val l = left + by
        val t = top + by
        return RectD(l, t, (right - by).coerceAtLeast(l), (bottom - by).coerceAtLeast(t))
    }
}
