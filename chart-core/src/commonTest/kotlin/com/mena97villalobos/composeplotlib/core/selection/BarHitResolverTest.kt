package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.PlannedBarSegment
import com.mena97villalobos.composeplotlib.core.layout.forHitTestFromDrawOrder
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BarHitResolverTest {
    private val a =
        PlannedBarSegment(
            groupId = "a",
            categoryIndex = 0,
            entry = BarEntry(1.0, 3.0),
            bounds = RectD(40.0, 120.0, 60.0, 180.0),
            fillColor = ChartColor.Green,
        )
    private val b =
        PlannedBarSegment(
            groupId = "b",
            categoryIndex = 0,
            entry = BarEntry(1.0, 2.0),
            bounds = RectD(40.0, 80.0, 60.0, 120.0),
            fillColor = ChartColor.Red,
        )

    @Test
    fun plannedHitTestPrefersTopSegmentFirstInList() {
        val hitFirstTop =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(b, a),
                touchPoint = OffsetD(50.0, 100.0),
            )
        assertNotNull(hitFirstTop)
        assertEquals("b", hitFirstTop.target.seriesId)
        assertEquals(OffsetD(50.0, 80.0), hitFirstTop.pixelOffset)
        assertEquals(b.bounds.center, hitFirstTop.markerAnchor)
        assertEquals(b.bounds, hitFirstTop.hitBounds)
    }

    @Test
    fun plannedHitTestFallsThroughToLowerWhenTopMisses() {
        val hit =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(b, a),
                touchPoint = OffsetD(50.0, 150.0),
            )
        assertNotNull(hit)
        assertEquals("a", hit.target.seriesId)
        assertTrue(hit.markerAnchor!!.y > hit.pixelOffset.y)
    }

    @Test
    fun plannedHitTestReturnsNullOutsideAllBars() {
        assertNull(
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(b, a),
                touchPoint = OffsetD(5.0, 5.0),
            ),
        )
    }

    @Test
    fun legacyTargetsExposeMarkerAnchorAndBounds() {
        val sel =
            BarHitResolver.resolve(
                seriesId = "g",
                bars =
                    listOf(
                        BarHitTarget(0, XyPoint(1.0, 2.0), RectD(0.0, 10.0, 20.0, 40.0)),
                    ),
                touchPoint = OffsetD(10.0, 25.0),
            )
        assertNotNull(sel)
        assertEquals(RectD(0.0, 10.0, 20.0, 40.0), sel.hitBounds)
        assertEquals(OffsetD(10.0, 10.0), sel.pixelOffset)
    }

    @Test
    fun nonFiniteTouchReturnsNull_forPlannedAndLegacy() {
        assertNull(BarHitResolver.resolvePlanned(listOf(a), OffsetD(50.0, Double.NaN)))
        assertNull(
            BarHitResolver.resolve(
                seriesId = "g",
                bars =
                    listOf(
                        BarHitTarget(0, XyPoint(1.0, 2.0), RectD(0.0, 10.0, 20.0, 40.0)),
                    ),
                touchPoint = OffsetD(Double.POSITIVE_INFINITY, 25.0),
            ),
        )
    }

    @Test
    fun plannedHitTestSkipsInvalidBoundsAndFallsThroughToNextSegment() {
        val badInverted = a.copy(bounds = RectD(60.0, 120.0, 40.0, 180.0))
        val afterBad =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(badInverted, a),
                touchPoint = OffsetD(50.0, 150.0),
            )
        assertNotNull(afterBad)
        assertEquals("a", afterBad.target.seriesId)

        val badNan = a.copy(bounds = RectD(Double.NaN, 120.0, 60.0, 180.0))
        assertNull(
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(badNan),
                touchPoint = OffsetD(50.0, 150.0),
            ),
        )
    }

    @Test
    fun legacyResolveSkipsInvalidBounds() {
        assertNull(
            BarHitResolver.resolve(
                seriesId = "g",
                bars =
                    listOf(
                        BarHitTarget(0, XyPoint(1.0, 2.0), RectD(20.0, 10.0, 10.0, 40.0)),
                    ),
                touchPoint = OffsetD(15.0, 25.0),
            ),
        )
    }

    @Test
    fun clusteredSideBySide_tapSelectsMatchingGroup() {
        val left =
            PlannedBarSegment(
                groupId = "left",
                categoryIndex = 0,
                entry = BarEntry(1.0, 4.0),
                bounds = RectD(10.0, 50.0, 40.0, 200.0),
                fillColor = ChartColor.Green,
                barLayoutMode = BarLayoutMode.Clustered,
            )
        val right =
            PlannedBarSegment(
                groupId = "right",
                categoryIndex = 0,
                entry = BarEntry(1.0, 6.0),
                bounds = RectD(50.0, 30.0, 80.0, 200.0),
                fillColor = ChartColor.Red,
                barLayoutMode = BarLayoutMode.Clustered,
            )
        val hitLeft =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(right, left),
                touchPoint = OffsetD(25.0, 100.0),
            )
        assertNotNull(hitLeft)
        assertEquals("left", hitLeft.target.seriesId)
        val hitRight =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = listOf(right, left),
                touchPoint = OffsetD(65.0, 100.0),
            )
        assertNotNull(hitRight)
        assertEquals("right", hitRight.target.seriesId)
    }

    @Test
    fun stackedFromPlanner_hitTestOrder_matchesBarChartPlanForTopAndBottomSlice() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "bottom",
                            label = "B",
                            style = BarStyle(color = ChartColor.Green, barWidth = 24.0),
                            entries = listOf(BarEntry(1.0, 3.0)),
                        ),
                        BarGroup(
                            id = "top",
                            label = "T",
                            style = BarStyle(color = ChartColor.Red, barWidth = 24.0),
                            entries = listOf(BarEntry(1.0, 2.0)),
                        ),
                    ),
                barLayout = BarLayoutMode.Stacked,
                xAxis = AxisSpec(showGridLines = false, tickCount = 4),
                yAxis = AxisSpec(showGridLines = false, tickCount = 4),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 4.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertEquals(2, plan.barSegments.size)
        assertEquals(plan.barSegmentsForHitTest(), plan.barSegments.forHitTestFromDrawOrder())
        val bottomSeg = plan.barSegments.first { it.groupId == "bottom" }
        val topSeg = plan.barSegments.first { it.groupId == "top" }
        val midBottom =
            OffsetD(
                (bottomSeg.bounds.left + bottomSeg.bounds.right) / 2.0,
                (bottomSeg.bounds.top + bottomSeg.bounds.bottom) / 2.0,
            )
        val midTop =
            OffsetD(
                (topSeg.bounds.left + topSeg.bounds.right) / 2.0,
                (topSeg.bounds.top + topSeg.bounds.bottom) / 2.0,
            )
        val hitBottom =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = plan.barSegmentsForHitTest(),
                touchPoint = midBottom,
            )
        assertNotNull(hitBottom)
        assertEquals("bottom", hitBottom.target.seriesId)
        assertEquals(bottomSeg.bounds, hitBottom.hitBounds)
        val hitTop =
            BarHitResolver.resolvePlanned(
                segmentsHitTestFirst = plan.barSegmentsForHitTest(),
                touchPoint = midTop,
            )
        assertNotNull(hitTop)
        assertEquals("top", hitTop.target.seriesId)
        assertEquals(topSeg.bounds, hitTop.hitBounds)
    }

    @Test
    fun stackedThreeLayers_hitTestOrder_prefersTopMiddleBottom() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "bottom",
                            label = "B0",
                            style = BarStyle(color = ChartColor.Green, barWidth = 24.0),
                            entries = listOf(BarEntry(1.0, 2.0)),
                        ),
                        BarGroup(
                            id = "mid",
                            label = "M",
                            style = BarStyle(color = ChartColor.Blue, barWidth = 24.0),
                            entries = listOf(BarEntry(1.0, 3.0)),
                        ),
                        BarGroup(
                            id = "top",
                            label = "T",
                            style = BarStyle(color = ChartColor.Red, barWidth = 24.0),
                            entries = listOf(BarEntry(1.0, 4.0)),
                        ),
                    ),
                barLayout = BarLayoutMode.Stacked,
                xAxis = AxisSpec(showGridLines = false, tickCount = 4),
                yAxis = AxisSpec(showGridLines = false, tickCount = 4),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 4.0),
                                yRange = ClosedRangeD(0.0, 12.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertEquals(3, plan.barSegments.size)
        assertEquals(plan.barSegmentsForHitTest(), plan.barSegments.forHitTestFromDrawOrder())
        val bottom = plan.barSegments.first { it.groupId == "bottom" }
        val mid = plan.barSegments.first { it.groupId == "mid" }
        val top = plan.barSegments.first { it.groupId == "top" }

        fun centerY(seg: PlannedBarSegment) = (seg.bounds.top + seg.bounds.bottom) / 2.0

        fun hitAt(seg: PlannedBarSegment) =
            OffsetD(
                (seg.bounds.left + seg.bounds.right) / 2.0,
                centerY(seg),
            )
        assertEquals(
            "top",
            BarHitResolver.resolvePlanned(plan.barSegmentsForHitTest(), hitAt(top))!!.target.seriesId,
        )
        assertEquals(
            "mid",
            BarHitResolver.resolvePlanned(plan.barSegmentsForHitTest(), hitAt(mid))!!.target.seriesId,
        )
        assertEquals(
            "bottom",
            BarHitResolver.resolvePlanned(plan.barSegmentsForHitTest(), hitAt(bottom))!!.target.seriesId,
        )
    }
}
