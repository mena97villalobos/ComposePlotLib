package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AreaFillMode
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.MarkerShape
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.ReferenceLine
import com.mena97villalobos.composeplotlib.core.spec.ReferenceOrientation
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Planner tests for the 1.1.0 chart-expansion features (A1 area fill, A2 reference lines, A3/A5 markers, A4 gauge, A6 candlestick). */
class ChartExpansionPlannerTest {
    private fun cartesianInput(
        xMax: Double = 4.0,
        yMax: Double = 10.0,
    ) = ChartLayoutInput(
        canvasSize = SizeD(320.0, 200.0),
        viewport = ChartViewport(xRange = ClosedRangeD(0.0, xMax), yRange = ClosedRangeD(0.0, yMax)),
    )

    private fun radialInput() =
        ChartLayoutInput(
            canvasSize = SizeD(200.0, 200.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0)),
        )

    private fun line(vararg ys: Double): LineSeries = LineSeries(id = "s", label = "s", points = ys.mapIndexed { i, y -> ChartPoint(i.toDouble(), y) })

    // ---- A1: area / threshold fill ----

    @Test
    fun areaFill_disabledByDefault_emitsNoFillPath() {
        val plan = ChartPlanner.planLineChart(cartesianInput(), LineChartSpec(series = listOf(line(1.0, 5.0, 3.0))), selection = null)
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Path }, "no fill path when areaFill is null")
    }

    @Test
    fun areaFill_solid_emitsFillPath() {
        val spec =
            LineChartSpec(
                series = listOf(line(1.0, 5.0, 3.0).copy(lineStyle = LineStyle(areaFill = ChartColor.Blue))),
            )
        val paths =
            ChartPlanner
                .planLineChart(cartesianInput(), spec, selection = null)
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Path>()
        assertEquals(1, paths.size)
        assertTrue(paths.single().style is PathDrawStyle.Fill)
    }

    @Test
    fun areaFill_gradient_emitsVerticalGradientFill() {
        val spec =
            LineChartSpec(
                series = listOf(line(1.0, 5.0, 3.0).copy(lineStyle = LineStyle(areaFill = ChartColor.Blue, areaFillEnd = ChartColor.White))),
            )
        val path =
            ChartPlanner
                .planLineChart(cartesianInput(), spec, selection = null)
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Path>()
                .single()
        assertTrue(path.style is PathDrawStyle.VerticalGradientFill)
    }

    @Test
    fun areaFill_belowThreshold_wrapsFillInClipRect() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        line(1.0, 5.0, 3.0).copy(
                            lineStyle = LineStyle(areaFill = ChartColor.Blue, areaFillMode = AreaFillMode.BelowThreshold(4.0)),
                        ),
                    ),
            )
        val commands = ChartPlanner.planLineChart(cartesianInput(), spec, selection = null).layout.plotDrawCommands
        val clip = commands.filterIsInstance<DrawCommand.ClipRect>().singleOrNull()
        assertTrue(clip != null && clip.children.any { it is DrawCommand.Path }, "below-threshold fill is clipped")
        assertTrue(commands.none { it is DrawCommand.Path }, "the fill path lives inside the clip, not at top level")
    }

    // ---- A2: reference lines ----

    @Test
    fun referenceLine_horizontal_emitsSpanningLineWithDash() {
        val refColor = ChartColor(0xFF112233)
        val spec =
            LineChartSpec(
                series = listOf(line(1.0, 9.0)),
                referenceLines =
                    listOf(ReferenceLine(orientation = ReferenceOrientation.Horizontal, value = 5.0, color = refColor, dashIntervals = listOf(6.0, 4.0))),
            )
        val plan = ChartPlanner.planLineChart(cartesianInput(), spec, selection = null)
        val ref =
            plan.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Line>()
                .single { it.color == refColor }
        assertEquals(ref.start.y, ref.end.y, "horizontal reference line is level")
        assertEquals(plan.layout.plotBounds.left, ref.start.x)
        assertEquals(plan.layout.plotBounds.right, ref.end.x)
        assertTrue(ref.strokeStyle?.dashIntervals?.isNotEmpty() == true, "dashed when dashIntervals provided")
    }

    // ---- A3 / A5: marker shapes + per-point overrides ----

    private fun scatter(
        points: List<ChartPoint>,
        style: PointStyle,
    ) = ScatterChartSpec(series = listOf(ScatterSeries(id = "p", label = "p", points = points, pointStyle = style)))

    @Test
    fun markerShape_square_emitsRectInsteadOfCircle() {
        val spec = scatter(listOf(ChartPoint(1.0, 5.0)), PointStyle(shape = MarkerShape.Square))
        val cmds = ChartPlanner.planScatterChart(cartesianInput(), spec, selection = null).layout.plotDrawCommands
        assertTrue(cmds.any { it is DrawCommand.Rect } && cmds.none { it is DrawCommand.Circle })
    }

    @Test
    fun markerShape_triangleAndDiamond_emitFilledPaths() {
        for (shape in listOf(MarkerShape.Triangle, MarkerShape.Diamond)) {
            val spec = scatter(listOf(ChartPoint(1.0, 5.0)), PointStyle(shape = shape))
            val cmds = ChartPlanner.planScatterChart(cartesianInput(), spec, selection = null).layout.plotDrawCommands
            assertTrue(cmds.any { it is DrawCommand.Path && it.style is PathDrawStyle.Fill }, "$shape -> filled path")
        }
    }

    @Test
    fun perPointColorAndRadius_overrideSeriesDefaults() {
        val override = ChartColor(0xFF00FF00)
        val spec = scatter(listOf(ChartPoint(1.0, 5.0, markerColor = override, markerRadius = 17.0)), PointStyle(radius = 3.0))
        val circle =
            ChartPlanner
                .planScatterChart(cartesianInput(), spec, selection = null)
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Circle>()
                .single()
        assertEquals(override, circle.color)
        assertEquals(17.0, circle.radius)
    }

    // ---- A4: gauge ----

    @Test
    fun gauge_emitsTrackAndProgressArcs_withClampedSweep() {
        val arcs =
            ChartPlanner
                .planGaugeChart(radialInput(), GaugeChartSpec(progress = 0.5, sweepDegrees = 360.0))
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Arc>()
        assertEquals(2, arcs.size, "track + progress")
        assertEquals(360.0, arcs[0].sweepAngleDegrees, "track spans full sweep")
        assertEquals(180.0, arcs[1].sweepAngleDegrees, "progress = sweep * 0.5")
    }

    @Test
    fun gauge_clampsProgressAboveOne() {
        val arcs =
            ChartPlanner
                .planGaugeChart(radialInput(), GaugeChartSpec(progress = 1.5, sweepDegrees = 270.0))
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Arc>()
        assertEquals(270.0, arcs[1].sweepAngleDegrees, "progress clamped to 1.0")
    }

    @Test
    fun gauge_zeroProgress_emitsOnlyTrack() {
        val arcs =
            ChartPlanner
                .planGaugeChart(radialInput(), GaugeChartSpec(progress = 0.0))
                .layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Arc>()
        assertEquals(1, arcs.size, "no progress arc at 0%")
    }

    // ---- A6: candlestick ----

    @Test
    fun candlestick_bullAndBearColoring_andBodyWickCommands() {
        val style =
            com.mena97villalobos.composeplotlib.core.spec
                .CandlestickStyle(bullColor = ChartColor(0xFF00AA00), bearColor = ChartColor(0xFFAA0000))
        val spec =
            CandlestickChartSpec(
                series =
                    listOf(
                        CandlestickSeries(
                            id = "c",
                            label = "c",
                            points =
                                listOf(
                                    OHLCPoint(x = 0.0, open = 2.0, high = 6.0, low = 1.0, close = 5.0), // bull (close > open)
                                    OHLCPoint(x = 1.0, open = 5.0, high = 6.0, low = 2.0, close = 3.0), // bear (close < open)
                                ),
                            style = style,
                        ),
                    ),
            )
        val plan = ChartPlanner.planCandlestickChart(cartesianInput(xMax = 2.0, yMax = 8.0), spec)
        assertEquals(2, plan.candles.size)
        assertTrue(plan.candles[0].isBull && !plan.candles[1].isBull)
        assertEquals(style.bullColor, plan.candles[0].bodyColor)
        assertEquals(style.bearColor, plan.candles[1].bodyColor)
        // each candle contributes a wick (Line) and a body (Rect)
        assertTrue(plan.layout.plotDrawCommands.any { it is DrawCommand.Rect }, "bodies are rects")
        assertTrue(plan.layout.plotDrawCommands.any { it is DrawCommand.Line }, "wicks are lines")
    }

    @Test
    fun candlestick_emptySeriesAllowed_noCandles() {
        val plan = ChartPlanner.planCandlestickChart(cartesianInput(), CandlestickChartSpec(series = emptyList()))
        assertTrue(plan.candles.isEmpty())
        assertNull(plan.selection)
    }
}
