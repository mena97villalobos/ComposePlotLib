package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InteractiveLegendTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun spec(hidden: Set<String>) =
        LineChartSpec(
            series =
                listOf(
                    LineSeries("a", "A", listOf(ChartPoint(0.0, 2.0), ChartPoint(2.0, 8.0), ChartPoint(4.0, 5.0)), LineStyle(color = ChartColor.Blue)),
                    LineSeries("b", "B", listOf(ChartPoint(0.0, 10.0), ChartPoint(2.0, 14.0), ChartPoint(4.0, 12.0)), LineStyle(color = ChartColor.Red)),
                ),
            legend = LegendSpec(enabled = true),
            hiddenSeriesIds = hidden,
        )

    private fun polylineCount(plan: LineChartPlan) = plan.layout.plotDrawCommands.count { it is DrawCommand.Polyline }

    @Test
    fun hiddenSeriesIsNotDrawnButStillInLegend() {
        val all = ChartPlanner.planLineChart(input, spec(emptySet()), selection = null)
        val hiddenB = ChartPlanner.planLineChart(input, spec(setOf("b")), selection = null)
        assertEquals(2, polylineCount(all))
        assertEquals(1, polylineCount(hiddenB), "hidden series 'b' is not drawn")
        // Both entries still appear in the legend, tagged with their series id and hidden flag.
        assertEquals(listOf("a", "b"), hiddenB.layout.legendEntries.map { it.seriesId })
        assertFalse(
            hiddenB.layout.legendEntries
                .first { it.seriesId == "a" }
                .hidden,
        )
        assertTrue(
            hiddenB.layout.legendEntries
                .first { it.seriesId == "b" }
                .hidden,
        )
    }

    @Test
    fun legendEntryExposesHittableBounds() {
        val plan = ChartPlanner.planLineChart(input, spec(emptySet()), selection = null)
        val entry = plan.layout.legendEntries.first()
        // Bounds contain the indicator swatch center.
        assertTrue(entry.bounds.contains(entry.indicatorCenter))
        assertEquals("a", entry.seriesId)
    }
}
