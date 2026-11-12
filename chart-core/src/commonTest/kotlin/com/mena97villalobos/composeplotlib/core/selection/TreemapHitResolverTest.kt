package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.DrawCommand
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.layout.TreemapLabelMeasurer
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TreemapHitResolverTest {
    private fun leaf(
        id: String,
        value: Double,
    ) = TreemapHierarchyNode(id, id.uppercase(), value)

    private val nodes =
        listOf(
            TreemapHierarchyNode("a", "A", children = listOf(leaf("a1", 3.0), leaf("a2", 1.0))),
            leaf("b", 4.0),
        )

    /** 4px gaps, 6px padding, 20px header so gaps, headers and padding are easy to hit. */
    private val spec =
        TreemapHierarchySpec(
            nodes = nodes,
            tileGapPx = 4.0,
            nodePaddingPx = 6.0,
            headerHeightPx = 20.0,
            contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0),
        )

    private fun plan(
        spec: TreemapHierarchySpec = this.spec,
        highlighted: Set<String> = emptySet(),
    ): TreemapHierarchyPlan =
        ChartPlanner.planTreemapHierarchy(
            ChartLayoutInput(SizeD(400.0, 200.0), ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0))),
            spec,
            highlightedNodeIds = highlighted,
            labelMeasurer = TreemapLabelMeasurer.estimate(),
        )

    private fun tile(
        plan: TreemapHierarchyPlan,
        id: String,
    ) = plan.tiles.single { it.nodeId == id }

    @Test
    fun centersResolveToTheDeepestTile_atBothLevels() {
        val plan = plan()
        for (id in listOf("a1", "a2", "b")) {
            assertEquals(id, TreemapHitResolver.resolve(tile(plan, id).bounds.center, plan)?.nodeId)
        }
    }

    @Test
    fun edgesAreInclusive() {
        val plan = plan()
        val b = tile(plan, "b").bounds
        assertEquals("b", TreemapHitResolver.resolve(OffsetD(b.left, b.top), plan)?.nodeId)
        assertEquals("b", TreemapHitResolver.resolve(OffsetD(b.right, b.bottom), plan)?.nodeId)
    }

    @Test
    fun headerBandAndPaddingResolveToTheParent() {
        val plan = plan()
        val a = tile(plan, "a").bounds
        assertEquals("a", TreemapHitResolver.resolve(OffsetD(a.center.x, a.top + 5.0), plan)?.nodeId)
        assertEquals("a", TreemapHitResolver.resolve(OffsetD(a.left + 2.0, a.center.y), plan)?.nodeId)
    }

    @Test
    fun gapBetweenChildrenResolvesToTheParent_gapBetweenTopLevelTilesToNothing() {
        val plan = plan()
        val a1 = tile(plan, "a1").bounds
        val a2 = tile(plan, "a2").bounds
        val betweenChildren =
            if (a2.left >= a1.right) OffsetD((a1.right + a2.left) / 2.0, a1.center.y) else OffsetD(a1.center.x, (a1.bottom + a2.top) / 2.0)
        assertEquals("a", TreemapHitResolver.resolve(betweenChildren, plan)?.nodeId)

        val a = tile(plan, "a").bounds
        val b = tile(plan, "b").bounds
        val betweenTopLevel = if (b.left >= a.right) OffsetD((a.right + b.left) / 2.0, a.center.y) else OffsetD(a.center.x, (a.bottom + b.top) / 2.0)
        assertNull(TreemapHitResolver.resolve(betweenTopLevel, plan))
        assertNull(TreemapHitResolver.resolve(OffsetD(-1.0, -1.0), plan))
        assertNull(TreemapHitResolver.resolve(OffsetD(Double.NaN, 10.0), plan))
    }

    @Test
    fun resolveAtDepth_answersForAGivenLevel() {
        val plan = plan()
        val center = tile(plan, "a1").bounds.center
        assertEquals("a", TreemapHitResolver.resolveAtDepth(center, plan, depth = 1)?.nodeId)
        assertEquals("a1", TreemapHitResolver.resolveAtDepth(center, plan, depth = 2)?.nodeId)
        assertNull(TreemapHitResolver.resolveAtDepth(tile(plan, "b").bounds.center, plan, depth = 2))
    }

    @Test
    fun hitCarriesPathDepthAndAggregateMembers() {
        val hit = TreemapHitResolver.hitFor(tile(plan(), "a1"))
        assertEquals(listOf("a", "a1"), hit.path)
        assertEquals(2, hit.depth)
        assertEquals(3.0, hit.value)
        assertEquals(false, hit.hasChildren)

        val crowded = spec.copy(nodes = listOf(leaf("big", 100.0)) + (1..20).map { leaf("s$it", 0.1) }, minTileSidePx = 40.0)
        val aggregatePlan = plan(crowded)
        val aggregate = aggregatePlan.tiles.single { it.isAggregate }
        val aggregateHit = TreemapHitResolver.resolve(aggregate.bounds.center, aggregatePlan)!!
        val payload = TreemapHitResolver.hitFor(aggregateHit)
        assertTrue(payload.isAggregate)
        assertEquals((1..20).map { "s$it" }.toSet(), payload.memberIds.toSet())
    }

    // --- L-007: highlight ---------------------------------------------------------------------------------------

    private fun outlines(plan: TreemapHierarchyPlan) =
        plan.layout.overlayDrawCommands
            .filterIsInstance<DrawCommand.RoundRect>()
            .filter { it.style is ShapeDrawStyle.Stroke }

    @Test
    fun highlightedLeafAndParentAreOutlined_insideTheirTiles() {
        for (id in listOf("a1", "a")) {
            val plan = plan(highlighted = setOf(id))
            val outline = outlines(plan).single()
            val bounds = tile(plan, id).bounds
            assertTrue(outline.bounds.left >= bounds.left && outline.bounds.right <= bounds.right)
            assertTrue(outline.bounds.top >= bounds.top && outline.bounds.bottom <= bounds.bottom)
            assertEquals(3.0, (outline.style as ShapeDrawStyle.Stroke).strokeWidth)
        }
    }

    @Test
    fun unknownOrHiddenIds_drawNothingAndDoNotFail() {
        val plain = plan()
        assertEquals(plain, plan(highlighted = setOf("missing")))
        // A node below the depth limit is not drawn, so it is not highlighted either.
        val deep = spec.copy(maxDepth = 1)
        assertTrue(outlines(plan(deep, highlighted = setOf("a1"))).isEmpty())
    }

    @Test
    fun anAggregateIsHighlightedWhenItContainsAHighlightedId() {
        val crowded = spec.copy(nodes = listOf(leaf("big", 100.0)) + (1..20).map { leaf("s$it", 0.1) }, minTileSidePx = 40.0)
        val plan = plan(crowded, highlighted = setOf("s7"))
        assertEquals(1, outlines(plan).size)
    }

    @Test
    fun dimOthers_wrapsEveryTileOutsideTheHighlightedSubtreeInAGroup() {
        val style = HierarchyHighlightStyle(outlineColor = ChartColor.Red, dimOthersAlpha = 0.3)
        val plan = plan(spec.copy(highlight = style), highlighted = setOf("a"))
        val fills = plan.layout.plotDrawCommands
        plan.tiles.forEachIndexed { i, tile ->
            val dimmed = fills[i] is DrawCommand.Group
            assertEquals(tile.path.first() != "a", dimmed, tile.nodeId)
            if (dimmed) assertEquals(0.3, (fills[i] as DrawCommand.Group).alpha)
        }
        assertEquals(ChartColor.Red, (outlines(plan).single().style as ShapeDrawStyle.Stroke).color)
        // Without a highlight nothing is dimmed.
        assertTrue(plan(spec.copy(highlight = style)).layout.plotDrawCommands.none { it is DrawCommand.Group })
    }
}
