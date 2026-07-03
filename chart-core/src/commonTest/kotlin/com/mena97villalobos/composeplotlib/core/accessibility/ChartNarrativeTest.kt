package com.mena97villalobos.composeplotlib.core.accessibility

import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartNarrativeTest {
    @Test
    fun risingSeriesReportsCountRangeAndTrend() {
        val spec =
            LineChartSpec(
                series = listOf(LineSeries("s", "Sales", listOf(ChartPoint(0.0, 2.0), ChartPoint(1.0, 5.0), ChartPoint(2.0, 10.0)))),
            )
        val narrative = lineChartNarrative(spec)
        assertTrue(narrative.startsWith("Line chart, 1 series."), narrative)
        assertTrue(narrative.contains("Sales: 3 points"), narrative)
        assertTrue(narrative.contains("rises from 2 to 10"), narrative)
        assertTrue(narrative.contains("ranging 2 to 10"), narrative)
    }

    @Test
    fun fallingSeriesUsesFallsPhrase() {
        val spec =
            LineChartSpec(series = listOf(LineSeries("s", "S", listOf(ChartPoint(0.0, 10.0), ChartPoint(1.0, 3.0)))))
        assertTrue(lineChartNarrative(spec).contains("falls from 10 to 3"))
    }

    @Test
    fun flatSeriesReadsAsSteady() {
        val spec =
            LineChartSpec(series = listOf(LineSeries("s", "S", listOf(ChartPoint(0.0, 5.0), ChartPoint(1.0, 5.0), ChartPoint(2.0, 5.0)))))
        assertTrue(lineChartNarrative(spec).contains("stays around 5"))
    }

    @Test
    fun narrativeSortsByXBeforeComputingTrend() {
        // Points out of x order: first(x=0)=1, last(x=2)=9 after sorting -> rising.
        val spec =
            LineChartSpec(series = listOf(LineSeries("s", "S", listOf(ChartPoint(2.0, 9.0), ChartPoint(0.0, 1.0), ChartPoint(1.0, 5.0)))))
        assertTrue(lineChartNarrative(spec).contains("rises from 1 to 9"))
    }

    @Test
    fun emptyAndNoDataAreDescribed() {
        assertEquals("Line chart with no series.", lineChartNarrative(LineChartSpec(series = emptyList())))
        val noData = LineChartSpec(series = listOf(LineSeries("s", "Empty", emptyList())))
        assertTrue(lineChartNarrative(noData).contains("Empty has no data."))
    }

    @Test
    fun barNarrativePluralizesGroups() {
        val one = BarChartSpec(groups = listOf(BarGroup("g", "G", listOf(BarEntry(0.0, 3.0)))))
        assertTrue(barChartNarrative(one).startsWith("Bar chart, 1 group."))
        val two =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup("a", "A", listOf(BarEntry(0.0, 1.0), BarEntry(1.0, 4.0))),
                        BarGroup("b", "B", listOf(BarEntry(0.0, 2.0))),
                    ),
            )
        assertTrue(barChartNarrative(two).startsWith("Bar chart, 2 groups."))
    }

    @Test
    fun classifyTrendThresholds() {
        assertEquals(ChartTrend.Rising, classifyTrend(first = 0.0, last = 10.0, min = 0.0, max = 10.0))
        assertEquals(ChartTrend.Falling, classifyTrend(first = 10.0, last = 0.0, min = 0.0, max = 10.0))
        // Net change under 5% of the range reads steady.
        assertEquals(ChartTrend.Steady, classifyTrend(first = 5.0, last = 5.2, min = 0.0, max = 10.0))
    }
}
