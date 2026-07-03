package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.selection.SankeyHitResolver
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyColumnAlignment
import com.mena97villalobos.composeplotlib.core.spec.SankeyColumnVerticalAlignment
import com.mena97villalobos.composeplotlib.core.spec.SankeyLayoutSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyLink
import com.mena97villalobos.composeplotlib.core.spec.SankeyLinkColorMode
import com.mena97villalobos.composeplotlib.core.spec.SankeyNode
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeLabelOrientation
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeLabelPlacement
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeLabelsSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeOrder
import com.mena97villalobos.composeplotlib.core.spec.SankeyOrientation
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SankeyChartPlannerTest {
    private val eps = 1e-6

    private fun input(
        width: Double = 400.0,
        height: Double = 300.0,
        measurements: ChartTextMeasurementInput = ChartTextMeasurementInput(),
    ) = ChartLayoutInput(
        canvasSize = SizeD(width, height),
        viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(0.0, 1.0),
            ),
        textMeasurements = measurements,
    )

    private fun spec(
        nodes: List<SankeyNode>,
        links: List<SankeyLink>,
        layout: SankeyLayoutSpec = SankeyLayoutSpec(),
    ) = SankeyChartSpec(id = "s", nodes = nodes, links = links, layout = layout)

    private fun plan(
        spec: SankeyChartSpec,
        input: ChartLayoutInput = input(),
    ) = ChartPlanner.planSankeyChart(input = input, spec = spec, selection = null)

    private fun linkSelection(
        spec: SankeyChartSpec,
        plan: SankeyChartPlan,
        idx: Int,
    ): ChartSelection? {
        val l = plan.links.getOrNull(idx) ?: return null
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = spec.id,
                    pointIndex = idx,
                    point = XyPoint(0.0, 0.0),
                    layerId = SankeyHitResolver.LayerLink,
                ),
            pixelOffset = l.markerAnchor,
            markerAnchor = l.markerAnchor,
            hitBounds = l.pixelBounds,
        )
    }

    private fun collectRoundRects(commands: List<DrawCommand>): List<DrawCommand.RoundRect> {
        val out = mutableListOf<DrawCommand.RoundRect>()

        fun visit(cmd: DrawCommand) {
            when (cmd) {
                is DrawCommand.RoundRect -> out.add(cmd)
                is DrawCommand.Group -> cmd.children.forEach(::visit)
                is DrawCommand.ClipRect -> cmd.children.forEach(::visit)
                else -> Unit
            }
        }
        commands.forEach(::visit)
        return out
    }

    private fun collectPaths(commands: List<DrawCommand>): List<DrawCommand.Path> {
        val out = mutableListOf<DrawCommand.Path>()

        fun visit(cmd: DrawCommand) {
            when (cmd) {
                is DrawCommand.Path -> out.add(cmd)
                is DrawCommand.Group -> cmd.children.forEach(::visit)
                is DrawCommand.ClipRect -> cmd.children.forEach(::visit)
                else -> Unit
            }
        }
        commands.forEach(::visit)
        return out
    }

    private fun collectTexts(commands: List<DrawCommand>): List<DrawCommand.Text> {
        val out = mutableListOf<DrawCommand.Text>()

        fun visit(cmd: DrawCommand) {
            when (cmd) {
                is DrawCommand.Text -> out.add(cmd)
                is DrawCommand.Group -> cmd.children.forEach(::visit)
                is DrawCommand.ClipRect -> cmd.children.forEach(::visit)
                else -> Unit
            }
        }
        commands.forEach(::visit)
        return out
    }

    // --- Column packing / scale ---------------------------------------------------------------

    @Test
    fun fanOut_1to10_allNodesFitColumnAndStayVisible() {
        val targets = (0 until 10).map { SankeyNode("t$it", column = 1) }
        val s =
            spec(
                nodes = listOf(SankeyNode("S", column = 0)) + targets,
                links = targets.map { SankeyLink("S", it.id, 10.0) },
            )
        val p = plan(s)
        val plot = p.layout.plotBounds
        val pad = 0.06 * plot.height
        val col1 = p.nodes.filter { it.nodeId.startsWith("t") }
        assertEquals(10, col1.size)
        col1.forEach { n ->
            assertTrue(n.pixelBounds.height > 0.0, "node ${n.nodeId} collapsed to zero height")
            assertTrue(
                n.pixelBounds.bottom <= plot.bottom - pad + 1.0,
                "node ${n.nodeId} clipped at plot bottom (${n.pixelBounds.bottom} > ${plot.bottom - pad})",
            )
        }
        // Equal values ⇒ equal heights.
        val heights = col1.map { it.pixelBounds.height }
        heights.forEach { h -> assertTrue(abs(h - heights[0]) < 1e-3) }
    }

    @Test
    fun equalTotals_differentNodeCounts_noOverflow() {
        val s =
            spec(
                nodes =
                    listOf(
                        SankeyNode("A", column = 0),
                        SankeyNode("B", column = 1),
                        SankeyNode("C", column = 1),
                    ),
                links = listOf(SankeyLink("A", "B", 6.0), SankeyLink("A", "C", 4.0)),
            )
        val p = plan(s)
        val plot = p.layout.plotBounds
        val pad = 0.06 * plot.height
        val b = p.nodes.first { it.nodeId == "B" }
        val c = p.nodes.first { it.nodeId == "C" }
        assertTrue(b.pixelBounds.bottom <= plot.bottom - pad + 1.0)
        assertTrue(c.pixelBounds.bottom <= plot.bottom - pad + 1.0)
        // Heights stay value-proportional (6:4) — no clipping-induced squash.
        assertTrue(abs(b.pixelBounds.height / c.pixelBounds.height - 1.5) < 1e-3)
    }

    // --- Alignment / ordering in pixel space --------------------------------------------------

    @Test
    fun alignmentStart_anchorsShortColumnAtVisualTop() {
        // Column 1 total (4) is under column 0 total (10) ⇒ column 1 is short.
        val s =
            spec(
                nodes =
                    listOf(
                        SankeyNode("A", column = 0),
                        SankeyNode("B", column = 0),
                        SankeyNode("T", column = 1),
                    ),
                links = listOf(SankeyLink("A", "T", 2.0), SankeyLink("B", "T", 2.0), SankeyLink("A", "B", 8.0)),
                layout = SankeyLayoutSpec(columnAlignment = SankeyColumnAlignment.Start),
            )
        val p = plan(s)
        val plot = p.layout.plotBounds
        val pad = 0.06 * plot.height
        val t = p.nodes.first { it.nodeId == "T" }
        assertTrue(
            abs(t.pixelBounds.top - (plot.top + pad)) < 1.0,
            "Start alignment must anchor at the visual top (got top=${t.pixelBounds.top}, expected ≈${plot.top + pad})",
        )
    }

    @Test
    fun alignmentEnd_anchorsShortColumnAtVisualBottom() {
        val s =
            spec(
                nodes =
                    listOf(
                        SankeyNode("A", column = 0),
                        SankeyNode("B", column = 0),
                        SankeyNode("T", column = 1),
                    ),
                links = listOf(SankeyLink("A", "T", 2.0), SankeyLink("B", "T", 2.0), SankeyLink("A", "B", 8.0)),
                layout = SankeyLayoutSpec(columnAlignment = SankeyColumnAlignment.End),
            )
        val p = plan(s)
        val plot = p.layout.plotBounds
        val pad = 0.06 * plot.height
        val t = p.nodes.first { it.nodeId == "T" }
        assertTrue(
            abs(t.pixelBounds.bottom - (plot.bottom - pad)) < 1.0,
            "End alignment must anchor at the visual bottom (got bottom=${t.pixelBounds.bottom}, expected ≈${plot.bottom - pad})",
        )
    }

    @Test
    fun legacyColumnVerticalAlignmentTop_mapsToStart() {
        fun build(layout: SankeyLayoutSpec) =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("T", column = 1), SankeyNode("U", column = 1)),
                    links = listOf(SankeyLink("A", "T", 3.0), SankeyLink("A", "U", 1.0)),
                    layout = layout,
                ),
            )
        val legacy = build(SankeyLayoutSpec(columnVerticalAlignment = SankeyColumnVerticalAlignment.Top))
        val neutral = build(SankeyLayoutSpec(columnAlignment = SankeyColumnAlignment.Start))
        legacy.nodes.zip(neutral.nodes).forEach { (l, n) ->
            assertEquals(l.pixelBounds, n.pixelBounds, "legacy Top and neutral Start must produce identical layout")
        }
    }

    @Test
    fun nodeOrderInput_firstDeclaredNodeIsVisuallyTopmost() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("First", column = 1), SankeyNode("Second", column = 1)),
                links = listOf(SankeyLink("A", "First", 1.0), SankeyLink("A", "Second", 1.0)),
                layout = SankeyLayoutSpec(nodeOrder = SankeyNodeOrder.Input),
            )
        val p = plan(s)
        val first = p.nodes.first { it.nodeId == "First" }
        val second = p.nodes.first { it.nodeId == "Second" }
        assertTrue(
            first.pixelBounds.top < second.pixelBounds.top,
            "first declared node must render above later ones (Input order)",
        )
    }

    @Test
    fun nodeOrderValueDesc_largestNodeVisuallyTopmost() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("Small", column = 1), SankeyNode("Big", column = 1)),
                links = listOf(SankeyLink("A", "Small", 1.0), SankeyLink("A", "Big", 9.0)),
                layout = SankeyLayoutSpec(nodeOrder = SankeyNodeOrder.ValueDesc),
            )
        val p = plan(s)
        val big = p.nodes.first { it.nodeId == "Big" }
        val small = p.nodes.first { it.nodeId == "Small" }
        assertTrue(big.pixelBounds.top < small.pixelBounds.top, "ValueDesc must place the largest node at the visual top")
    }

    @Test
    fun outgoingLinkOrder_targetTopToBottom_stacksByTargetPixelTop() {
        val s =
            spec(
                nodes =
                    listOf(
                        SankeyNode("S", column = 0),
                        SankeyNode("Top", column = 1),
                        SankeyNode("Bottom", column = 1),
                    ),
                // Declared bottom-target first: ordering must still stack the top-target band first.
                links = listOf(SankeyLink("S", "Bottom", 1.0), SankeyLink("S", "Top", 1.0)),
            )
        val p = plan(s)
        val toTop = p.links.first { it.targetNodeId == "Top" }
        val toBottom = p.links.first { it.targetNodeId == "Bottom" }
        // polygon[0] is the source attach band's start point.
        assertTrue(
            toTop.pixelPolygon[0].y < toBottom.pixelPolygon[0].y,
            "TargetTopToBottom must allocate the band to the visually-top target first",
        )
    }

    // --- Orientation ---------------------------------------------------------------------------

    @Test
    fun transpositionInvariant_horizontalVsVertical() {
        val nodes =
            listOf(
                SankeyNode("A", column = 0),
                SankeyNode("B", column = 1),
                SankeyNode("C", column = 1),
                SankeyNode("D", column = 2),
            )
        val links =
            listOf(
                SankeyLink("A", "B", 6.0),
                SankeyLink("A", "C", 4.0),
                SankeyLink("B", "D", 6.0),
                SankeyLink("C", "D", 4.0),
            )
        val square = input(width = 400.0, height = 400.0)
        val h = plan(spec(nodes, links, SankeyLayoutSpec(orientation = SankeyOrientation.Horizontal)), square)
        val v = plan(spec(nodes, links, SankeyLayoutSpec(orientation = SankeyOrientation.Vertical)), square)
        val plot = h.layout.plotBounds
        assertTrue(abs(plot.width - plot.height) < eps, "test requires a square plot")

        fun transpose(pt: OffsetD): OffsetD = OffsetD(plot.left + (pt.y - plot.top), plot.top + (pt.x - plot.left))

        h.nodes.zip(v.nodes).forEach { (hn, vn) ->
            assertEquals(hn.nodeId, vn.nodeId)
            val tl = transpose(OffsetD(hn.pixelBounds.left, hn.pixelBounds.top))
            val br = transpose(OffsetD(hn.pixelBounds.right, hn.pixelBounds.bottom))
            assertTrue(abs(tl.x - vn.pixelBounds.left) < 1e-3, "node ${hn.nodeId} left")
            assertTrue(abs(tl.y - vn.pixelBounds.top) < 1e-3, "node ${hn.nodeId} top")
            assertTrue(abs(br.x - vn.pixelBounds.right) < 1e-3, "node ${hn.nodeId} right")
            assertTrue(abs(br.y - vn.pixelBounds.bottom) < 1e-3, "node ${hn.nodeId} bottom")
        }
        h.links.zip(v.links).forEach { (hl, vl) ->
            assertEquals(hl.pixelPolygon.size, vl.pixelPolygon.size)
            hl.pixelPolygon.zip(vl.pixelPolygon).forEach { (hp, vp) ->
                val t = transpose(hp)
                assertTrue(abs(t.x - vp.x) < 1e-3 && abs(t.y - vp.y) < 1e-3, "link polygon point mismatch")
            }
            val ta = transpose(hl.markerAnchor)
            assertTrue(abs(ta.x - vl.markerAnchor.x) < 1e-3 && abs(ta.y - vl.markerAnchor.y) < 1e-3)
        }
    }

    @Test
    fun verticalOrientation_flowAdvancesDownward() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 1.0)),
                layout = SankeyLayoutSpec(orientation = SankeyOrientation.Vertical),
            )
        val p = plan(s)
        val a = p.nodes.first { it.nodeId == "A" }
        val b = p.nodes.first { it.nodeId == "B" }
        assertTrue(a.pixelBounds.bottom <= b.pixelBounds.top, "column 0 must sit above column 1 in Vertical orientation")
        assertTrue(a.pixelBounds.width > a.pixelBounds.height, "vertical-flow nodes are wide bars")
    }

    // --- Link stacking -------------------------------------------------------------------------

    @Test
    fun minThicknessSaturation_renormalizesInsteadOfZeroing() {
        val smalls = (0 until 20).map { SankeyNode("m$it", column = 1) }
        val s =
            spec(
                nodes = listOf(SankeyNode("S", column = 0), SankeyNode("Big", column = 1)) + smalls,
                links =
                    listOf(SankeyLink("S", "Big", 10.0)) +
                        smalls.map { SankeyLink("S", it.id, 0.001) },
            )
        val p = plan(s)
        val sNode = p.nodes.first { it.nodeId == "S" }
        assertEquals(21, p.links.size)
        p.links.forEach { l ->
            val sourceBandStart = l.pixelPolygon.first()
            val sourceBandEnd = l.pixelPolygon.last()
            val extent = abs(sourceBandEnd.y - sourceBandStart.y)
            assertTrue(extent > 0.0, "link ${l.sourceNodeId}→${l.targetNodeId} source band collapsed to zero")
            assertTrue(
                sourceBandEnd.y <= sNode.pixelBounds.bottom + 1.0 && sourceBandStart.y >= sNode.pixelBounds.top - 1.0,
                "band must stay within the source node's extent",
            )
        }
    }

    @Test
    fun adjacentColumnLinks_flowForward_across12Columns() {
        val nodes = (0 until 12).map { SankeyNode("n$it", column = it) }
        val links = (0 until 11).map { SankeyLink("n$it", "n${it + 1}", 1.0) }
        val p = plan(spec(nodes, links))
        assertEquals(11, p.links.size)
        p.links.forEach { l ->
            val sourceX = l.pixelPolygon.first().x
            val targetX = l.pixelPolygon[16].x // last sample of the top edge = target attach point
            assertTrue(
                sourceX < targetX,
                "link ${l.sourceNodeId}→${l.targetNodeId} runs backwards ($sourceX >= $targetX): columns overlap",
            )
        }
    }

    // --- Column index normalization -------------------------------------------------------------

    @Test
    fun negativeColumns_produceSameLayoutAsZeroBased() {
        val negative =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = -1), SankeyNode("B", column = 0)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                ),
            )
        val zeroBased =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                ),
            )
        negative.nodes.zip(zeroBased.nodes).forEach { (n, z) ->
            assertEquals(z.pixelBounds, n.pixelBounds, "negative columns must normalize to the same layout")
        }
    }

    @Test
    fun leadingColumnOffset_normalized_interiorGapPreserved() {
        val shifted =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 3), SankeyNode("B", column = 4)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                ),
            )
        val base =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                ),
            )
        shifted.nodes.zip(base.nodes).forEach { (s, b) -> assertEquals(b.pixelBounds, s.pixelBounds) }

        // Interior gaps stay: columns {0, 2} keep an empty middle slot (3-column spacing).
        val gapped =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 2)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                ),
            )
        val threeCol =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("M", column = 1), SankeyNode("B", column = 2)),
                    links = listOf(SankeyLink("A", "M", 5.0), SankeyLink("M", "B", 5.0)),
                ),
            )
        val gappedB = gapped.nodes.first { it.nodeId == "B" }
        val threeColB = threeCol.nodes.first { it.nodeId == "B" }
        assertTrue(abs(gappedB.pixelBounds.left - threeColB.pixelBounds.left) < 1e-3)
    }

    // --- Zero-value nodes ------------------------------------------------------------------------

    @Test
    fun zeroValueNode_inMixedColumn_getsVisiblePlaceholder() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("Z", column = 1)),
                links = listOf(SankeyLink("A", "B", 10.0)),
            )
        val p = plan(s)
        val z = p.nodes.first { it.nodeId == "Z" }
        assertTrue(z.pixelBounds.height > 0.0, "link-less node must keep a visible placeholder extent")
    }

    @Test
    fun allZeroColumn_extentIsCappedNotFullHeight() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0)),
                links = emptyList(),
            )
        val p = plan(s)
        val a = p.nodes.first()
        val plot = p.layout.plotBounds
        assertTrue(
            a.pixelBounds.height <= 0.05 * plot.height + 1.0,
            "all-zero column node must be a small stub, not full height (got ${a.pixelBounds.height})",
        )
    }

    @Test
    fun emptySpec_producesNoCommands_noCrash() {
        val p = plan(spec(nodes = emptyList(), links = emptyList()))
        assertTrue(p.nodes.isEmpty())
        assertTrue(p.links.isEmpty())
        assertTrue(p.layout.plotDrawCommands.isEmpty())
    }

    // --- Command emission: colors / gradients / selection ---------------------------------------

    @Test
    fun defaultLinks_emitSourceToTargetGradient_fromPaletteColors() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
            )
        val p = plan(s)
        val gradients =
            collectPaths(p.layout.plotDrawCommands).mapNotNull { it.style as? PathDrawStyle.LinearGradientFill }
        assertEquals(1, gradients.size, "default link must be a source→target gradient")
        val g = gradients.single()
        val palette = SankeyLayoutSpec.DefaultNodePalette
        assertEquals(palette[0].argb and 0x00FFFFFF, g.startColor.argb and 0x00FFFFFF, "gradient start = source node color")
        assertEquals(palette[1].argb and 0x00FFFFFF, g.endColor.argb and 0x00FFFFFF, "gradient end = target node color")
    }

    @Test
    fun explicitLinkColor_beatsColorMode_andStaticModeUsesDefaultLinkColor() {
        val explicit = ChartColor(0xAA112233)
        val withExplicit =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 5.0, color = explicit)),
                ),
            )
        val fills = collectPaths(withExplicit.layout.plotDrawCommands).mapNotNull { it.style as? PathDrawStyle.Fill }
        assertTrue(fills.any { it.color == explicit }, "explicit link color must win over the color mode")

        val staticLayout = SankeyLayoutSpec(linkColorMode = SankeyLinkColorMode.Static)
        val staticPlan =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                    layout = staticLayout,
                ),
            )
        val staticFills = collectPaths(staticPlan.layout.plotDrawCommands).mapNotNull { it.style as? PathDrawStyle.Fill }
        assertTrue(staticFills.any { it.color == staticLayout.defaultLinkColor })
    }

    @Test
    fun paletteAssignment_bySpecIndex_stableUnderNodeOrder() {
        val nodes = listOf(SankeyNode("A", column = 0), SankeyNode("Small", column = 1), SankeyNode("Big", column = 1))
        val links = listOf(SankeyLink("A", "Small", 1.0), SankeyLink("A", "Big", 9.0))
        // ValueDesc places Big (spec index 2) ABOVE Small (spec index 1) in the layout: if palette
        // assignment followed layout order, their colors would swap.
        val reordered = plan(spec(nodes, links, SankeyLayoutSpec(nodeOrder = SankeyNodeOrder.ValueDesc)))
        val palette = SankeyLayoutSpec.DefaultNodePalette
        val rects = collectRoundRects(reordered.layout.plotDrawCommands)

        fun fillOf(nodeId: String): ChartColor {
            val node = reordered.nodes.first { it.nodeId == nodeId }
            val rect = rects.first { it.bounds == node.pixelBounds }
            return (rect.style as ShapeDrawStyle.Fill).color
        }
        assertEquals(palette[1], fillOf("Small"), "Small keeps its spec-index color under reordering")
        assertEquals(palette[2], fillOf("Big"), "Big keeps its spec-index color under reordering")
    }

    @Test
    fun emptyPalette_fallsBackToDefaultNodeColor() {
        val layout = SankeyLayoutSpec(nodePalette = emptyList())
        val p =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 5.0)),
                    layout = layout,
                ),
            )
        collectRoundRects(p.layout.plotDrawCommands).forEach { r ->
            val fill = r.style as ShapeDrawStyle.Fill
            assertEquals(layout.defaultNodeColor, fill.color)
        }
    }

    @Test
    fun selectedLink_isExcludedFromDimGroup_andReEmittedWithRibbonStroke() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
            )
        val base = plan(s)
        val selection = linkSelection(s, base, 0)
        val p = ChartPlanner.planSankeyChart(input = input(), spec = s, selection = selection)

        val commands = p.layout.plotDrawCommands
        val dimGroup = commands.filterIsInstance<DrawCommand.Group>().first()
        assertEquals(0.55, dimGroup.alpha, 1e-9)
        val selectedSegments = p.links[0].pathSegments
        val dimmedPaths = collectPaths(dimGroup.children)
        assertTrue(
            dimmedPaths.none { it.segments == selectedSegments && it.style !is PathDrawStyle.Stroke },
            "selected ribbon fill must be excluded from the dim group",
        )
        val emphasized = commands.filterIsInstance<DrawCommand.Path>()
        assertTrue(
            emphasized.any { it.segments == selectedSegments && it.style !is PathDrawStyle.Stroke },
            "selected ribbon must be re-emitted at full alpha",
        )
        val stroke =
            emphasized.firstOrNull {
                it.segments == selectedSegments && (it.style as? PathDrawStyle.Stroke)?.color == s.selectionStyle.pointColor
            }
        assertNotNull(stroke, "selected ribbon must get an outline stroke along its actual curved geometry")
        // No straight-chord highlight: every emphasis path reuses the exact ribbon segments.
        assertTrue(emphasized.all { it.segments == selectedSegments })
    }

    // --- Geometry: anchors, bounds, labels -------------------------------------------------------

    @Test
    fun markerAnchor_isBezierMidpointOfCenterline() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                links = listOf(SankeyLink("A", "B", 3.0), SankeyLink("A", "C", 7.0)),
            )
        val p = plan(s)
        p.links.forEach { l ->
            val sTop = l.pixelPolygon[0]
            val tTop = l.pixelPolygon[16]
            val tBot = l.pixelPolygon[17]
            val sBot = l.pixelPolygon.last()
            val sMid = OffsetD((sTop.x + sBot.x) / 2.0, (sTop.y + sBot.y) / 2.0)
            val tMid = OffsetD((tTop.x + tBot.x) / 2.0, (tTop.y + tBot.y) / 2.0)
            val curvature = 0.55
            val c1x = sMid.x + (tMid.x - sMid.x) * curvature
            val c2x = tMid.x - (tMid.x - sMid.x) * curvature
            val expectedX = (sMid.x + 3.0 * c1x + 3.0 * c2x + tMid.x) / 8.0
            val expectedY = (sMid.y + 3.0 * sMid.y + 3.0 * tMid.y + tMid.y) / 8.0
            assertTrue(abs(l.markerAnchor.x - expectedX) < 1e-3, "anchor x")
            assertTrue(abs(l.markerAnchor.y - expectedY) < 1e-3, "anchor y")
        }
    }

    @Test
    fun pixelBounds_containDenselySampledCurvePoints() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                links = listOf(SankeyLink("A", "B", 3.0), SankeyLink("A", "C", 7.0)),
            )
        val p = plan(s)
        p.links.forEach { l ->
            l.pixelPolygon.forEach { pt ->
                assertTrue(pt.x >= l.pixelBounds.left - eps && pt.x <= l.pixelBounds.right + eps)
                assertTrue(pt.y >= l.pixelBounds.top - eps && pt.y <= l.pixelBounds.bottom + eps)
            }
        }
    }

    @Test
    fun linkLabel_skippedWhenTallerThanRibbon_emittedWhenItFits() {
        val label = "A → B: 5"

        fun measurements(labelHeight: Double) =
            ChartTextMeasurementInput(
                tickLabelMetrics = mapOf(label to TextLayoutMetrics.uniformBox(SizeD(40.0, labelHeight))),
            )

        val thinSpec =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                // The A→B ribbon is 5/100 of the flow: a few pixels thick.
                links = listOf(SankeyLink("A", "B", 5.0), SankeyLink("A", "C", 95.0)),
                layout = SankeyLayoutSpec(nodeLabels = SankeyNodeLabelsSpec.Disabled),
            )
        val thin = plan(thinSpec, input(measurements = measurements(labelHeight = 30.0)))
        assertTrue(
            collectTexts(thin.layout.plotDrawCommands).none { it.text == label },
            "a 30px-tall label must not be painted across a thinner ribbon",
        )

        val thickSpec =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
                layout = SankeyLayoutSpec(nodeLabels = SankeyNodeLabelsSpec.Disabled),
            )
        val thick = plan(thickSpec, input(measurements = measurements(labelHeight = 10.0)))
        assertTrue(
            collectTexts(thick.layout.plotDrawCommands).any { it.text == label },
            "label must be emitted when it fits the ribbon",
        )
    }

    @Test
    fun adjacentNodeLabels_pointInward_perColumnHalf() {
        val measurements =
            ChartTextMeasurementInput(
                tickLabelMetrics =
                    mapOf(
                        "A" to TextLayoutMetrics.uniformBox(SizeD(20.0, 10.0)),
                        "B" to TextLayoutMetrics.uniformBox(SizeD(20.0, 10.0)),
                    ),
            )
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
                layout = SankeyLayoutSpec(linkLabels = com.mena97villalobos.composeplotlib.core.spec.SankeyLinkLabelsSpec.Disabled),
            )
        val p = plan(s, input(measurements = measurements))
        val texts = collectTexts(p.layout.plotDrawCommands)
        val a = p.nodes.first { it.nodeId == "A" }
        val b = p.nodes.first { it.nodeId == "B" }
        val aLabel = texts.first { it.text == "A" }
        val bLabel = texts.first { it.text == "B" }
        assertTrue(aLabel.position.x >= a.pixelBounds.right, "first-half label sits after its node")
        assertTrue(bLabel.position.x + 20.0 <= b.pixelBounds.left + eps, "second-half label sits before its node")
        assertEquals(0.0, aLabel.rotationDegrees, "adjacent labels are horizontal")
    }

    @Test
    fun insideNodeLabels_ellipsizeInsteadOfDrop() {
        val label = "ABCDEFGHIJ"
        val measurements =
            ChartTextMeasurementInput(
                tickLabelMetrics = mapOf(label to TextLayoutMetrics.uniformBox(SizeD(100.0, 10.0))),
            )
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0, label = label), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
                layout =
                    SankeyLayoutSpec(
                        nodeLabels =
                            SankeyNodeLabelsSpec(
                                placement = SankeyNodeLabelPlacement.Inside,
                                orientation = SankeyNodeLabelOrientation.Horizontal,
                            ),
                        linkLabels = com.mena97villalobos.composeplotlib.core.spec.SankeyLinkLabelsSpec.Disabled,
                    ),
            )
        val p = plan(s, input(measurements = measurements))
        val texts = collectTexts(p.layout.plotDrawCommands)
        val ellipsized = texts.firstOrNull { it.text.endsWith("…") }
        assertNotNull(ellipsized, "too-wide inside label must be ellipsized, not silently dropped")
        assertTrue(ellipsized.text.length < label.length)
    }

    @Test
    fun sankeyChart_hasNoAxisTicks() {
        val p =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 1.0)),
                ),
            )
        assertTrue(p.layout.xTicks.isEmpty())
        assertTrue(p.layout.yTicks.isEmpty())
        assertTrue(p.layout.overlayDrawCommands.isEmpty())
    }

    @Test
    fun selectionWithStaleIndex_dimsWithoutCrash() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
            )
        val base = plan(s)
        val valid = linkSelection(s, base, 0)
        assertNotNull(valid)
        val stale = valid.copy(target = valid.target.copy(pointIndex = 99))
        val p = ChartPlanner.planSankeyChart(input = input(), spec = s, selection = stale)
        assertTrue(
            p.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Group>()
                .any { it.alpha == 0.55 },
        )
    }

    @Test
    fun linkWithZeroOrNegativeValue_isNotPlanned() {
        val p =
            plan(
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                    links = listOf(SankeyLink("A", "B", 0.0), SankeyLink("A", "B", -1.0), SankeyLink("A", "B", 2.0)),
                ),
            )
        assertEquals(1, p.links.size)
        assertEquals(2.0, p.links[0].value)
    }

    @Test
    fun noLinks_hitResolverReturnsNullOffNode() {
        val s = spec(nodes = listOf(SankeyNode("A", column = 0)), links = emptyList())
        val p = plan(s)
        // Production path: resolving a tap outside the only node on a link-less plan.
        assertNull(SankeyHitResolver.resolve(OffsetD(1.0, 1.0), s, p))
    }

    @Test
    fun selectedNode_isExcludedFromDimGroup_andReEmittedWithOutline() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
                links = listOf(SankeyLink("A", "B", 5.0)),
            )
        val base = plan(s)
        val a = base.nodes.first { it.nodeId == "A" }
        val selection = SankeyHitResolver.resolve(a.pixelBounds.center, s, base)
        assertNotNull(selection)
        assertEquals(SankeyHitResolver.LayerNode, selection.target.layerId)

        val p = ChartPlanner.planSankeyChart(input = input(), spec = s, selection = selection)
        val commands = p.layout.plotDrawCommands
        val dimGroup = commands.filterIsInstance<DrawCommand.Group>().first()
        assertEquals(0.55, dimGroup.alpha, 1e-9)
        val selectedBounds = p.nodes[selection.target.pointIndex].pixelBounds
        val dimmedFills =
            collectRoundRects(dimGroup.children).filter { it.style is ShapeDrawStyle.Fill }
        assertTrue(
            dimmedFills.none { it.bounds == selectedBounds },
            "selected node fill must be excluded from the dim group",
        )
        val emphasized = commands.filterIsInstance<DrawCommand.RoundRect>()
        assertTrue(
            emphasized.any { it.bounds == selectedBounds && it.style is ShapeDrawStyle.Fill },
            "selected node must be re-emitted at full alpha",
        )
        assertTrue(
            emphasized.any {
                it.bounds == selectedBounds && (it.style as? ShapeDrawStyle.Stroke)?.color == s.selectionStyle.pointColor
            },
            "selected node must get an outline stroke in the selection color",
        )
    }

    @Test
    fun zoomedViewport_scalesGeometry_inBothOrientations() {
        val nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1))
        val links = listOf(SankeyLink("A", "B", 5.0))
        for (orientation in SankeyOrientation.entries) {
            val layout = SankeyLayoutSpec(orientation = orientation)
            val identity = plan(spec(nodes, links, layout))
            val zoomedInput =
                ChartLayoutInput(
                    canvasSize = SizeD(400.0, 300.0),
                    viewport =
                        ChartViewport(
                            xRange = ClosedRangeD(0.25, 0.75),
                            yRange = ClosedRangeD(0.25, 0.75),
                        ),
                )
            val zoomed = ChartPlanner.planSankeyChart(zoomedInput, spec(nodes, links, layout), selection = null)
            identity.nodes.zip(zoomed.nodes).forEach { (i, z) ->
                // Halving the visible span doubles every pixel extent.
                assertTrue(
                    abs(z.pixelBounds.width - 2.0 * i.pixelBounds.width) < 1e-3,
                    "$orientation: zoomed node width must double (got ${z.pixelBounds.width} vs ${i.pixelBounds.width})",
                )
                assertTrue(
                    abs(z.pixelBounds.height - 2.0 * i.pixelBounds.height) < 1e-3,
                    "$orientation: zoomed node height must double",
                )
            }
        }
    }

    @Test
    fun verticalOrientation_linkLabelGate_usesCrossAxisWidth() {
        val label = "A → B: 5"

        fun verticalPlan(labelWidth: Double): SankeyChartPlan {
            val measurements =
                ChartTextMeasurementInput(
                    tickLabelMetrics = mapOf(label to TextLayoutMetrics.uniformBox(SizeD(labelWidth, 10.0))),
                )
            val s =
                spec(
                    nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                    // The A→B ribbon is 5% of the flow: a thin band along the cross (x) axis.
                    links = listOf(SankeyLink("A", "B", 5.0), SankeyLink("A", "C", 95.0)),
                    layout =
                        SankeyLayoutSpec(
                            orientation = SankeyOrientation.Vertical,
                            nodeLabels = SankeyNodeLabelsSpec.Disabled,
                        ),
                )
            return plan(s, input(measurements = measurements))
        }

        // A wide label cannot fit the thin ribbon's cross extent in Vertical orientation…
        assertTrue(
            collectTexts(verticalPlan(labelWidth = 60.0).layout.plotDrawCommands).none { it.text.startsWith("A → B") },
            "wide label must be skipped on a thin vertical ribbon",
        )
        // …while a narrow one does fit.
        assertTrue(
            collectTexts(verticalPlan(labelWidth = 8.0).layout.plotDrawCommands).any { it.text.startsWith("A → B") },
            "narrow label must be emitted on the vertical ribbon",
        )
    }

    @Test
    fun subPixelRibbon_isWidenedToStayVisible() {
        val s =
            spec(
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("Tiny", column = 1), SankeyNode("Big", column = 1)),
                links = listOf(SankeyLink("A", "Tiny", 0.0001), SankeyLink("A", "Big", 100.0)),
            )
        val p = plan(s)
        val tiny = p.links.first { it.targetNodeId == "Tiny" }
        val sourceBandExtent = abs(tiny.pixelPolygon.last().y - tiny.pixelPolygon.first().y)
        assertTrue(
            sourceBandExtent >= 0.99,
            "sub-pixel ribbon band must be widened to ~1px (got $sourceBandExtent px)",
        )
    }
}
