package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.SankeyChartPlan
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyLayoutSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyLink
import com.mena97villalobos.composeplotlib.core.spec.SankeyNode
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SankeyHitResolverTest {
    private fun input() =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport =
                ChartViewport(
                    xRange = ClosedRangeD(0.0, 1.0),
                    yRange = ClosedRangeD(0.0, 1.0),
                ),
        )

    private fun plan(spec: SankeyChartSpec): SankeyChartPlan = ChartPlanner.planSankeyChart(input(), spec, selection = null)

    private fun simpleSpec(layout: SankeyLayoutSpec = SankeyLayoutSpec()) =
        SankeyChartSpec(
            id = "s",
            nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1)),
            links = listOf(SankeyLink("A", "B", 5.0)),
            layout = layout,
        )

    @Test
    fun tapInsideNodeRect_returnsNodeSelection() {
        val spec = simpleSpec()
        val p = plan(spec)
        val a = p.nodes.first { it.nodeId == "A" }
        val sel = SankeyHitResolver.resolve(a.pixelBounds.center, spec, p)
        assertNotNull(sel)
        assertEquals(SankeyHitResolver.LayerNode, sel.target.layerId)
        assertEquals(p.nodes.indexOf(a), sel.target.pointIndex)
    }

    @Test
    fun tapOnRibbonMidSpan_returnsLinkSelection() {
        val spec = simpleSpec()
        val p = plan(spec)
        // The marker anchor is the ribbon centerline midpoint — guaranteed inside the drawn ribbon.
        val sel = SankeyHitResolver.resolve(p.links[0].markerAnchor, spec, p)
        assertNotNull(sel)
        assertEquals(SankeyHitResolver.LayerLink, sel.target.layerId)
        assertEquals(0, sel.target.pointIndex)
    }

    @Test
    fun tapJustAboveSampledTopEdge_missesRibbon() {
        val spec =
            SankeyChartSpec(
                id = "s",
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                // The A→B ribbon is thin (5%), so "just above its top edge" is empty background.
                links = listOf(SankeyLink("A", "C", 95.0), SankeyLink("A", "B", 5.0)),
            )
        val p = plan(spec)
        val thin = p.links.first { it.targetNodeId == "B" }
        // Top-edge sample at mid-flow (polygon[8] of 0..16), nudged above the curve.
        val above = OffsetD(thin.pixelPolygon[8].x, thin.pixelPolygon[8].y - 2.0)
        val sel = SankeyHitResolver.resolve(above, spec, p)
        if (sel != null) {
            // Must not claim the thin ribbon; any other hit (e.g. the thick ribbon below) is fine.
            assertEquals(SankeyHitResolver.LayerLink, sel.target.layerId)
            assertEquals(p.links.indexOf(p.links.first { it.targetNodeId == "C" }), sel.target.pointIndex)
        }
    }

    @Test
    fun nodeLinkPriority_followsLinksBehindNodes() {
        // A long ribbon (S→T across three columns) passes behind/over the middle column's node M.
        fun spec(linksBehindNodes: Boolean) =
            SankeyChartSpec(
                id = "s",
                nodes =
                    listOf(
                        SankeyNode("S", column = 0),
                        SankeyNode("S2", column = 0),
                        SankeyNode("M", column = 1),
                        SankeyNode("T", column = 2),
                    ),
                links = listOf(SankeyLink("S", "T", 10.0), SankeyLink("S2", "M", 5.0)),
                layout = SankeyLayoutSpec(linksBehindNodes = linksBehindNodes),
            )

        val behindSpec = spec(linksBehindNodes = true)
        val behindPlan = plan(behindSpec)
        val m = behindPlan.nodes.first { it.nodeId == "M" }
        val longRibbon = behindPlan.links.first { it.targetNodeId == "T" }
        // Find a probe point inside BOTH the node M and the long ribbon's polygon bounds.
        val probe =
            OffsetD(
                x = m.pixelBounds.center.x,
                y = maxOf(m.pixelBounds.top + 2.0, longRibbon.pixelBounds.top + 2.0),
            )
        val nodeFirst = SankeyHitResolver.resolve(probe, behindSpec, behindPlan)
        assertNotNull(nodeFirst)
        assertEquals(
            SankeyHitResolver.LayerNode,
            nodeFirst.target.layerId,
            "with links behind nodes, the node (drawn on top) must win the tap",
        )

        val frontSpec = spec(linksBehindNodes = false)
        val frontPlan = plan(frontSpec)
        val frontSel = SankeyHitResolver.resolve(probe, frontSpec, frontPlan)
        assertNotNull(frontSel)
        assertEquals(
            SankeyHitResolver.LayerLink,
            frontSel.target.layerId,
            "with links drawn on top, the ribbon must win the tap",
        )
    }

    @Test
    fun tapOutsideEverything_returnsNull() {
        val spec = simpleSpec()
        val p = plan(spec)
        assertNull(SankeyHitResolver.resolve(OffsetD(1.0, 1.0), spec, p))
    }

    @Test
    fun selectionIndices_roundTripToPlannedLinks_whenZeroValueLinksAreSkipped() {
        val spec =
            SankeyChartSpec(
                id = "s",
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1), SankeyNode("C", column = 1)),
                // The zero-value link is not planned; indices must address plan.links, not spec.links.
                links = listOf(SankeyLink("A", "B", 0.0), SankeyLink("A", "C", 5.0)),
            )
        val p = plan(spec)
        assertEquals(1, p.links.size)
        val sel = SankeyHitResolver.resolve(p.links[0].markerAnchor, spec, p)
        assertNotNull(sel)
        assertEquals(0, sel.target.pointIndex)
        assertEquals("C", p.links[sel.target.pointIndex].targetNodeId)
    }
}
