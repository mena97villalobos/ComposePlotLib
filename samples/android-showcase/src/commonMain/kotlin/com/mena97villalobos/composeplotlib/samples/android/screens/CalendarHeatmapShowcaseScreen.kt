package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.CalendarHeatmapChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapDay
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapSpec
import com.mena97villalobos.composeplotlib.core.spec.CalendarMonthLabel
import com.mena97villalobos.composeplotlib.core.spec.HeatmapColorScale

@Composable
fun CalendarHeatmapShowcaseScreen() {
    val spec =
        remember {
            // Deterministic pseudo-random daily "contributions" over 53 weeks.
            val days =
                (0 until 371).map { day ->
                    val v = ((day * 2654435761u.toLong()) ushr 8).toInt() and 0x7
                    CalendarHeatmapDay(dayIndex = day, value = v.toDouble())
                }
            CalendarHeatmapSpec(
                days = days,
                startWeekday = 2,
                colorScale =
                    HeatmapColorScale(
                        minValue = 0.0,
                        maxValue = 7.0,
                        minColor = ChartColor(0xFFEBEDF0),
                        maxColor = ChartColor(0xFF216E39),
                    ),
                monthLabels =
                    listOf(
                        CalendarMonthLabel(0, "Jan"),
                        CalendarMonthLabel(9, "Mar"),
                        CalendarMonthLabel(18, "May"),
                        CalendarMonthLabel(27, "Jul"),
                        CalendarMonthLabel(35, "Sep"),
                        CalendarMonthLabel(44, "Nov"),
                    ),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Calendar heatmap showcase. A year of daily activity as a GitHub-style contribution grid."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Calendar Heatmap Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "A year of daily values as a week-column × weekday-row grid, colored by a HeatmapColorScale. " +
                    "Callers pass 0-based day offsets (no date library), plus optional weekday and month labels.",
            style = MaterialTheme.typography.bodyLarge,
        )
        CalendarHeatmapChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
    }
}
