package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.BoxPlotEntry
import com.mena97villalobos.composeplotlib.core.model.BoxPlotSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.BoxPlotHitResolver
import com.mena97villalobos.composeplotlib.core.selection.KeyboardSelectionRoam
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BoxPlotTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun plan(
        entry: BoxPlotEntry,
        selectionIndex: Int? = null,
    ): BoxPlotChartPlan {
        val spec =
            BoxPlotChartSpec(
                series = listOf(BoxPlotSeries("s", "S", listOf(entry))),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val prelim = ChartPlanner.planBoxPlotChart(input, spec)
        val selection =
            selectionIndex?.let { idx ->
                BoxPlotHitResolver.resolvePlanned(prelim.boxesForHitTest(), prelim.boxes[idx].boxBounds.center)
            }
        return ChartPlanner.planBoxPlotChart(input, spec, selection = selection)
    }

    private inline fun <reified T : DrawCommand> count(p: BoxPlotChartPlan) = p.layout.plotDrawCommands.count { it is T }

    @Test
    fun fromSamplesComputesQuartilesWhiskersAndOutliers() {
        val entry = BoxPlotEntry.fromSamples(0.0, listOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 100.0))!!
        assertEquals(3.25, entry.q1, 1e-9)
        assertEquals(5.5, entry.median, 1e-9)
        assertEquals(7.75, entry.q3, 1e-9)
        assertEquals(1.0, entry.min, 1e-9)
        assertEquals(9.0, entry.max, 1e-9)
        assertEquals(listOf(100.0), entry.outliers)
    }

    @Test
    fun fromSamplesReturnsNullForNoFiniteValues() {
        assertNull(BoxPlotEntry.fromSamples(0.0, listOf(Double.NaN, Double.POSITIVE_INFINITY)))
    }

    @Test
    fun boxEmitsExpectedDrawCommands() {
        val p = plan(BoxPlotEntry(x = 2.0, min = 2.0, q1 = 6.0, median = 10.0, q3 = 14.0, max = 18.0, outliers = listOf(19.0)))
        assertEquals(5, count<DrawCommand.Line>(p)) // 2 whiskers + 2 caps + median
        assertEquals(1, count<DrawCommand.Rect>(p)) // box fill
        assertEquals(1, count<DrawCommand.RoundRect>(p)) // box border
        assertEquals(1, count<DrawCommand.Circle>(p)) // one outlier
    }

    @Test
    fun selectionAddsAccentRoundRect() {
        val entry = BoxPlotEntry(x = 2.0, min = 2.0, q1 = 6.0, median = 10.0, q3 = 14.0, max = 18.0)
        val p = plan(entry, selectionIndex = 0)
        val selection = assertNotNull(p.selection)
        assertEquals(0, selection.target.pointIndex)
        assertEquals(2, count<DrawCommand.RoundRect>(p)) // border + selection accent
    }

    @Test
    fun keyboardRoamStepsWithinAndSwitchesSeries() {
        val spec =
            BoxPlotChartSpec(
                series =
                    listOf(
                        BoxPlotSeries(
                            "s1",
                            "S1",
                            listOf(
                                BoxPlotEntry(0.0, 2.0, 6.0, 10.0, 14.0, 18.0),
                                BoxPlotEntry(1.0, 3.0, 7.0, 11.0, 15.0, 19.0),
                            ),
                        ),
                        BoxPlotSeries(
                            "s2",
                            "S2",
                            listOf(
                                BoxPlotEntry(0.0, 1.0, 5.0, 9.0, 13.0, 17.0),
                                BoxPlotEntry(1.0, 2.0, 6.0, 10.0, 14.0, 18.0),
                            ),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            )
        val plan = ChartPlanner.planBoxPlotChart(input, spec)

        val first = assertNotNull(KeyboardSelectionRoam.boxPlotChartRoam(plan, null, deltaPoint = 1, deltaSeries = 0))
        assertEquals("s1", first.target.seriesId)
        assertEquals(0, first.target.pointIndex)

        val second = assertNotNull(KeyboardSelectionRoam.boxPlotChartRoam(plan, first, 1, 0))
        assertEquals(1, second.target.pointIndex)

        // Switching series (Shift+Down) resets to the first entry of the new series (matches candlestick).
        val down = assertNotNull(KeyboardSelectionRoam.boxPlotChartRoam(plan, second, 0, 1))
        assertEquals("s2", down.target.seriesId)
        assertEquals(0, down.target.pointIndex)

        // Clamp at the right end (no wrap).
        val right = assertNotNull(KeyboardSelectionRoam.boxPlotChartRoam(plan, down, 1, 0))
        val clamp = assertNotNull(KeyboardSelectionRoam.boxPlotChartRoam(plan, right, 1, 0))
        assertEquals(1, clamp.target.pointIndex)
    }

    @Test
    fun tapOutsideResolvesNull() {
        val spec =
            BoxPlotChartSpec(
                series =
                    listOf(
                        BoxPlotSeries(
                            "s",
                            "S",
                            listOf(BoxPlotEntry(2.0, 2.0, 6.0, 10.0, 14.0, 18.0)),
                        ),
                    ),
            )
        val plan = ChartPlanner.planBoxPlotChart(input, spec)
        assertNull(
            BoxPlotHitResolver.resolvePlanned(plan.boxesForHitTest(), OffsetD(5.0, 5.0)),
        )
    }
}
