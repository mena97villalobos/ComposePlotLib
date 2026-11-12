package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.spec.layoutSize
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TreemapHierarchyPlannerTest {
    private val measurer = TreemapLabelMeasurer.estimate(charWidthPx = 7.0, lineHeightPx = 16.0)

    private fun input(
        width: Double = 600.0,
        height: Double = 400.0,
    ) = ChartLayoutInput(
        canvasSize = SizeD(width, height),
        viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0)),
    )

    /** No gaps, padding, header or content padding: every area is exactly proportional. */
    private fun exactSpec(
        nodes: List<TreemapHierarchyNode>,
        maxDepth: Int = 2,
        minTileSidePx: Double = 0.0,
    ) = TreemapHierarchySpec(
        nodes = nodes,
        maxDepth = maxDepth,
        tileGapPx = 0.0,
        nodePaddingPx = 0.0,
        headerHeightPx = 0.0,
        showLabels = false,
        minTileSidePx = minTileSidePx,
        contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0),
    )

    private fun plan(
        spec: TreemapHierarchySpec,
        root: String? = null,
        width: Double = 600.0,
        height: Double = 400.0,
    ) = ChartPlanner.planTreemapHierarchy(input(width, height), spec, root, measurer)

    private fun RectD.area() = width * height

    private fun RectD.containsRect(other: RectD) = other.left >= left - 1e-6 && other.top >= top - 1e-6 && other.right <= right + 1e-6 && other.bottom <= bottom + 1e-6

    private fun leaf(
        id: String,
        value: Double,
    ) = TreemapHierarchyNode(id, id, value)

    private val twoLevel =
        listOf(
            TreemapHierarchyNode("a", "A", children = listOf(leaf("a1", 6.0), leaf("a2", 3.0), leaf("a3", 1.0))),
            TreemapHierarchyNode("b", "B", children = listOf(leaf("b1", 4.0), leaf("b2", 1.0))),
            leaf("c", 5.0),
        )

    // --- L-002: hierarchy and nested layout ---------------------------------------------------------------------

    @Test
    fun twoLevelTree_childrenInsideParents_areasProportionalWithinEachParent() {
        val plan = plan(exactSpec(twoLevel))
        val byId = plan.tiles.associateBy { it.nodeId }
        assertEquals(setOf("a", "b", "c", "a1", "a2", "a3", "b1", "b2"), byId.keys)

        val totalArea = 600.0 * 400.0
        assertEquals(10.0 / 20.0 * totalArea, byId.getValue("a").bounds.area(), 1e-6)
        assertEquals(5.0 / 20.0 * totalArea, byId.getValue("b").bounds.area(), 1e-6)
        assertEquals(5.0 / 20.0 * totalArea, byId.getValue("c").bounds.area(), 1e-6)

        for (parent in listOf("a", "b")) {
            val p = byId.getValue(parent)
            assertTrue(p.hasDrawnChildren)
            val kids = plan.tiles.filter { it.path.dropLast(1) == p.path }
            kids.forEach { kid ->
                assertTrue(p.bounds.containsRect(kid.bounds), "$parent must contain ${kid.nodeId}")
                assertEquals(2, kid.depth)
                assertEquals(kid.value / p.value * p.bounds.area(), kid.bounds.area(), 1e-6)
            }
        }
        assertFalse(byId.getValue("c").hasDrawnChildren)
        assertEquals(listOf("a", "a1"), byId.getValue("a1").path)
    }

    @Test
    fun tilesArePreOrder_parentsBeforeChildren() {
        val plan = plan(exactSpec(twoLevel))
        plan.tiles.forEachIndexed { index, tile ->
            if (tile.path.size > 1) {
                val parentIndex = plan.tiles.indexOfFirst { it.nodeId == tile.path[tile.path.size - 2] }
                assertTrue(parentIndex in 0 until index)
            }
        }
    }

    @Test
    fun flatList_isOneLevel() {
        val plan = plan(exactSpec(listOf(leaf("x", 3.0), leaf("y", 1.0))))
        assertEquals(listOf(1, 1), plan.tiles.map { it.depth })
        assertEquals(
            0.75 * 600.0 * 400.0,
            plan.tiles
                .first { it.nodeId == "x" }
                .bounds
                .area(),
            1e-6,
        )
    }

    @Test
    fun zeroNegativeAndNonFiniteValues_areOmitted() {
        val nodes =
            listOf(
                leaf("zero", 0.0),
                leaf("neg", -5.0),
                leaf("nan", Double.NaN),
                leaf("inf", Double.POSITIVE_INFINITY),
                leaf("ok", 2.0),
                TreemapHierarchyNode("emptyParent", "E", children = listOf(leaf("z", 0.0))),
            )
        val plan = plan(exactSpec(nodes))
        assertEquals(listOf("ok"), plan.tiles.map { it.nodeId })
        assertEquals(
            600.0 * 400.0,
            plan.tiles
                .single()
                .bounds
                .area(),
            1e-6,
        )
    }

    @Test
    fun allZero_orEmpty_producesNoTiles() {
        assertTrue(plan(exactSpec(emptyList())).tiles.isEmpty())
        assertTrue(plan(exactSpec(listOf(leaf("a", 0.0)))).tiles.isEmpty())
        assertTrue(plan(exactSpec(twoLevel), width = 0.0, height = 0.0).tiles.isEmpty())
    }

    @Test
    fun singleChild_fillsItsParentContent() {
        val nodes = listOf(TreemapHierarchyNode("p", "P", children = listOf(leaf("only", 3.0))), leaf("q", 3.0))
        val plan = plan(exactSpec(nodes))
        val p = plan.tiles.first { it.nodeId == "p" }
        val only = plan.tiles.first { it.nodeId == "only" }
        assertEquals(p.bounds.area(), only.bounds.area(), 1e-6)
    }

    @Test
    fun deepTree_isCutAtTheDepthLimit_butDeeperValuesStillCount() {
        val deep =
            TreemapHierarchyNode(
                "l1",
                "L1",
                children =
                    listOf(
                        TreemapHierarchyNode(
                            "l2",
                            "L2",
                            children = listOf(TreemapHierarchyNode("l3", "L3", children = listOf(leaf("l4", 8.0)))),
                        ),
                    ),
            )
        val nodes = listOf(deep, leaf("other", 8.0))
        val depth2 = plan(exactSpec(nodes, maxDepth = 2))
        assertEquals(setOf("l1", "l2", "other"), depth2.tiles.map { it.nodeId }.toSet())
        val l2 = depth2.tiles.first { it.nodeId == "l2" }
        assertEquals(8.0, l2.value)
        assertTrue(l2.hasChildren)
        assertFalse(l2.hasDrawnChildren)
        assertEquals(
            0.5 * 600.0 * 400.0,
            depth2.tiles
                .first { it.nodeId == "l1" }
                .bounds
                .area(),
            1e-6,
        )

        val depth1 = plan(exactSpec(nodes, maxDepth = 1))
        assertEquals(setOf("l1", "other"), depth1.tiles.map { it.nodeId }.toSet())

        val depth4 = plan(exactSpec(nodes, maxDepth = 4))
        assertEquals(4, depth4.tiles.maxOf { it.depth })
    }

    @Test
    fun verySkewedDistribution_staysProportionalAndInsideBounds() {
        val nodes = listOf(leaf("huge", 1e9)) + (1..50).map { leaf("tiny$it", 1.0) }
        val plan = plan(exactSpec(nodes))
        val total = 1e9 + 50.0
        val area = 600.0 * 400.0
        assertEquals(51, plan.tiles.size)
        plan.tiles.forEach { tile ->
            assertTrue(RectD(0.0, 0.0, 600.0, 400.0).containsRect(tile.bounds))
            assertTrue(tile.bounds.area() > 0.0)
            assertEquals(tile.value / total * area, tile.bounds.area(), area * 1e-9)
        }
    }

    @Test
    fun explicitParentValue_leavesAnUndrawnRemainder() {
        val nodes = listOf(TreemapHierarchyNode("dir", "Dir", value = 10.0, children = listOf(leaf("f1", 3.0), leaf("f2", 1.0))))
        val plan = plan(exactSpec(nodes))
        val dir = plan.tiles.first { it.nodeId == "dir" }
        assertEquals(10.0, dir.value)
        val childArea = plan.tiles.filter { it.depth == 2 }.sumOf { it.bounds.area() }
        assertEquals(0.4 * dir.bounds.area(), childArea, 1e-6)
        // A value below the children's sum is ignored.
        assertEquals(4.0, TreemapHierarchyNode("x", "X", value = 1.0, children = listOf(leaf("f1", 3.0), leaf("f2", 1.0))).layoutSize())
    }

    @Test
    fun drilledRoot_fillsThePlotWithItsChildren_andFallsBackWhenUnknown() {
        val spec = exactSpec(twoLevel)
        val drilled = plan(spec, root = "a")
        assertEquals("a", drilled.rootNodeId)
        assertEquals(listOf("a"), drilled.rootPath)
        assertEquals(setOf("a1", "a2", "a3"), drilled.tiles.map { it.nodeId }.toSet())
        assertEquals(600.0 * 400.0, drilled.tiles.sumOf { it.bounds.area() }, 1e-6)
        assertEquals(listOf("a", "a1"), drilled.tiles.first { it.nodeId == "a1" }.path)
        assertEquals(1, drilled.tiles.first().depth)

        val unknown = plan(spec, root = "nope")
        assertNull(unknown.rootNodeId)
        assertEquals(plan(spec).tiles, unknown.tiles)

        val leafRoot = plan(spec, root = "c")
        assertEquals(listOf("c"), leafRoot.tiles.map { it.nodeId })
    }

    @Test
    fun drillingThroughThreeLevels_alwaysFillsThePlot() {
        val tree =
            listOf(
                TreemapHierarchyNode(
                    "r",
                    "R",
                    children =
                        listOf(
                            TreemapHierarchyNode("m", "M", children = listOf(leaf("m1", 2.0), leaf("m2", 1.0))),
                            leaf("n", 1.0),
                        ),
                ),
                leaf("s", 1.0),
            )
        val spec = exactSpec(tree)
        for (root in listOf(null, "r", "m")) {
            val plan = plan(spec, root)
            val topLevel = plan.tiles.filter { it.depth == 1 }
            assertEquals(600.0 * 400.0, topLevel.sumOf { it.bounds.area() }, 1e-6, "root=$root")
        }
    }

    @Test
    fun colors_comeFromThePaletteByTopLevelIndex_andLightenWithDepth_stableAcrossDrill() {
        val spec = exactSpec(twoLevel).copy(palette = ChartPalettes.Category10)
        val top = plan(spec)
        assertEquals(ChartPalettes.Category10[0], top.tiles.first { it.nodeId == "a" }.color)
        assertEquals(ChartPalettes.Category10[2], top.tiles.first { it.nodeId == "c" }.color)
        val a1Top = top.tiles.first { it.nodeId == "a1" }.color
        assertTrue(a1Top != ChartPalettes.Category10[0])
        assertEquals(a1Top, plan(spec, root = "a").tiles.first { it.nodeId == "a1" }.color)
        val explicit = ChartColor(0xFF123456)
        val custom = exactSpec(listOf(TreemapHierarchyNode("k", "K", 1.0, color = explicit)))
        assertEquals(explicit, plan(custom).tiles.single().color)
    }

    @Test
    fun layoutIsDeterministic() {
        val spec = TreemapHierarchySpec(nodes = twoLevel, minTileSidePx = 30.0, valueFormatter = { "${it.layoutSize()}" })
        assertEquals(plan(spec), plan(spec))
    }

    @Test
    fun internalSquarify_matchesThePublicFlatLayoutExactly() {
        var seed = 7L

        fun next(): Double {
            seed = (seed * 6364136223846793005L + 1442695040888963407L)
            return ((seed ushr 11) % 10_000L).toDouble() + 1.0
        }
        repeat(20) { round ->
            val values = List(5 + round * 7) { next() }
            val bounds = RectD(3.0, 5.0, 503.0, 305.0 + round)
            assertEquals(squarifiedTreemap(values, bounds), squarify(values.toDoubleArray(), bounds).toList())
        }
    }

    // --- L-005: minimum tile size and aggregate tile ------------------------------------------------------------

    private val longTail =
        listOf(
            TreemapHierarchyNode(
                "folder",
                "Folder",
                children = listOf(leaf("big", 500.0), leaf("mid", 200.0)) + (1..40).map { leaf("small$it", 1.0 + it % 3) },
            ),
            leaf("other", 400.0),
        ) + (1..30).map { leaf("crumb$it", 2.0) }

    @Test
    fun minTileSide_noDrawnTileIsSmaller() {
        val minSide = 48.0
        for ((w, h) in listOf(600.0 to 400.0, 360.0 to 640.0, 1000.0 to 300.0)) {
            val plan = plan(TreemapHierarchySpec(nodes = longTail, minTileSidePx = minSide), width = w, height = h)
            assertTrue(plan.tiles.isNotEmpty())
            plan.tiles.forEach { tile ->
                assertTrue(
                    tile.bounds.width >= minSide - 1e-9 && tile.bounds.height >= minSide - 1e-9,
                    "${tile.nodeId} is ${tile.bounds.width}x${tile.bounds.height} at ${w}x$h",
                )
            }
        }
    }

    @Test
    fun minTileSide_renderedValuesSumToEachParent() {
        val plan = plan(TreemapHierarchySpec(nodes = longTail, minTileSidePx = 48.0))
        val topTotal = longTail.sumOf { it.layoutSize() }
        assertEquals(topTotal, plan.tiles.filter { it.depth == 1 }.sumOf { it.value }, 1e-9)
        plan.tiles.filter { it.hasDrawnChildren }.forEach { parent ->
            val kids = plan.tiles.filter { it.path.dropLast(1) == parent.path }
            assertEquals(parent.value, kids.sumOf { it.value }, 1e-9)
        }
    }

    @Test
    fun aggregate_isIdentifiable_andListsItsMembers() {
        val spec = TreemapHierarchySpec(nodes = longTail, minTileSidePx = 48.0, aggregateLabel = { "$it more" })
        val plan = plan(spec)
        val topAggregate = plan.tiles.single { it.isAggregate && it.depth == 1 }
        assertEquals("treemap" + TreemapHierarchyPlan.AGGREGATE_ID_SUFFIX, topAggregate.nodeId)
        assertEquals(listOf(topAggregate.nodeId), topAggregate.path)
        assertEquals((1..30).map { "crumb$it" }.toSet(), topAggregate.memberIds.toSet())
        assertEquals("${topAggregate.memberIds.size} more", topAggregate.label)
        assertEquals(topAggregate.memberIds.size * 2.0, topAggregate.value, 1e-9)
        assertFalse(topAggregate.hasChildren)

        val nested = plan.tiles.single { it.isAggregate && it.depth == 2 }
        assertEquals("folder" + TreemapHierarchyPlan.AGGREGATE_ID_SUFFIX, nested.nodeId)
        assertEquals(listOf("folder", nested.nodeId), nested.path)
        assertTrue(nested.memberIds.all { it.startsWith("small") })
        // Members are never also drawn on their own.
        val drawnIds = plan.tiles.map { it.nodeId }.toSet()
        assertTrue(nested.memberIds.none { it in drawnIds })
        assertTrue(topAggregate.memberIds.none { it in drawnIds })
    }

    @Test
    fun aggregate_valueFormatterReceivesASyntheticNodeOfTheMembers() {
        var seen: TreemapHierarchyNode? = null
        val spec =
            TreemapHierarchySpec(
                nodes = longTail,
                minTileSidePx = 48.0,
                valueFormatter = { node ->
                    if (node.id.endsWith(TreemapHierarchyPlan.AGGREGATE_ID_SUFFIX) && node.id.startsWith("treemap")) seen = node
                    node.layoutSize().toString()
                },
            )
        val aggregate = plan(spec).tiles.single { it.isAggregate && it.depth == 1 }
        val node = seen!!
        assertEquals(aggregate.memberIds, node.children.map { it.id })
        assertEquals(aggregate.value, node.layoutSize(), 1e-9)
        assertEquals(aggregate.value.toString(), aggregate.valueText)
    }

    @Test
    fun aTailJustBelowTheMinimum_growsInsteadOfSwallowingAMediumSibling() {
        // The 30 crumbs together are just under the area a 48px tile needs; "other" is 400 units.
        val nodes =
            listOf(
                TreemapHierarchyNode("folder", "Folder", children = listOf(leaf("big", 500.0), leaf("mid", 200.0))),
                leaf("other", 400.0),
            ) + (1..30).map { leaf("crumb$it", 0.5) }
        val plan = plan(TreemapHierarchySpec(nodes = nodes, minTileSidePx = 48.0))
        val aggregate = plan.tiles.single { it.isAggregate }
        assertEquals((1..30).map { "crumb$it" }.toSet(), aggregate.memberIds.toSet())
        assertEquals(15.0, aggregate.value, 1e-9)
        assertTrue(plan.tiles.any { it.nodeId == "other" })
        plan.tiles.forEach { assertTrue(it.bounds.width >= 48.0 && it.bounds.height >= 48.0, it.nodeId) }
        assertEquals(nodes.sumOf { it.layoutSize() }, plan.tiles.filter { it.depth == 1 }.sumOf { it.value }, 1e-9)
    }

    @Test
    fun aParentWhoseOnlyTileWouldAggregateAllItsChildren_drawsNoChildren() {
        val nodes =
            listOf(
                TreemapHierarchyNode("p", "P", children = (1..6).map { leaf("k$it", 1.0) }),
                leaf("q", 1.0),
            )
        val plan = plan(TreemapHierarchySpec(nodes = nodes, minTileSidePx = 85.0), width = 400.0, height = 200.0)
        val p = plan.tiles.single { it.nodeId == "p" }
        assertFalse(p.hasDrawnChildren)
        assertTrue(plan.tiles.none { it.depth == 2 })
    }

    @Test
    fun aSingleSmallLeftover_isNotWrappedInAnAggregate() {
        val nodes = listOf(leaf("a", 100.0), leaf("b", 100.0), leaf("c", 100.0), leaf("d", 2.0))
        val plan = plan(TreemapHierarchySpec(nodes = nodes, minTileSidePx = 40.0, contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0)), width = 300.0, height = 120.0)
        assertTrue(plan.tiles.none { it.isAggregate && it.memberIds.size < 2 })
        plan.tiles.forEach { assertTrue(it.bounds.width >= 40.0 && it.bounds.height >= 40.0) }
    }

    @Test
    fun whenNothingFits_theParentKeepsNoChildren() {
        val nodes = listOf(TreemapHierarchyNode("p", "P", children = (1..5).map { leaf("k$it", 1.0) }))
        // The 100x100 canvas leaves an 82x82 tile for p; below its header the children get about 74x59, so not even
        // one aggregate of all five reaches 70px.
        val plan = plan(TreemapHierarchySpec(nodes = nodes, minTileSidePx = 70.0), width = 100.0, height = 100.0)
        assertEquals(listOf("p"), plan.tiles.map { it.nodeId })
        assertFalse(plan.tiles.single().hasDrawnChildren)
    }

    @Test
    fun minTileSide_layoutIsDeterministic() {
        val spec = TreemapHierarchySpec(nodes = longTail, minTileSidePx = 48.0)
        assertEquals(plan(spec, width = 412.0, height = 300.0), plan(spec, width = 412.0, height = 300.0))
    }

    // --- L-006: labels -------------------------------------------------------------------------------------------

    private fun labelTexts(plan: TreemapHierarchyPlan): List<DrawCommand.Text> = plan.layout.overlayDrawCommands.flatMap { (it as DrawCommand.ClipRect).children.map { c -> c as DrawCommand.Text } }

    @Test
    fun labels_drawBothLines_nameOnly_orNothing_dependingOnTileSize() {
        val spec =
            TreemapHierarchySpec(
                nodes = listOf(leaf("n", 1.0)),
                valueFormatter = { "1 GB" },
                tileGapPx = 0.0,
                contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0),
            )
        // 2 * 4px padding + 16px per line.
        assertEquals(listOf("n", "1 GB"), labelTexts(plan(spec, width = 100.0, height = 40.0)).map { it.text })
        assertEquals(listOf("n"), labelTexts(plan(spec, width = 100.0, height = 30.0)).map { it.text })
        assertTrue(labelTexts(plan(spec, width = 100.0, height = 20.0)).isEmpty())
        // The value line also needs its own width.
        assertEquals(listOf("n"), labelTexts(plan(spec, width = 30.0, height = 40.0)).map { it.text })
    }

    @Test
    fun labels_longNamesAreEllipsized_toFitTheTileWidth() {
        val spec =
            TreemapHierarchySpec(
                nodes = listOf(TreemapHierarchyNode("n", "Downloads and other things", 1.0)),
                tileGapPx = 0.0,
                contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0),
            )
        val text = labelTexts(plan(spec, width = 80.0, height = 40.0)).single().text
        assertTrue(text.endsWith("…"), text)
        assertTrue(measurer.measure(text, TreemapLabelLine.Name).width <= 80.0 - 8.0)
        assertTrue(labelTexts(plan(spec, width = 10.0, height = 40.0)).isEmpty())
    }

    @Test
    fun labels_neverExtendOutsideTheirTile_atSeveralSizesAndTextScales() {
        val spec = TreemapHierarchySpec(nodes = longTail + twoLevel, valueFormatter = { "${it.layoutSize()} B" }, minTileSidePx = 24.0)
        for ((w, h) in listOf(600.0 to 400.0, 320.0 to 240.0, 200.0 to 900.0)) {
            for (scale in listOf(1.0, 1.4, 2.0)) {
                val scaled = TreemapLabelMeasurer.estimate(charWidthPx = 7.0 * scale, lineHeightPx = 16.0 * scale)
                val plan = ChartPlanner.planTreemapHierarchy(input(w, h), spec, null, scaled)
                plan.layout.overlayDrawCommands.forEach { command ->
                    val clip = command as DrawCommand.ClipRect
                    assertTrue(plan.tiles.any { it.bounds.containsRect(clip.bounds) }, "clip must lie in a tile")
                    clip.children.forEach { child ->
                        val text = child as DrawCommand.Text
                        val line =
                            when {
                                text.isPieSliceLabel -> TreemapLabelLine.Header
                                text.isYAxisTick -> TreemapLabelLine.Value
                                else -> TreemapLabelLine.Name
                            }
                        val size = scaled.measure(text.text, line)
                        val box = RectD(text.position.x, text.position.y, text.position.x + size.width, text.position.y + size.height)
                        assertTrue(clip.bounds.containsRect(box), "'${text.text}' at ${w}x$h scale $scale")
                    }
                }
            }
        }
    }

    @Test
    fun labels_formatterReceivesTheNode() {
        val seen = mutableListOf<String>()
        val spec =
            TreemapHierarchySpec(nodes = twoLevel, valueFormatter = { node ->
                seen += node.id
                "v:${node.id}"
            })
        val plan = plan(spec)
        assertTrue(seen.containsAll(listOf("a", "a1", "b", "c")))
        assertEquals("v:a1", plan.tiles.first { it.nodeId == "a1" }.valueText)
    }

    @Test
    fun labels_parentsUseTheHeaderBand() {
        val spec = TreemapHierarchySpec(nodes = twoLevel, headerHeightPx = 20.0)
        val plan = plan(spec)
        val a = plan.tiles.first { it.nodeId == "a" }
        val header =
            plan.layout.overlayDrawCommands
                .map { it as DrawCommand.ClipRect }
                .first { clip -> clip.children.any { (it as DrawCommand.Text).text == "A" } }
        assertTrue((header.children.first() as DrawCommand.Text).isPieSliceLabel)
        assertEquals(a.bounds.top, header.bounds.top)
        assertEquals(a.bounds.top + 20.0, header.bounds.bottom, 1e-9)
        plan.tiles.filter { it.depth == 2 && it.path.first() == "a" }.forEach { assertTrue(it.bounds.top >= header.bounds.bottom) }
    }

    @Test
    fun labels_headerBandGrowsToFitLargeText() {
        val big = TreemapLabelMeasurer.estimate(charWidthPx = 10.0, lineHeightPx = 30.0)
        val plan = ChartPlanner.planTreemapHierarchy(input(), TreemapHierarchySpec(nodes = twoLevel, headerHeightPx = 18.0), null, big)
        val a = plan.tiles.first { it.nodeId == "a" }
        val firstChildTop = plan.tiles.filter { it.path.dropLast(1) == a.path }.minOf { it.bounds.top }
        assertTrue(firstChildTop - a.bounds.top >= 32.0)
    }

    @Test
    fun labels_colorContrastsWithEachTile_orUsesTheOverride() {
        val dark = ChartColor(0xFF1B1B1B)
        val light = ChartColor(0xFFF5F5F5)
        val nodes = listOf(TreemapHierarchyNode("d", "Dark", 1.0, color = dark), TreemapHierarchyNode("l", "Light", 1.0, color = light))
        val auto = labelTexts(plan(TreemapHierarchySpec(nodes = nodes))).associate { it.text to it.color }
        assertEquals(ChartColor.White, auto["Dark"])
        assertEquals(ChartColor.Black, auto["Light"])
        val forced = labelTexts(plan(TreemapHierarchySpec(nodes = nodes, labelColor = ChartColor.Red))).map { it.color }.toSet()
        assertEquals(setOf(ChartColor.Red), forced)
    }

    @Test
    fun labels_hiddenWhenShowLabelsIsFalse() {
        assertTrue(plan(TreemapHierarchySpec(nodes = twoLevel, showLabels = false)).layout.overlayDrawCommands.isEmpty())
    }

    @Test
    fun ellipsized_neverSplitsASurrogatePair() {
        val text = "ab😀cd" // a b 😀 c d
        assertEquals("ab…", TreemapHierarchyPlanner.ellipsized(text, 3))
        assertEquals("ab😀…", TreemapHierarchyPlanner.ellipsized(text, 4))
        assertEquals("a…", TreemapHierarchyPlanner.ellipsized("a  b", 3))
    }
}
