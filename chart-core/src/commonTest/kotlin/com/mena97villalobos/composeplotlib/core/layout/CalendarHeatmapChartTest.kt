package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapDay
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapSpec
import com.mena97villalobos.composeplotlib.core.spec.CalendarMonthLabel
import com.mena97villalobos.composeplotlib.core.spec.resolvedWeekCount
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarHeatmapChartTest {
    private fun plan(spec: CalendarHeatmapSpec): ChartLayoutPlan {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 160.0),
                viewport = ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)),
            )
        return ChartPlanner.planCalendarHeatmapChart(input, spec).layout
    }

    @Test
    fun autoSizesWeeksFromLargestDay() {
        // Day 20 with startWeekday 0 → column 2, so 3 weeks.
        val spec = CalendarHeatmapSpec(days = listOf(CalendarHeatmapDay(0, 1.0), CalendarHeatmapDay(20, 5.0)))
        assertEquals(3, spec.resolvedWeekCount())
    }

    @Test
    fun emitsBackgroundGridPlusValueCells() {
        val spec =
            CalendarHeatmapSpec(
                days = listOf(CalendarHeatmapDay(0, 1.0), CalendarHeatmapDay(8, 3.0)),
                weekCount = 3,
            )
        val roundRects = plan(spec).plotDrawCommands.filterIsInstance<DrawCommand.RoundRect>()
        // 3 weeks * 7 rows background + 2 value cells.
        assertEquals(3 * 7 + 2, roundRects.size)
    }

    @Test
    fun emitsWeekdayAndMonthLabels() {
        val spec =
            CalendarHeatmapSpec(
                days = listOf(CalendarHeatmapDay(0, 1.0)),
                weekCount = 4,
                weekdayLabels = listOf("", "Mon", "", "Wed", "", "Fri", ""),
                monthLabels = listOf(CalendarMonthLabel(weekIndex = 0, label = "Jan")),
            )
        val texts = plan(spec).overlayDrawCommands.filterIsInstance<DrawCommand.Text>()
        // 3 non-blank weekday labels + 1 month label.
        assertEquals(4, texts.size)
        assertTrue(texts.any { it.text == "Jan" })
        assertTrue(texts.any { it.text == "Wed" })
    }
}
