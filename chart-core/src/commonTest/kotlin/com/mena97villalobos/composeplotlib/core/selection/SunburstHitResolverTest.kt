package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.DrawCommand
import com.mena97villalobos.composeplotlib.core.layout.PlannedSunburstSegment
import com.mena97villalobos.composeplotlib.core.layout.SunburstHierarchyPlan
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode
import com.mena97villalobos.composeplotlib.core.spec.nodeId
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SunburstHitResolverTest {
    private val tree =
        SunburstNode(
            label = "Root",
            id = "root",
            children =
                listOf(
                    SunburstNode(
                        "Docs",
                        id = "docs",
                        children = listOf(SunburstNode("Pdf", 3.0, id = "pdf"), SunburstNode("Txt", 1.0, id = "txt")),
                    ),
                    SunburstNode("Media", 4.0, id = "media"),
                ),
        )
    private val spec = SunburstChartSpec(root = tree, ringGapPx = 4.0)
    private val input = ChartLayoutInput(SizeD(400.0, 400.0), ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)))

    private fun plan(
        spec: SunburstChartSpec = this.spec,
        root: String? = null,
        highlighted: Set<String> = emptySet(),
        style: HierarchyHighlightStyle = HierarchyHighlightStyle(),
    ): SunburstHierarchyPlan = ChartPlanner.planSunburstHierarchy(input, spec, root, highlighted, style)

    private fun SunburstHierarchyPlan.segment(id: String) = segments.single { it.nodeId == id }

    /** A point in the middle of [segment] (mid radius, mid angle). */
    private fun SunburstHierarchyPlan.middleOf(segment: PlannedSunburstSegment): OffsetD {
        val r = if (segment.depth == 0) segment.outerRadius / 2.0 else (segment.innerRadius + segment.outerRadius) / 2.0
        val a = (segment.startAngleDegrees + segment.sweepAngleDegrees / 2.0) * PI / 180.0
        return OffsetD(center.x + r * cos(a), center.y + r * sin(a))
    }

    @Test
    fun drawing_isIdenticalToTheReadOnlyPlanner_whenNothingIsDrilledOrHighlighted() {
        for (s in listOf(spec, spec.copy(showRoot = false), spec.copy(sweepDegrees = 180.0, startAngleDegrees = 0.0))) {
            assertEquals(ChartPlanner.planSunburstChart(input, s).layout, plan(s).layout)
        }
    }

    @Test
    fun tappingASegmentAtAnyRing_reportsThatNode() {
        val plan = plan()
        for (id in listOf("root", "docs", "media", "pdf", "txt")) {
            val segment = plan.segment(id)
            assertEquals(id, SunburstHitResolver.resolve(plan.middleOf(segment), plan)?.nodeId, id)
        }
        assertEquals(listOf("root", "docs", "pdf"), plan.segment("pdf").path)
        assertEquals(2, plan.segment("pdf").depth)
    }

    @Test
    fun anglesWrapAroundTheStartAngle() {
        // Start at 12 o'clock (-90°); "media" covers the second half of the turn, which crosses 180°/-180°.
        val plan = plan()
        val media = plan.segment("media")
        val r = (media.innerRadius + media.outerRadius) / 2.0
        val west = OffsetD(plan.center.x - r, plan.center.y + 0.01)
        val westAbove = OffsetD(plan.center.x - r, plan.center.y - 0.01)
        assertEquals("media", SunburstHitResolver.resolve(west, plan)?.nodeId)
        assertEquals("media", SunburstHitResolver.resolve(westAbove, plan)?.nodeId)
    }

    @Test
    fun ringGaps_outsideTheRings_andOutsideAPartialSweep_resolveToNothing() {
        val plan = plan()
        val docs = plan.segment("docs")
        val gapRadius = docs.innerRadius - 1.0
        val a = (docs.startAngleDegrees + docs.sweepAngleDegrees / 2.0) * PI / 180.0
        assertNull(SunburstHitResolver.resolve(OffsetD(plan.center.x + gapRadius * cos(a), plan.center.y + gapRadius * sin(a)), plan))
        assertNull(SunburstHitResolver.resolve(OffsetD(0.0, 0.0), plan))
        assertNull(SunburstHitResolver.resolve(OffsetD(Double.NaN, 1.0), plan))

        val half = plan(spec.copy(startAngleDegrees = 0.0, sweepDegrees = 180.0))
        val ring1 = half.segment("docs")
        val r = (ring1.innerRadius + ring1.outerRadius) / 2.0
        assertNull(SunburstHitResolver.resolve(OffsetD(half.center.x, half.center.y - r), half), "the top half is not drawn")
    }

    @Test
    fun drillIn_showsTheNodeAsTheCenter_withStableColors() {
        val full = plan()
        val drilled = plan(root = "docs")
        assertEquals("docs", drilled.rootNodeId)
        assertEquals(listOf("root", "docs"), drilled.rootPath)
        assertEquals(0, drilled.segment("docs").depth)
        assertEquals(1, drilled.segment("pdf").depth)
        assertEquals(360.0 * 3.0 / 4.0, drilled.segment("pdf").sweepAngleDegrees, 1e-9)
        assertEquals(full.segment("pdf").color, drilled.segment("pdf").color)
        assertEquals(setOf("docs", "pdf", "txt"), drilled.segments.map { it.nodeId }.toSet())
        // Unknown ids fall back to the spec's root.
        assertEquals(full, plan(root = "missing"))
    }

    @Test
    fun hit_hasTheTreemapCallbackShape() {
        val plan = plan()
        val hit = SunburstHitResolver.hitFor(plan.segment("docs"), plan)
        assertEquals("docs", hit.nodeId)
        assertEquals(listOf("root", "docs"), hit.path)
        assertTrue(hit.hasChildren)
        assertEquals(4.0, hit.value)
        val m = plan.middleOf(plan.segment("docs"))
        assertTrue(hit.bounds.contains(m))
    }

    @Test
    fun nodesWithoutAnId_areIdentifiedByTheirLabel() {
        assertEquals("Pdf", SunburstNode("Pdf", 3.0).nodeId)
        val unnamed = SunburstChartSpec(root = SunburstNode("R", children = listOf(SunburstNode("A", 1.0), SunburstNode("B", 2.0))))
        assertEquals(setOf("R", "A", "B"), plan(unnamed).segments.map { it.nodeId }.toSet())
    }

    @Test
    fun highlight_outlinesTheSegment_andCanDimTheRest() {
        val plan = plan(highlighted = setOf("pdf", "missing"), style = HierarchyHighlightStyle(dimOthersAlpha = 0.4))
        val outline = plan.layout.overlayDrawCommands.single() as DrawCommand.Arc
        assertTrue(outline.style is ShapeDrawStyle.Stroke)
        assertEquals(plan.segment("pdf").startAngleDegrees, outline.startAngleDegrees)
        val dimmed = plan.layout.plotDrawCommands.count { it is DrawCommand.Group }
        assertEquals(plan.segments.size - 1, dimmed)
        assertTrue(plan(highlighted = setOf("missing")).layout.overlayDrawCommands.isEmpty())
    }
}
