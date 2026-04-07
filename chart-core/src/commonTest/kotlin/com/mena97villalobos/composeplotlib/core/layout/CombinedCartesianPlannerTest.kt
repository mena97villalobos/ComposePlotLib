package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.CombinedCartesianHitResolver
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CombinedCartesianPlannerTest {
    @Test
    fun combined_emitsBarsThenLineDrawCommands_andLayerIds() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries =
                                            listOf(
                                                BarEntry(0.0, 5.0),
                                                BarEntry(1.0, 8.0),
                                            ),
                                    ),
                                ),
                        ),
                        CombinedCartesianLayer.Line(
                            id = "l",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "s",
                                        label = "S",
                                        lineStyle = LineStyle(color = ChartColor.Blue),
                                        pointStyle = PointStyle(radius = 3.0),
                                        points =
                                            listOf(
                                                ChartPoint(0.0, 2.0),
                                                ChartPoint(1.0, 6.0),
                                            ),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 4),
                legend = LegendSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 280.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 1.5),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = false,
            )
        val plot = plan.layout.plotDrawCommands
        val firstRectIdx = plot.indexOfFirst { it is DrawCommand.Rect }
        val firstPolyIdx = plot.indexOfFirst { it is DrawCommand.Polyline }
        assertTrue(firstRectIdx >= 0, "expected bar rects")
        assertTrue(firstPolyIdx >= 0, "expected line polylines")
        assertTrue(firstRectIdx < firstPolyIdx, "bars (lower z) should appear before line polylines")
        assertTrue(plan.allBarSegments.isNotEmpty())
        assertTrue(plan.allBarSegments.all { it.layerId == "b" })
        assertTrue(plan.yTicksSecondary.isEmpty())
    }

    @Test
    fun dualY_emitsSecondaryYTicks() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries = listOf(BarEntry(0.0, 4.0), BarEntry(1.0, 6.0)),
                                    ),
                                ),
                        ),
                        CombinedCartesianLayer.Line(
                            id = "l",
                            yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "s",
                                        label = "S",
                                        lineStyle = LineStyle(color = ChartColor.Red),
                                        pointStyle = PointStyle(radius = 3.0),
                                        points = listOf(ChartPoint(0.0, 40.0), ChartPoint(1.0, 90.0)),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 3),
                yAxisSecondary = AxisSpec(tickCount = 3),
                legend = LegendSpec(),
                grid = GridSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(420.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 1.5),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = true,
            )
        assertTrue(plan.yTicksSecondary.isNotEmpty())
        assertNotNull(plan.hitTestLayers.find { it is CombinedHitTestLayer.Bars })
        assertNotNull(plan.hitTestLayers.find { it is CombinedHitTestLayer.Lines })
    }

    @Test
    fun dualY_marksSecondaryAxisTickTextForTypographySlot() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries = listOf(BarEntry(0.0, 4.0), BarEntry(1.0, 6.0)),
                                    ),
                                ),
                        ),
                        CombinedCartesianLayer.Line(
                            id = "l",
                            yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "s",
                                        label = "S",
                                        lineStyle = LineStyle(color = ChartColor.Red),
                                        pointStyle = PointStyle(radius = 3.0),
                                        points = listOf(ChartPoint(0.0, 40.0), ChartPoint(1.0, 90.0)),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 3),
                yAxisSecondary = AxisSpec(tickCount = 3),
                legend = LegendSpec(),
                grid = GridSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(420.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 1.5),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = true,
            )
        val texts = plan.layout.overlayDrawCommands.filterIsInstance<DrawCommand.Text>()
        val primaryYTicks = texts.filter { it.isYAxisTick && !it.isSecondaryYAxisTick }
        val secondaryYTicks = texts.filter { it.isSecondaryYAxisTick }
        assertTrue(primaryYTicks.isNotEmpty())
        assertTrue(secondaryYTicks.isNotEmpty())
        assertTrue(secondaryYTicks.all { it.isYAxisTick })
    }

    @Test
    fun dualY_secondaryLine_constantY_expandsDegenerateSecondarySpan() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries = listOf(BarEntry(0.0, 4.0), BarEntry(1.0, 6.0)),
                                    ),
                                ),
                        ),
                        CombinedCartesianLayer.Line(
                            id = "l",
                            yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "flat",
                                        label = "Flat",
                                        lineStyle = LineStyle(color = ChartColor.Blue),
                                        pointStyle = PointStyle(radius = 3.0),
                                        points =
                                            listOf(
                                                ChartPoint(0.0, 42.0),
                                                ChartPoint(1.0, 42.0),
                                                ChartPoint(2.0, 42.0),
                                            ),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 3),
                yAxisSecondary = AxisSpec(tickCount = 3),
                legend = LegendSpec(),
                grid = GridSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(420.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 2.5),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = true,
            )
        val sec = plan.secondaryViewport
        assertNotNull(sec)
        assertTrue(sec.yRange.span >= FpMath.DEFAULT_DEGENERATE_PAD * 0.99)
        assertTrue(plan.yTicksSecondary.isNotEmpty())
    }

    @Test
    fun hitResolver_prefersTopLayerLineOverBar() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries = listOf(BarEntry(0.5, 7.0)),
                                    ),
                                ),
                        ),
                        CombinedCartesianLayer.Line(
                            id = "l",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "s",
                                        label = "S",
                                        lineStyle = LineStyle(color = ChartColor.Blue),
                                        pointStyle = PointStyle(radius = 4.0, visible = true),
                                        points = listOf(ChartPoint(0.5, 7.0)),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 4),
                legend = LegendSpec(),
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(360.0, 260.0),
                        viewport = viewport,
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = false,
            )
        val px = plan.layout.plotBounds.center
        val sel =
            CombinedCartesianHitResolver.resolve(
                spec = spec,
                plan = plan,
                primaryViewport = viewport,
                touchPx = OffsetD(px.x, px.y),
                plotBounds = plan.layout.plotBounds,
                lineMaxDistancePx = 80.0,
            )
        assertNotNull(sel)
        assertEquals("l", sel.target.layerId)
        assertEquals("s", sel.target.seriesId)
    }

    @Test
    fun hitResolver_barOnly_carriesCombinedLayerIdOnTarget() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "barsLayer",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g",
                                        label = "G",
                                        style = BarStyle(color = ChartColor.Green, barWidth = 28.0),
                                        entries = listOf(BarEntry(0.5, 6.0)),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 4),
                legend = LegendSpec(),
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(360.0, 260.0),
                        viewport = viewport,
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = false,
            )
        val seg = plan.allBarSegments.single()
        val touch =
            OffsetD(
                (seg.bounds.left + seg.bounds.right) / 2.0,
                (seg.bounds.top + seg.bounds.bottom) / 2.0,
            )
        val sel =
            CombinedCartesianHitResolver.resolve(
                spec = spec,
                plan = plan,
                primaryViewport = viewport,
                touchPx = touch,
                plotBounds = plan.layout.plotBounds,
                lineMaxDistancePx = 28.0,
            )
        assertNotNull(sel)
        assertEquals("barsLayer", sel.target.layerId)
        assertEquals("g", sel.target.seriesId)
    }

    @Test
    fun dualY_secondaryViewport_yRange_fromSecondaryLayerData() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Line(
                            id = "sec",
                            yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                            series =
                                listOf(
                                    LineSeries(
                                        id = "s",
                                        label = "S",
                                        lineStyle = LineStyle(color = ChartColor.Red),
                                        pointStyle = PointStyle(radius = 3.0),
                                        points =
                                            listOf(
                                                ChartPoint(0.0, 12.0),
                                                ChartPoint(1.0, 48.0),
                                            ),
                                    ),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 4),
                yAxisSecondary = AxisSpec(tickCount = 4),
                legend = LegendSpec(),
                grid = GridSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 280.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 1.5),
                                yRange = ClosedRangeD(0.0, 100.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = true,
            )
        val sec = plan.secondaryViewport!!
        assertEquals(12.0, sec.yRange.start, absoluteTolerance = 1e-9)
        assertEquals(48.0, sec.yRange.endInclusive, absoluteTolerance = 1e-9)
    }

    @Test
    fun barLayer_clusteredMismatchedGroups_fallsBackToIndependentLayout() {
        val spec =
            CombinedCartesianChartSpec(
                layers =
                    listOf(
                        CombinedCartesianLayer.Bar(
                            id = "b",
                            yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                            groups =
                                listOf(
                                    BarGroup(
                                        id = "g1",
                                        label = "G1",
                                        style = BarStyle(color = ChartColor.Green),
                                        entries = listOf(BarEntry(0.0, 4.0), BarEntry(1.0, 6.0)),
                                    ),
                                    BarGroup(
                                        id = "g2",
                                        label = "G2",
                                        style = BarStyle(color = ChartColor.Red),
                                        entries = listOf(BarEntry(0.0, 2.0)),
                                    ),
                                ),
                            barLayout = BarLayoutMode.Clustered,
                        ),
                    ),
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 4),
                legend = LegendSpec(),
                grid = GridSpec(),
            )
        val plan =
            ChartPlanner.planCombinedCartesianChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(420.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-0.5, 1.5),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
                reserveRightYAxisSpace = false,
            )
        assertEquals(3, plan.allBarSegments.size)
    }
}
