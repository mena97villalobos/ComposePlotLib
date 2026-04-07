package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieSliceLabelsSpec
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PieChartPlannerTest {
    private fun baseInput() =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport =
                ChartViewport(
                    xRange = ClosedRangeD(0.0, 1.0),
                    yRange = ClosedRangeD(0.0, 1.0),
                ),
        )

    private fun collectArcs(commands: List<DrawCommand>): List<DrawCommand.Arc> {
        val out = mutableListOf<DrawCommand.Arc>()

        fun visit(cmd: DrawCommand) {
            when (cmd) {
                is DrawCommand.Arc -> out.add(cmd)
                is DrawCommand.Group -> cmd.children.forEach(::visit)
                is DrawCommand.ClipRect -> cmd.children.forEach(::visit)
                else -> Unit
            }
        }
        commands.forEach(::visit)
        return out
    }

    @Test
    fun pieChart_hasNoAxisTicks_orAxisOverlay() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(1.0, "A", ChartColor.Blue),
                                PieChartSlice(1.0, "B", ChartColor.Green),
                            ),
                    ),
                selection = null,
            )
        assertTrue(plan.layout.xTicks.isEmpty())
        assertTrue(plan.layout.yTicks.isEmpty())
        assertTrue(plan.layout.overlayDrawCommands.isEmpty())
    }

    @Test
    fun nPositiveSlices_emitsNFills() {
        val slices =
            listOf(
                PieChartSlice(3.0, "a", ChartColor.Blue),
                PieChartSlice(2.0, "b", ChartColor.Green),
                PieChartSlice(5.0, "c", ChartColor.Red),
            )
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec = PieChartSpec(slices = slices),
                selection = null,
            )
        val fills = collectArcs(plan.layout.plotDrawCommands).filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(3, fills.size)
        assertTrue(fills.all { it.innerRadius == null })
        assertEquals(108.0, fills[0].sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(72.0, fills[1].sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(180.0, fills[2].sweepAngleDegrees, absoluteTolerance = 1e-6)
    }

    @Test
    fun donut_setsInnerRadiusOnArcs() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(1.0, "a", ChartColor.Blue),
                                PieChartSlice(1.0, "b", ChartColor.Green),
                            ),
                        holeRadiusRatio = 0.4,
                    ),
                selection = null,
            )
        val fills = collectArcs(plan.layout.plotDrawCommands).filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(2, fills.size)
        val inner = fills[0].innerRadius!!
        assertTrue(inner > 0.0)
        assertTrue(fills[1].innerRadius == inner)
        assertTrue(inner < fills[0].radius)
    }

    @Test
    fun emptySlices_emitsNoArcs() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec = PieChartSpec(slices = emptyList()),
                selection = null,
            )
        assertTrue(collectArcs(plan.layout.plotDrawCommands).isEmpty())
    }

    @Test
    fun allZeroOrNegativeValues_emitsNoArcs() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(0.0, "a", ChartColor.Blue),
                                PieChartSlice(-1.0, "b", ChartColor.Green),
                            ),
                    ),
                selection = null,
            )
        assertTrue(collectArcs(plan.layout.plotDrawCommands).isEmpty())
    }

    @Test
    fun skipsZeroValueSlicesForAngles() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(1.0, "a", ChartColor.Blue),
                                PieChartSlice(0.0, "b", ChartColor.Green),
                                PieChartSlice(1.0, "c", ChartColor.Red),
                            ),
                    ),
                selection = null,
            )
        val fills = collectArcs(plan.layout.plotDrawCommands).filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(2, fills.size)
        assertEquals(180.0, fills[0].sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(180.0, fills[1].sweepAngleDegrees, absoluteTolerance = 1e-6)
    }

    @Test
    fun selectionAddsHighlightGroupWhenIdsMatch() {
        val spec =
            PieChartSpec(
                id = "sales",
                slices =
                    listOf(
                        PieChartSlice(1.0, "q1", ChartColor.Blue),
                        PieChartSlice(3.0, "q2", ChartColor.Green),
                    ),
                legend = LegendSpec(enabled = false),
                selectionStyle = SelectionStyle(pointColor = ChartColor.Red),
            )
        val sel =
            ChartSelection(
                target =
                    HighlightTarget(
                        seriesId = "sales",
                        pointIndex = 1,
                        point = XyPoint(0.0, 0.0),
                    ),
                pixelOffset = OffsetD(1.0, 1.0),
            )
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec = spec,
                selection = sel,
            )
        val groups = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Group>()
        assertEquals(1, groups.size)
        assertEquals(0.35, groups[0].alpha, absoluteTolerance = 1e-9)
        val hi = groups[0].children.filterIsInstance<DrawCommand.Arc>().single()
        val fillStyle = hi.style
        assertTrue(fillStyle is ShapeDrawStyle.Fill)
        assertEquals(270.0, hi.sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(ChartColor.Red, fillStyle.color)
    }

    @Test
    fun selectionIgnoredWhenSeriesIdMismatches() {
        val spec =
            PieChartSpec(
                id = "sales",
                slices = listOf(PieChartSlice(1.0, "a", ChartColor.Blue)),
                legend = LegendSpec(enabled = false),
            )
        val sel =
            ChartSelection(
                target =
                    HighlightTarget(
                        seriesId = "other",
                        pointIndex = 0,
                        point = XyPoint(0.0, 0.0),
                    ),
                pixelOffset = OffsetD(0.0, 0.0),
            )
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec = spec,
                selection = sel,
            )
        assertTrue(plan.layout.plotDrawCommands.none { it is DrawCommand.Group })
    }

    @Test
    fun legendDisabled_leavesLegendEntriesEmpty() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices = listOf(PieChartSlice(1.0, "only", ChartColor.Blue)),
                        legend = LegendSpec(enabled = false),
                    ),
                selection = null,
            )
        assertTrue(plan.layout.legendEntries.isEmpty())
    }

    @Test
    fun sliceLabels_emitPolylineAndTextPerSlice() {
        val slices =
            listOf(
                PieChartSlice(50.0, "A", ChartColor.Blue),
                PieChartSlice(50.0, "B", ChartColor.Green),
            )
        val spec =
            PieChartSpec(
                slices = slices,
                sliceLabels = PieSliceLabelsSpec(enabled = true),
                legend = LegendSpec(enabled = false),
            )
        val uniform = TextLayoutMetrics.uniformBox(SizeD(72.0, 14.0))
        val input =
            baseInput().copy(
                textMeasurements =
                    ChartTextMeasurementInput(
                        pieSliceLabelMeasurements =
                            mapOf(
                                "0" to uniform,
                                "1" to uniform,
                            ),
                    ),
            )
        val plan = ChartPlanner.planPieChart(input, spec, selection = null)
        val polylines = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Polyline>()
        val texts = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Text>()
        assertEquals(2, polylines.size)
        assertEquals(2, texts.size)
        assertTrue(texts.all { it.isPieSliceLabel })
        assertTrue(texts.any { it.text.contains("A") && it.text.contains("%") })
    }

    @Test
    fun sliceLabels_respectMinSweepDegrees() {
        val slices =
            listOf(
                PieChartSlice(99.0, "Big", ChartColor.Blue),
                PieChartSlice(1.0, "Tiny", ChartColor.Green),
            )
        val spec =
            PieChartSpec(
                slices = slices,
                sliceLabels = PieSliceLabelsSpec(enabled = true, minSweepDegrees = 10.0),
                legend = LegendSpec(enabled = false),
            )
        val uniform = TextLayoutMetrics.uniformBox(SizeD(72.0, 14.0))
        val input =
            baseInput().copy(
                textMeasurements =
                    ChartTextMeasurementInput(
                        pieSliceLabelMeasurements = mapOf("0" to uniform, "1" to uniform),
                    ),
            )
        val plan = ChartPlanner.planPieChart(input, spec, selection = null)
        val texts = plan.layout.plotDrawCommands.filterIsInstance<DrawCommand.Text>()
        assertEquals(1, texts.size)
        assertTrue(texts.single().text.contains("Big"))
    }

    @Test
    fun startAngle_rotatesFirstArcStart() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(1.0, "A", ChartColor.Blue),
                                PieChartSlice(1.0, "B", ChartColor.Green),
                            ),
                        startAngleDegrees = 0.0,
                        legend = LegendSpec(enabled = false),
                    ),
                selection = null,
            )
        val fills = collectArcs(plan.layout.plotDrawCommands).filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(2, fills.size)
        assertEquals(0.0, fills[0].startAngleDegrees, absoluteTolerance = 1e-6)
    }

    @Test
    fun sliceGap_reducesSweep_andOffsetsSecondArc() {
        val plan =
            ChartPlanner.planPieChart(
                input = baseInput(),
                spec =
                    PieChartSpec(
                        slices =
                            listOf(
                                PieChartSlice(1.0, "A", ChartColor.Blue),
                                PieChartSlice(1.0, "B", ChartColor.Green),
                            ),
                        sliceGapDegrees = 10.0,
                        legend = LegendSpec(enabled = false),
                    ),
                selection = null,
            )
        val fills = collectArcs(plan.layout.plotDrawCommands).filter { it.style is ShapeDrawStyle.Fill }
        assertEquals(2, fills.size)
        assertEquals(170.0, fills[0].sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(170.0, fills[1].sweepAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(-90.0, fills[0].startAngleDegrees, absoluteTolerance = 1e-6)
        assertEquals(90.0, fills[1].startAngleDegrees, absoluteTolerance = 1e-6)
    }
}
