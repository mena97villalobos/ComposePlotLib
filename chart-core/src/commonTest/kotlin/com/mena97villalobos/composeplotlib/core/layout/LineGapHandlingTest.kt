package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.DataDomain
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Null / missing-data gap handling: a non-finite y marks a gap that breaks the line unless connectAcrossGaps. */
class LineGapHandlingTest {
    private fun planWith(
        points: List<ChartPoint>,
        connectAcrossGaps: Boolean,
        lineStyle: LineStyle = LineStyle(color = ChartColor.Blue),
    ): LineChartPlan =
        ChartPlanner.planLineChart(
            input =
                ChartLayoutInput(
                    canvasSize = SizeD(320.0, 200.0),
                    viewport =
                        ChartViewport(
                            xRange = ClosedRangeD(0.0, 4.0),
                            yRange = ClosedRangeD(0.0, 6.0),
                        ),
                ),
            spec =
                LineChartSpec(
                    series =
                        listOf(
                            LineSeries(
                                id = "s",
                                label = "S",
                                lineStyle = lineStyle,
                                points = points,
                            ),
                        ),
                    lineRender = LineRenderSpec(connectAcrossGaps = connectAcrossGaps),
                ),
            selection = null,
        )

    private fun polylineCount(plan: LineChartPlan): Int = plan.layout.plotDrawCommands.count { it is DrawCommand.Polyline }

    private val gappedPoints =
        listOf(
            ChartPoint(0.0, 1.0),
            ChartPoint(1.0, 2.0),
            ChartPoint.gap(2.0),
            ChartPoint(3.0, 4.0),
            ChartPoint(4.0, 5.0),
        )

    @Test
    fun gapSplitsLineIntoTwoFragments() {
        val plan = planWith(gappedPoints, connectAcrossGaps = false)
        // Two finite runs of two points each -> two separate polylines.
        assertEquals(2, polylineCount(plan))
    }

    @Test
    fun connectAcrossGapsBridgesIntoOneFragment() {
        val plan = planWith(gappedPoints, connectAcrossGaps = true)
        assertEquals(1, polylineCount(plan))
    }

    @Test
    fun noGapProducesSingleFragment() {
        val plan =
            planWith(
                listOf(
                    ChartPoint(0.0, 1.0),
                    ChartPoint(1.0, 2.0),
                    ChartPoint(3.0, 4.0),
                    ChartPoint(4.0, 5.0),
                ),
                connectAcrossGaps = false,
            )
        assertEquals(1, polylineCount(plan))
    }

    @Test
    fun areaFillAlsoBreaksAtGap() {
        val filled = LineStyle(color = ChartColor.Blue, areaFill = ChartColor(0x551565C0L))
        val broken = planWith(gappedPoints, connectAcrossGaps = false, lineStyle = filled)
        val bridged = planWith(gappedPoints, connectAcrossGaps = true, lineStyle = filled)
        val brokenAreas = broken.layout.plotDrawCommands.count { it is DrawCommand.Path }
        val bridgedAreas = bridged.layout.plotDrawCommands.count { it is DrawCommand.Path }
        assertEquals(2, brokenAreas, "area fill should split into one path per run")
        assertEquals(1, bridgedAreas, "connectAcrossGaps yields a single area fill")
    }

    @Test
    fun gapPointExcludedFromDataDomain() {
        // A gap must not drag the y-extent to NaN; bounds come only from finite samples.
        val domain =
            DataDomain.fromPoints(
                listOf(
                    XyPoint(0.0, 1.0),
                    XyPoint(1.0, Double.NaN),
                    XyPoint(2.0, 3.0),
                ),
            )
        assertTrue(domain.yRange.start.isFinite() && domain.yRange.endInclusive.isFinite())
        assertEquals(1.0, domain.yRange.start)
        assertEquals(3.0, domain.yRange.endInclusive)
        // Gap x still counts toward the x-extent so the viewport spans the hole.
        assertEquals(0.0, domain.xRange.start)
        assertEquals(2.0, domain.xRange.endInclusive)
    }

    @Test
    fun chartPointGapFactoryMarksGap() {
        val gap = ChartPoint.gap(7.0)
        assertTrue(gap.isGap)
        assertTrue(gap.y.isNaN())
        assertEquals(7.0, gap.x)
        assertFalse(ChartPoint(7.0, 0.0).isGap)
    }
}
