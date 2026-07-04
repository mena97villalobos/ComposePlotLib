package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One day of a [CalendarHeatmapSpec]: a day offset and its value.
 *
 * @property dayIndex 0-based offset from the first day of the calendar (day 0 is the first cell). Callers compute
 *   this from their own date type — the library stays KMP-safe and date-library-free.
 * @property value the day's magnitude, colored via [CalendarHeatmapSpec.colorScale].
 */
data class CalendarHeatmapDay(
    val dayIndex: Int,
    val value: Double,
)

/** A month (or period) label placed above a specific week column of a [CalendarHeatmapSpec]. */
data class CalendarMonthLabel(
    val weekIndex: Int,
    val label: String,
)

/**
 * Immutable configuration for a **calendar / year heatmap** (GitHub-contribution-style grid): each
 * [CalendarHeatmapDay] is placed in a column (week) and row (weekday) grid and colored by its value via a
 * [HeatmapColorScale]. Weeks run left→right; weekdays run top→bottom. Read-only (no pan/zoom/selection).
 *
 * Day → cell mapping: `pos = dayIndex + startWeekday`, `column = pos / 7`, `row = pos % 7` (row 0 at the top).
 *
 * @property days the values to plot; out-of-range days (column ≥ week count) are skipped.
 * @property startWeekday weekday of `dayIndex = 0` as a top-row offset (`0` = the first weekday row).
 * @property weekCount number of week columns; `null` auto-sizes to fit the largest day.
 * @property colorScale value→color mapping (reused from the heatmap family).
 * @property emptyCellColor background color for grid cells with no value.
 * @property cellGapPx gap between adjacent cells in pixels.
 * @property cellCornerRadiusPx cell corner radius in pixels.
 * @property weekdayLabels 7 row labels (top→bottom); blanks hide a row's label.
 * @property monthLabels optional labels placed above specific week columns.
 * @property id chart identity string.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding reserved for the weekday (left) and month (top) labels, in pixels.
 */
data class CalendarHeatmapSpec(
    val days: List<CalendarHeatmapDay>,
    val startWeekday: Int = 0,
    val weekCount: Int? = null,
    val colorScale: HeatmapColorScale = HeatmapColorScale(),
    val emptyCellColor: ChartColor = ChartColor(0xFFEBEDF0),
    val cellGapPx: Double = 2.0,
    val cellCornerRadiusPx: Double = 2.0,
    val weekdayLabels: List<String> = listOf("", "Mon", "", "Wed", "", "Fri", ""),
    val monthLabels: List<CalendarMonthLabel> = emptyList(),
    val id: String = "calendarHeatmap",
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 32.0, top = 20.0, right = 8.0, bottom = 8.0),
)

/** Number of week columns: [CalendarHeatmapSpec.weekCount], else enough to fit the largest day. */
fun CalendarHeatmapSpec.resolvedWeekCount(): Int {
    val explicit = weekCount
    if (explicit != null && explicit > 0) return explicit
    val maxDay = days.maxOfOrNull { it.dayIndex } ?: return 1
    return (maxDay + startWeekday.coerceAtLeast(0)) / 7 + 1
}
