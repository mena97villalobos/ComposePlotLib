package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.AxisTickDensity
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.XTickLabelOverlapStrategy
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ChartPlannerTest {
    @Test
    fun planLineChart_identicalInputs_reusesGridLineDrawCommands() {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(300.0, 200.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 9.0),
                        yRange = ClosedRangeD(0.0, 23.0),
                    ),
            )
        val spec = LineChartSpec(series = emptyList())
        val a = ChartPlanner.planLineChart(input, spec, selection = null)
        val b = ChartPlanner.planLineChart(input, spec, selection = null)
        val gridLinesA = a.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        val gridLinesB = b.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertTrue(gridLinesA.isNotEmpty())
        assertEquals(gridLinesA.size, gridLinesB.size)
        for (i in gridLinesA.indices) {
            assertSame(gridLinesA[i], gridLinesB[i], "grid line $i")
        }
    }

    @Test
    fun generatesReadableTicksAcrossViewport() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series =
                            listOf(
                                LineSeries(
                                    id = "series",
                                    label = "Series",
                                    lineStyle = LineStyle(color = ChartColor.Blue),
                                    points =
                                        listOf(
                                            ChartPoint(0.0, 1.0),
                                            ChartPoint(3.0, 8.0),
                                            ChartPoint(9.0, 21.0),
                                        ),
                                ),
                            ),
                    ),
                selection = null,
            )

        assertTrue(plan.layout.xTicks.size >= 2)
        assertTrue(plan.layout.yTicks.size >= 2)
        assertTrue(plan.layout.xTicks.first().value <= 0.0)
        assertTrue(plan.layout.xTicks.last().value >= 9.0)
    }

    @Test
    fun axisTickDrawCommandsIncludeBaselineFromPlannerMetrics() {
        val baseInput =
            ChartLayoutInput(
                canvasSize = SizeD(300.0, 200.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 9.0),
                        yRange = ClosedRangeD(0.0, 23.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "series",
                            label = "Series",
                            lineStyle = LineStyle(color = ChartColor.Blue),
                            points =
                                listOf(
                                    ChartPoint(0.0, 1.0),
                                    ChartPoint(9.0, 21.0),
                                ),
                        ),
                    ),
            )
        val preliminary = ChartPlanner.planLineChart(baseInput, spec, selection = null)
        val expectedBaseline = 9.0
        val tickMetrics =
            buildMap {
                for (t in preliminary.layout.xTicks) {
                    if (t.showLabel) {
                        put(
                            t.label,
                            TextLayoutMetrics(
                                width = t.labelSize.width,
                                height = t.labelSize.height,
                                firstBaselineFromTop = expectedBaseline,
                                lastBaselineFromTop = expectedBaseline,
                            ),
                        )
                    }
                }
                for (t in preliminary.layout.yTicks) {
                    if (t.showLabel) {
                        put(
                            t.label,
                            TextLayoutMetrics(
                                width = t.labelSize.width,
                                height = t.labelSize.height,
                                firstBaselineFromTop = expectedBaseline,
                                lastBaselineFromTop = expectedBaseline,
                            ),
                        )
                    }
                }
            }
        val measuredPlan =
            ChartPlanner.planLineChart(
                input =
                    baseInput.copy(
                        textMeasurements = ChartTextMeasurementInput(tickLabelMetrics = tickMetrics),
                    ),
                spec = spec,
                selection = null,
            )
        val texts = measuredPlan.layout.overlayDrawCommands.filterIsInstance<DrawCommand.Text>()
        assertTrue(texts.isNotEmpty())
        assertTrue(texts.all { it.firstBaselineFromTop == expectedBaseline })
    }

    @Test
    fun precomputedTickValuePassMatchesImplicitCartesianPlanning() {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(300.0, 200.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 9.0),
                        yRange = ClosedRangeD(0.0, 23.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "series",
                            label = "Series",
                            lineStyle = LineStyle(color = ChartColor.Blue),
                            points =
                                listOf(
                                    ChartPoint(0.0, 1.0),
                                    ChartPoint(3.0, 8.0),
                                    ChartPoint(9.0, 21.0),
                                ),
                        ),
                    ),
            )
        val defaultPlan = ChartPlanner.planLineChart(input, spec, selection = null)
        val tickPass = ChartPlanner.computeCartesianTickValuePass(input.viewport, spec.xAxis, spec.yAxis)
        val reusedPlan =
            ChartPlanner.planLineChart(
                input = input,
                spec = spec,
                selection = null,
                tickValuePass = tickPass,
            )

        fun tickPairs(plan: LineChartPlan) = plan.layout.xTicks.map { it.value to it.label } to plan.layout.yTicks.map { it.value to it.label }
        assertEquals(tickPairs(defaultPlan), tickPairs(reusedPlan))
    }

    @Test
    fun cartesianFinalLayoutDependsOnTextMeasurementsForCacheInvalidationContract() {
        val inputNarrow =
            ChartLayoutInput(
                canvasSize = SizeD(280.0, 200.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 6.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = (0..6).associate { it.toString() to m(8.0, 12.0) },
                        fallbackTickLabelMetrics = m(8.0, 12.0),
                    ),
            )
        val inputWide =
            inputNarrow.copy(
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = (0..6).associate { it.toString() to m(120.0, 12.0) },
                        fallbackTickLabelMetrics = m(120.0, 12.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis = AxisSpec(tickCount = 7, tickDensity = AxisTickDensity.MaxTicks(7), showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val tickPass = ChartPlanner.computeCartesianTickValuePass(inputNarrow.viewport, spec.xAxis, spec.yAxis)
        val narrow = ChartPlanner.planLineChart(inputNarrow, spec, selection = null, tickValuePass = tickPass)
        val wide = ChartPlanner.planLineChart(inputWide, spec, selection = null, tickValuePass = tickPass)
        assertTrue(
            narrow.layout.xTicks.count { it.showLabel } > wide.layout.xTicks.count { it.showLabel },
        )
    }

    @Test
    fun reservesMoreSpaceForMeasuredAxisLabels() {
        val spec = sampleSpec()
        val baseline =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 1000.0),
                                yRange = ClosedRangeD(0.0, 2000.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        val measured =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 1000.0),
                                yRange = ClosedRangeD(0.0, 2000.0),
                            ),
                        textMeasurements =
                            ChartTextMeasurementInput(
                                tickLabelMetrics =
                                    mapOf(
                                        "0" to m(8.0, 12.0),
                                        "500" to m(28.0, 12.0),
                                        "1000" to m(60.0, 12.0),
                                        "2000" to m(60.0, 12.0),
                                    ),
                                fallbackTickLabelMetrics = m(12.0, 12.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )

        assertTrue(measured.layout.plotBounds.left > baseline.layout.plotBounds.left)
        assertTrue(measured.layout.xTicks.first().position.y > measured.layout.plotBounds.bottom)
    }

    @Test
    fun wrapsLegendEntriesIntoMultipleRowsWhenMeasuredWidthsOverflow() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        sampleSeries("first", "Very long legend label 1"),
                        sampleSeries("second", "Very long legend label 2"),
                        sampleSeries("third", "Very long legend label 3"),
                    ),
                legend = LegendSpec(enabled = true),
            )

        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(260.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                        textMeasurements =
                            ChartTextMeasurementInput(
                                legendLabelMetrics =
                                    mapOf(
                                        "Very long legend label 1" to m(120.0, 14.0),
                                        "Very long legend label 2" to m(120.0, 14.0),
                                        "Very long legend label 3" to m(120.0, 14.0),
                                    ),
                                fallbackLegendLabelMetrics = m(120.0, 14.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )

        assertEquals(3, plan.layout.legendEntries.size)
        assertTrue(plan.layout.legendEntries.map { it.labelPosition.y }.distinct().size > 1)
        assertTrue((plan.layout.reservedRegions.legendBounds?.height ?: 0.0) > 14.0)
    }

    @Test
    fun usesCustomAxisFormatterForTickLabels() {
        val spec =
            sampleSpec().copy(
                xAxis =
                    AxisSpec(
                        labelFormatter = { value -> "M${value.toInt() + 1}" },
                    ),
                yAxis =
                    AxisSpec(
                        labelFormatter = { value -> "\$${value.toInt()}k" },
                    ),
            )

        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 6.0),
                                yRange = ClosedRangeD(0.0, 24.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )

        assertTrue(plan.layout.xTicks.all { it.label.startsWith("M") })
        assertTrue(plan.layout.yTicks.all { it.label.startsWith("$") && it.label.endsWith("k") })
    }

    @Test
    fun xTickValuesIncreaseWithDomainPosition() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-5.0, 5.0),
                                yRange = ClosedRangeD(-12.0, 12.0),
                            ),
                    ),
                spec = LineChartSpec(series = listOf(sampleSeries("s", "S"))),
                selection = null,
            )

        val xs = plan.layout.xTicks.map { it.value }
        assertEquals(xs.sorted(), xs)
        assertTrue(xs.first() <= -5.0)
        assertTrue(xs.last() >= 5.0)
    }

    @Test
    fun yTickListRunsHighToLowForScreenPlacement() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 300.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(-10.0, 30.0),
                            ),
                    ),
                spec = sampleSpec(),
                selection = null,
            )

        val ys = plan.layout.yTicks.map { it.value }
        assertTrue(ys.isNotEmpty())
        assertTrue(ys.first() >= ys.last())
    }

    @Test
    fun emptySeriesStillEmitsAxisTicksAndNoPolylines() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 8.0),
                                yRange = ClosedRangeD(0.0, 16.0),
                            ),
                    ),
                spec = LineChartSpec(series = emptyList()),
                selection = null,
            )

        assertTrue(plan.layout.xTicks.size >= 2)
        assertTrue(plan.layout.yTicks.size >= 2)
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Polyline })
    }

    @Test
    fun singlePointSeries_producesFiniteLayoutAndTicks() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series =
                            listOf(
                                LineSeries(
                                    id = "one",
                                    label = "One",
                                    lineStyle = LineStyle(color = ChartColor.Blue),
                                    points = listOf(ChartPoint(5.0, 5.0)),
                                ),
                            ),
                    ),
                selection = null,
            )
        assertTrue(plan.layout.plotBounds.left.isFinite())
        assertTrue(plan.layout.xTicks.isNotEmpty())
        assertTrue(plan.layout.yTicks.isNotEmpty())
    }

    @Test
    fun lineRenderSpecDecimatesPolylineInPlanner() {
        val pts = (0..399).map { ChartPoint(it.toDouble(), sin(it.toDouble())) }
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "dense",
                            label = "Dense",
                            lineStyle = LineStyle(color = ChartColor.Blue),
                            points = pts,
                        ),
                    ),
                lineRender =
                    LineRenderSpec(
                        maxPointsPerSeries = 48,
                        decimation = LineDecimationStrategy.Uniform,
                        viewportContinuityPadding = 0,
                    ),
                xAxis = AxisSpec(showGridLines = false, tickCount = 4),
                yAxis = AxisSpec(showGridLines = false, tickCount = 4),
            )
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 260.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 399.0),
                                yRange = ClosedRangeD(-1.2, 1.2),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        val poly = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>().singleOrNull()
        kotlin.test.assertNotNull(poly)
        assertTrue(poly.points.size <= 48)
    }

    @Test
    fun barSegmentsOmittedWhenBarDoesNotIntersectPlotBounds() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "far",
                            label = "Far",
                            style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                            entries = listOf(BarEntry(500.0, 4.0)),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 8.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertTrue(plan.barSegments.isEmpty())
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Rect })
    }

    @Test
    fun xTickValuesHaveUniformStepForNiceRange() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 100.0),
                                yRange = ClosedRangeD(0.0, 50.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series = listOf(sampleSeries("s", "S")),
                        xAxis = AxisSpec(tickCount = 6),
                    ),
                selection = null,
            )

        val xs = plan.layout.xTicks.map { it.value }
        assertTrue(xs.size >= 2)
        val deltas = xs.zipWithNext { a, b -> b - a }
        val step = deltas.first()
        assertTrue(deltas.all { abs(it - step) < 1e-9 })
    }

    @Test
    fun xTickValuesRemainWithinViewportDomain() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(3.0, 17.0),
                                yRange = ClosedRangeD(0.0, 50.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series = listOf(sampleSeries("s", "S")),
                        xAxis = AxisSpec(tickCount = 6, showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                selection = null,
            )
        val lo = 3.0
        val hi = 17.0
        plan.layout.xTicks.forEach { t ->
            assertTrue(t.value + 1e-6 >= lo && t.value - 1e-6 <= hi)
        }
    }

    @Test
    fun emptyBarGroupsEmitTicksAndNoPlotDrawCommands() {
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 8.0),
                                yRange = ClosedRangeD(0.0, 16.0),
                            ),
                    ),
                spec =
                    BarChartSpec(
                        groups = emptyList(),
                        xAxis = AxisSpec(showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                selection = null,
            )

        assertTrue(plan.layout.xTicks.size >= 2)
        assertTrue(plan.layout.yTicks.size >= 2)
        assertTrue(plan.layout.plotDrawCommands.isEmpty())
    }

    @Test
    fun barChartSelectionAddsStrokeAlongTopOfSelectedBarOnly() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "g1",
                            label = "Group 1",
                            style = BarStyle(color = ChartColor.Green, barWidth = 24.0),
                            entries =
                                listOf(
                                    BarEntry(1.0, 5.0),
                                    BarEntry(3.0, 8.0),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val selection =
            ChartSelection(
                target = HighlightTarget(seriesId = "g1", pointIndex = 1, point = XyPoint(3.0, 8.0)),
                pixelOffset = OffsetD(0.0, 0.0),
            )

        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 6.0),
                                yRange = ClosedRangeD(0.0, 12.0),
                            ),
                    ),
                spec = spec,
                selection = selection,
            )

        val rects = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Rect>()
        val lines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertEquals(2, rects.size)
        assertEquals(1, lines.size)
        assertEquals(spec.selectionStyle.lineColor, lines.single().color)
        val top = lines.single().start.y
        assertEquals(top, lines.single().end.y, 1e-9)
        assertTrue(lines.single().start.x < lines.single().end.x)
    }

    @Test
    fun barChartNoSelectionStrokeWhenSeriesOrXDoesNotMatch() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "g1",
                            label = "Group 1",
                            style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                            entries = listOf(BarEntry(1.0, 5.0)),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val wrongSeries =
            ChartSelection(
                target = HighlightTarget(seriesId = "other", pointIndex = 0, point = XyPoint(1.0, 5.0)),
                pixelOffset = OffsetD(0.0, 0.0),
            )
        val wrongIndex =
            ChartSelection(
                target = HighlightTarget(seriesId = "g1", pointIndex = 3, point = XyPoint(1.0, 5.0)),
                pixelOffset = OffsetD(0.0, 0.0),
            )

        val input =
            ChartLayoutInput(
                canvasSize = SizeD(300.0, 200.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 6.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
            )

        val noMatchSeries = ChartPlanner.planBarChart(input, spec, wrongSeries)
        val noMatchIndex = ChartPlanner.planBarChart(input, spec, wrongIndex)

        assertTrue(noMatchSeries.layout.plotDrawCommands.none { it is DrawCommand.Line })
        assertTrue(noMatchIndex.layout.plotDrawCommands.none { it is DrawCommand.Line })
    }

    @Test
    fun barIndependentLayoutDoesNotHighlightWhenDomainXDoesNotMatchEntry() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "g1",
                            label = "Group 1",
                            style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                            entries = listOf(BarEntry(1.0, 5.0)),
                        ),
                    ),
                barLayout = BarLayoutMode.Independent,
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val inconsistent =
            ChartSelection(
                target = HighlightTarget(seriesId = "g1", pointIndex = 0, point = XyPoint(2.0, 5.0)),
                pixelOffset = OffsetD(0.0, 0.0),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 6.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = inconsistent,
            )
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Line })
    }

    @Test
    fun stackedBarSelectionStrokeMatchesSegmentByIndex() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "a",
                            label = "A",
                            style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                            entries = listOf(BarEntry(1.0, 3.0)),
                        ),
                        BarGroup(
                            id = "b",
                            label = "B",
                            style = BarStyle(color = ChartColor.Red, barWidth = 20.0),
                            entries = listOf(BarEntry(1.0, 2.0)),
                        ),
                    ),
                barLayout = BarLayoutMode.Stacked,
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val selection =
            ChartSelection(
                target = HighlightTarget(seriesId = "a", pointIndex = 0, point = XyPoint(1.0, 3.0)),
                pixelOffset = OffsetD(0.0, 0.0),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 4.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = selection,
            )
        assertEquals(2, plan.barSegments.size)
        val lines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertEquals(1, lines.size)
        val bottomSeg = plan.barSegments.first { it.groupId == "a" }
        val accentY = lines.single().start.y
        assertEquals(bottomSeg.bounds.top, accentY, 1e-6)
    }

    @Test
    fun clusteredBarSegmentsDoNotShareHorizontalBounds() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = "a",
                            label = "A",
                            style = BarStyle(color = ChartColor.Green, barWidth = 16.0),
                            entries = listOf(BarEntry(2.0, 4.0)),
                        ),
                        BarGroup(
                            id = "b",
                            label = "B",
                            style = BarStyle(color = ChartColor.Red, barWidth = 16.0),
                            entries = listOf(BarEntry(2.0, 6.0)),
                        ),
                    ),
                barLayout = BarLayoutMode.Clustered,
                clusterBarGapPx = 6.0,
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 6.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertEquals(2, plan.barSegments.size)
        val a = plan.barSegments.first { it.groupId == "a" }
        val b = plan.barSegments.first { it.groupId == "b" }
        assertTrue(abs(a.bounds.left - b.bounds.left) > 1.0)
    }

    @Test
    fun selectionAddsVerticalCrosshairAndHighlightPoint() {
        val spec =
            sampleSpec().copy(
                series =
                    listOf(
                        sampleSeries("series", "Series").copy(pointStyle = PointStyle(visible = false)),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val selection =
            ChartSelection(
                target = HighlightTarget(seriesId = "series", pointIndex = 1, point = XyPoint(3.0, 8.0)),
                pixelOffset = OffsetD(180.0, 95.0),
            )

        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                    ),
                spec = spec,
                selection = selection,
            )

        val lines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertEquals(1, lines.size)
        assertEquals(1, circles.size)
        assertEquals(180.0, lines.single().start.x, 1e-9)
        assertEquals(180.0, lines.single().end.x, 1e-9)
        assertEquals(OffsetD(180.0, 95.0), circles.single().center)
    }

    @Test
    fun hidesSomeXTickLabelsWhenHorizontalSpaceIsInsufficient() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(140.0, 120.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 4.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                        textMeasurements =
                            ChartTextMeasurementInput(
                                tickLabelMetrics =
                                    mapOf(
                                        "0" to m(72.0, 12.0),
                                        "1" to m(72.0, 12.0),
                                        "2" to m(72.0, 12.0),
                                        "3" to m(72.0, 12.0),
                                        "4" to m(72.0, 12.0),
                                    ),
                                fallbackTickLabelMetrics = m(72.0, 12.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series = listOf(sampleSeries("s", "S")),
                        xAxis = AxisSpec(tickCount = 6, showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                selection = null,
            )
        val n = plan.layout.xTicks.size
        assertTrue(n >= 2)
        assertTrue(plan.layout.xTicks.count { !it.showLabel } > 0)
    }

    @Test
    fun largerLabelLayoutScaleSuppressesMoreXTickLabelsWhenSpaceIsTight() {
        val inputBase =
            ChartLayoutInput(
                canvasSize = SizeD(140.0, 120.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 4.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics =
                            mapOf(
                                "0" to m(72.0, 12.0),
                                "1" to m(72.0, 12.0),
                                "2" to m(72.0, 12.0),
                                "3" to m(72.0, 12.0),
                                "4" to m(72.0, 12.0),
                            ),
                        fallbackTickLabelMetrics = m(72.0, 12.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis = AxisSpec(tickCount = 6, showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val lowScale =
            ChartPlanner.planLineChart(
                input = inputBase.copy(labelLayoutScale = 1.0),
                spec = spec,
                selection = null,
            )
        val highScale =
            ChartPlanner.planLineChart(
                input = inputBase.copy(labelLayoutScale = 2.0),
                spec = spec,
                selection = null,
            )
        val hiddenLow = lowScale.layout.xTicks.count { !it.showLabel }
        val hiddenHigh = highScale.layout.xTicks.count { !it.showLabel }
        assertTrue(hiddenHigh >= hiddenLow)
        assertTrue(hiddenHigh > 0)
    }

    @Test
    fun staggerOverlapStrategyPlacesLabelsOnTwoRowsWhenColliding() {
        val metrics = (0..7).associate { it.toString() to m(52.0, 13.0) }
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(280.0, 220.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 7.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = metrics,
                        fallbackTickLabelMetrics = m(52.0, 13.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis =
                    AxisSpec(
                        tickCount = 8,
                        tickDensity = AxisTickDensity.MaxTicks(8),
                        showGridLines = false,
                        xTickLabelOverlapStrategy = XTickLabelOverlapStrategy.StaggerAdjacent,
                    ),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan = ChartPlanner.planLineChart(input = input, spec = spec, selection = null)
        val visible = plan.layout.xTicks.filter { it.showLabel }
        assertTrue(visible.size >= 4)
        val rowYs = visible.map { it.position.y }.distinct().sorted()
        assertTrue(rowYs.size >= 2)
        assertTrue(rowYs.last() > rowYs.first() + 5.0)
    }

    @Test
    fun hideOverlapping_manyNarrowLabelsAtHighDensity_keepsMajorityVisible() {
        val metrics = (0..15).associate { it.toString() to m(10.0, 12.0) }
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(420.0, 220.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 15.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = metrics,
                        fallbackTickLabelMetrics = m(10.0, 12.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis =
                    AxisSpec(
                        tickCount = 16,
                        tickDensity = AxisTickDensity.MaxTicks(16),
                        showGridLines = false,
                        xTickLabelOverlapStrategy = XTickLabelOverlapStrategy.HideOverlapping,
                    ),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan = ChartPlanner.planLineChart(input = input, spec = spec, selection = null)
        val n = plan.layout.xTicks.size
        assertTrue(n >= 8)
        val visible = plan.layout.xTicks.count { it.showLabel }
        assertTrue(visible >= n / 2)
    }

    @Test
    fun staggerAdjacent_whenBothRowsCollide_hidesSomeLabels() {
        val metrics = (0..7).associate { it.toString() to m(92.0, 13.0) }
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(160.0, 220.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 7.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = metrics,
                        fallbackTickLabelMetrics = m(92.0, 13.0),
                    ),
            )
        val spec =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis =
                    AxisSpec(
                        tickCount = 8,
                        tickDensity = AxisTickDensity.MaxTicks(8),
                        showGridLines = false,
                        xTickLabelOverlapStrategy = XTickLabelOverlapStrategy.StaggerAdjacent,
                    ),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan = ChartPlanner.planLineChart(input = input, spec = spec, selection = null)
        assertTrue(plan.layout.xTicks.any { !it.showLabel })
    }

    @Test
    fun minVisibleXTickLabels_softTarget_reEnablesAfterHideOverlapping() {
        val metrics = (0..7).associate { it.toString() to m(84.0, 12.0) }
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(380.0, 220.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 7.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = metrics,
                        fallbackTickLabelMetrics = m(84.0, 12.0),
                    ),
            )
        val specWithoutMin =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis =
                    AxisSpec(
                        tickCount = 8,
                        tickDensity = AxisTickDensity.MaxTicks(8),
                        showGridLines = false,
                        xTickLabelOverlapStrategy = XTickLabelOverlapStrategy.HideOverlapping,
                    ),
                yAxis = AxisSpec(showGridLines = false),
            )
        val specWithMin =
            specWithoutMin.copy(
                xAxis = specWithoutMin.xAxis.copy(minVisibleXTickLabels = 3),
            )
        val low = ChartPlanner.planLineChart(input = input, spec = specWithoutMin, selection = null)
        val high = ChartPlanner.planLineChart(input = input, spec = specWithMin, selection = null)
        val lowVisible = low.layout.xTicks.count { it.showLabel }
        val highVisible = high.layout.xTicks.count { it.showLabel }
        assertTrue(highVisible >= 3)
        assertTrue(highVisible >= lowVisible)
    }

    @Test
    fun raiseTickDensityCapFromMeasuredLabelWidth_increasesTickCountVersusDefault() {
        val metrics = (0..12).associate { it.toString() to m(6.0, 12.0) }
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 220.0),
                viewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 12.0),
                        yRange = ClosedRangeD(0.0, 10.0),
                    ),
                textMeasurements =
                    ChartTextMeasurementInput(
                        tickLabelMetrics = metrics,
                        fallbackTickLabelMetrics = m(6.0, 12.0),
                    ),
            )
        val base =
            LineChartSpec(
                series = listOf(sampleSeries("s", "S")),
                xAxis =
                    AxisSpec(
                        tickCount = 12,
                        tickDensity = AxisTickDensity.Auto,
                        defaultDensitySpacingPx = 48.0,
                        showGridLines = false,
                    ),
                yAxis = AxisSpec(showGridLines = false),
            )
        val raised =
            base.copy(
                xAxis = base.xAxis.copy(raiseTickDensityCapFromMeasuredLabelWidth = true),
            )
        val planBase = ChartPlanner.planLineChart(input = input, spec = base, selection = null)
        val planRaised = ChartPlanner.planLineChart(input = input, spec = raised, selection = null)
        assertTrue(planRaised.layout.xTicks.size > planBase.layout.xTicks.size)
    }

    @Test
    fun legendTruncatesLabelsWhenLegendStyleMaxCharsSet() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(280.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series =
                            listOf(
                                LineSeries(
                                    id = "a",
                                    label = "VeryLongSeriesName",
                                    lineStyle = LineStyle(color = ChartColor.Blue),
                                    points = listOf(ChartPoint(0.0, 1.0), ChartPoint(1.0, 2.0)),
                                ),
                            ),
                        xAxis = AxisSpec(showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                        legend = LegendSpec(style = LegendStyle(labelMaxChars = 8)),
                    ),
                selection = null,
            )
        val entry = plan.layout.legendEntries.single()
        assertTrue(entry.label.endsWith("…"))
        assertTrue(entry.label.length <= 8)
    }

    @Test
    fun hidesXTickLabelsThatOverflowCanvasHorizontally() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(120.0, 160.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 2.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                        textMeasurements =
                            ChartTextMeasurementInput(
                                tickLabelMetrics =
                                    mapOf(
                                        "0" to m(100.0, 12.0),
                                        "1" to m(100.0, 12.0),
                                        "2" to m(100.0, 12.0),
                                    ),
                                fallbackTickLabelMetrics = m(100.0, 12.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series = listOf(sampleSeries("s", "S")),
                        xAxis = AxisSpec(tickCount = 4, showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                selection = null,
            )
        assertTrue(plan.layout.xTicks.any { !it.showLabel })
    }

    @Test
    fun xAxisBottomInsetGrowsWithMeasuredDescenderTail() {
        fun planWithMetrics(metrics: TextLayoutMetrics) =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(280.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                        textMeasurements =
                            ChartTextMeasurementInput(
                                tickLabelMetrics =
                                    mapOf(
                                        "0" to metrics,
                                        "3" to metrics,
                                        "6" to metrics,
                                        "9" to metrics,
                                    ),
                                fallbackTickLabelMetrics = metrics,
                            ),
                    ),
                spec =
                    sampleSpec().copy(
                        yAxis = AxisSpec(showGridLines = false),
                        contentPadding = sampleSpec().contentPadding.copy(bottom = 0.0),
                    ),
                selection = null,
            )
        val shallowDescender =
            TextLayoutMetrics(
                width = 24.0,
                height = 20.0,
                firstBaselineFromTop = 14.0,
                lastBaselineFromTop = 18.0,
            )
        val deepDescender =
            TextLayoutMetrics(
                width = 24.0,
                height = 20.0,
                firstBaselineFromTop = 14.0,
                lastBaselineFromTop = 14.0,
            )
        val shallowBottom = planWithMetrics(shallowDescender).layout.reservedRegions.contentInsets.bottom
        val deepBottom = planWithMetrics(deepDescender).layout.reservedRegions.contentInsets.bottom
        assertTrue(deepBottom > shallowBottom)
    }

    @Test
    fun appliesGroupedLineAndPointStylesToDrawCommands() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "styled",
                            label = "Styled",
                            lineStyle = LineStyle(color = ChartColor.Green, strokeWidth = 4.0),
                            pointStyle = PointStyle(visible = true, radius = 6.0, color = ChartColor.Red),
                            points =
                                listOf(
                                    ChartPoint(0.0, 1.0),
                                    ChartPoint(3.0, 8.0),
                                    ChartPoint(9.0, 21.0),
                                ),
                        ),
                    ),
            )

        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )

        val polyline = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>().single()
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()

        assertEquals(ChartColor.Green, polyline.color)
        assertEquals(4.0, polyline.strokeWidth)
        assertTrue(circles.all { it.radius == 6.0 && it.color == ChartColor.Red })
    }

    @Test
    fun linePolylineVerticesLieInPlotBoundsWhenSeriesExceedsViewportY() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "spike",
                            label = "Spike",
                            lineStyle = LineStyle(color = ChartColor.Blue),
                            pointStyle = PointStyle(visible = true, radius = 5.0, color = ChartColor.Red),
                            points =
                                listOf(
                                    ChartPoint(0.0, 0.0),
                                    ChartPoint(5.0, 1000.0),
                                    ChartPoint(10.0, 0.0),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        val pb = plan.layout.plotBounds
        val polylines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>()
        assertTrue(polylines.isNotEmpty())
        for (poly in polylines) {
            for (pt in poly.points) {
                assertTrue(pt.x >= pb.left - 1e-6 && pt.x <= pb.right + 1e-6)
                assertTrue(pt.y >= pb.top - 1e-6 && pt.y <= pb.bottom + 1e-6)
            }
        }
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertEquals(2, circles.size)
        assertTrue(circles.all { pb.intersectsDisc(it.center, it.radius) })
    }

    @Test
    fun scatterChart_decimation_capsCircleCommands() {
        val pts =
            (0 until 4000).map { i ->
                ChartPoint(x = (i % 160) * 0.05, y = (i / 160) * 0.02)
            }
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "dense",
                            label = "Dense",
                            points = pts,
                            pointStyle = PointStyle(visible = true, radius = 3.0, color = ChartColor.Blue),
                        ),
                    ),
                scatterRender = ScatterRenderSpec(maxPointsPerSeries = 150, decimation = LineDecimationStrategy.Uniform),
                xAxis = AxisSpec(showGridLines = false, tickCount = 4),
                yAxis = AxisSpec(showGridLines = false, tickCount = 4),
            )
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 260.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertTrue(circles.isNotEmpty())
        assertTrue(circles.size <= 150)
        assertTrue(circles.all { plan.layout.plotBounds.intersectsDisc(it.center, it.radius) })
    }

    @Test
    fun scatterChartEmitsCirclesOnlyWithExpectedCountAndStyles() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "a",
                            label = "A",
                            points = listOf(ChartPoint(0.0, 0.0), ChartPoint(5.0, 5.0)),
                            pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Red),
                        ),
                        ScatterSeries(
                            id = "b",
                            label = "B",
                            points = listOf(ChartPoint(2.0, 8.0), ChartPoint(8.0, 2.0), ChartPoint(9.0, 9.0)),
                            pointStyle = PointStyle(visible = true, radius = 6.0, color = ChartColor.Green),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false, tickCount = 4),
                yAxis = AxisSpec(showGridLines = false, tickCount = 4),
            )
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(400.0, 260.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertTrue(plan.layout.plotBounds.width > 1.0 && plan.layout.plotBounds.height > 1.0)
        assertTrue(plan.layout.xTicks.size >= 2)
        assertTrue(plan.layout.yTicks.size >= 2)
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Polyline })
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertEquals(5, circles.size)
        assertEquals(2, circles.count { it.radius == 4.0 && it.color == ChartColor.Red })
        assertEquals(3, circles.count { it.radius == 6.0 && it.color == ChartColor.Green })
        assertTrue(circles.all { plan.layout.plotBounds.intersectsDisc(it.center, it.radius) })
    }

    @Test
    fun scatterChartPlotsMarkerWhenDiscCrossesPlotEdgeEvenIfCenterOutside() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "edge",
                            label = "Edge",
                            points = listOf(ChartPoint(10.2, 5.0)),
                            pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Red),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false, tickCount = 3),
                yAxis = AxisSpec(showGridLines = false, tickCount = 3),
            )
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(220.0, 220.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        val circles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertEquals(1, circles.size)
        val c = circles.first()
        assertTrue(plan.layout.plotBounds.intersectsDisc(c.center, c.radius))
        assertTrue(!plan.layout.plotBounds.contains(c.center))
    }

    @Test
    fun scatterChartEmptySeriesStillEmitsTicksAndNoPlotCircles() {
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 8.0),
                                yRange = ClosedRangeD(0.0, 16.0),
                            ),
                    ),
                spec = ScatterChartSpec(series = emptyList()),
                selection = null,
            )
        assertTrue(plan.layout.xTicks.size >= 2)
        assertTrue(plan.layout.yTicks.size >= 2)
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Circle })
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Polyline })
    }

    @Test
    fun scatterChartHidesPointsWhenPointStyleNotVisible() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "v",
                            label = "V",
                            points = listOf(ChartPoint(1.0, 1.0)),
                            pointStyle = PointStyle(visible = false),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 240.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 6.0),
                                yRange = ClosedRangeD(0.0, 6.0),
                            ),
                    ),
                spec = spec,
                selection = null,
            )
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Circle })
    }

    @Test
    fun lineChart_negativeXDomain_degenerateYSpan_producesFiniteLayoutAndOrderedTicks() {
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 220.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-12.0, -4.0),
                                yRange = ClosedRangeD(7.0, 7.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series =
                            listOf(
                                LineSeries(
                                    id = "s",
                                    label = "S",
                                    lineStyle = LineStyle(color = ChartColor.Blue),
                                    points =
                                        listOf(
                                            ChartPoint(-10.0, 7.0),
                                            ChartPoint(-6.0, 7.0),
                                        ),
                                ),
                            ),
                        xAxis = AxisSpec(showGridLines = false, tickCount = 5),
                        yAxis = AxisSpec(showGridLines = false, tickCount = 4),
                    ),
                selection = null,
            )
        val xs = plan.layout.xTicks.map { it.value }
        assertTrue(xs.isNotEmpty())
        assertEquals(xs.sorted(), xs)
        assertTrue(plan.layout.yTicks.isNotEmpty())
        val b = plan.layout.plotBounds
        assertTrue(b.left.isFinite() && b.top.isFinite() && b.width.isFinite() && b.height.isFinite())
    }

    @Test
    fun barChart_negativeXDomain_degenerateYSpan_producesOrderedTicksAndFiniteBounds() {
        val plan =
            ChartPlanner.planBarChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-5.0, 5.0),
                                yRange = ClosedRangeD(3.0, 3.0),
                            ),
                    ),
                spec =
                    BarChartSpec(
                        groups =
                            listOf(
                                BarGroup(
                                    id = "g",
                                    label = "G",
                                    style = BarStyle(color = ChartColor.Green, barWidth = 20.0),
                                    entries = listOf(BarEntry(-2.0, 3.0)),
                                ),
                            ),
                        xAxis = AxisSpec(showGridLines = false),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                selection = null,
            )
        val xs = plan.layout.xTicks.map { it.value }
        assertEquals(xs.sorted(), xs)
        assertTrue(plan.layout.yTicks.isNotEmpty())
        val b = plan.layout.plotBounds
        assertTrue(b.left.isFinite() && b.top.isFinite() && b.width.isFinite() && b.height.isFinite())
    }

    @Test
    fun scatterChart_negativeXDomain_degenerateYSpan_producesFiniteLayoutAndOrderedTicks() {
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(320.0, 220.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(-12.0, -4.0),
                                yRange = ClosedRangeD(7.0, 7.0),
                            ),
                    ),
                spec =
                    ScatterChartSpec(
                        series =
                            listOf(
                                ScatterSeries(
                                    id = "s",
                                    label = "S",
                                    points =
                                        listOf(
                                            ChartPoint(-10.0, 7.0),
                                            ChartPoint(-6.0, 7.0),
                                        ),
                                ),
                            ),
                        xAxis = AxisSpec(showGridLines = false, tickCount = 5),
                        yAxis = AxisSpec(showGridLines = false, tickCount = 4),
                    ),
                selection = null,
            )
        val xs = plan.layout.xTicks.map { it.value }
        assertTrue(xs.isNotEmpty())
        assertEquals(xs.sorted(), xs)
        assertTrue(plan.layout.yTicks.isNotEmpty())
        assertTrue(plan.layout.plotDrawCommands.any { it is DrawCommand.Circle })
        val b = plan.layout.plotBounds
        assertTrue(b.left.isFinite() && b.top.isFinite() && b.width.isFinite() && b.height.isFinite())
    }

    @Test
    fun scatterChartSelectionAddsCrosshairCommands() {
        val spec =
            ScatterChartSpec(
                series =
                    listOf(
                        ScatterSeries(
                            id = "s",
                            label = "S",
                            points = listOf(ChartPoint(3.0, 4.0)),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val selection =
            ChartSelection(
                target =
                    HighlightTarget(
                        seriesId = "s",
                        pointIndex = 0,
                        point = XyPoint(3.0, 4.0),
                    ),
                pixelOffset = OffsetD(155.0, 80.0),
            )
        val plan =
            ChartPlanner.planScatterChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 10.0),
                                yRange = ClosedRangeD(0.0, 10.0),
                            ),
                    ),
                spec = spec,
                selection = selection,
            )
        val lines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertTrue(lines.isNotEmpty())
        val hiCircles = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Circle>()
        assertTrue(hiCircles.any { it.radius == spec.selectionStyle.pointRadius })
    }

    @Test
    fun gridLinesHonorGridLineStyleStrokeWidthAndDash() {
        val dash = StrokeStyle(dashIntervals = listOf(4.0, 4.0))
        val plan =
            ChartPlanner.planLineChart(
                input =
                    ChartLayoutInput(
                        canvasSize = SizeD(300.0, 200.0),
                        viewport =
                            ChartViewport(
                                xRange = ClosedRangeD(0.0, 9.0),
                                yRange = ClosedRangeD(0.0, 23.0),
                            ),
                    ),
                spec =
                    LineChartSpec(
                        series = listOf(sampleSeries("s", "S")),
                        grid =
                            GridSpec(
                                lineStyle =
                                    GridLineStyle(
                                        color = ChartColor.Gray,
                                        strokeWidth = 2.5,
                                        strokeStyle = dash,
                                    ),
                            ),
                    ),
                selection = null,
            )
        val gridLines =
            plan.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Line>()
                .filter { it.strokeStyle != null }
        assertTrue(gridLines.isNotEmpty(), "expected dashed grid lines in plot commands")
        assertTrue(gridLines.all { it.strokeWidth == 2.5 })
        assertTrue(
            gridLines.all { it.strokeStyle?.dashIntervals == dash.dashIntervals },
        )
    }

    private fun sampleSpec(): LineChartSpec =
        LineChartSpec(
            series = listOf(sampleSeries("series", "Series")),
        )

    private fun m(
        w: Double,
        h: Double,
    ): TextLayoutMetrics = TextLayoutMetrics.uniformBox(SizeD(w, h))

    private fun sampleSeries(
        id: String,
        label: String,
    ): LineSeries =
        LineSeries(
            id = id,
            label = label,
            lineStyle = LineStyle(color = ChartColor.Blue),
            points =
                listOf(
                    ChartPoint(0.0, 1.0),
                    ChartPoint(3.0, 8.0),
                    ChartPoint(9.0, 21.0),
                ),
        )
}
