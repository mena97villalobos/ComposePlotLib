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
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.samples.android.data.sampleCategoryLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleEpochMillisLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleLongCategoryLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleWeekdayEpochMillisLineChartSpec

@Composable
fun DomainAxesShowcaseScreen() {
    val categorySpec = remember { sampleCategoryLineChartSpec() }
    val longCategorySpec = remember { sampleLongCategoryLineChartSpec() }
    val timeSpec = remember { sampleEpochMillisLineChartSpec() }
    val weekdayTimeSpec = remember { sampleWeekdayEpochMillisLineChartSpec() }
    val categoryViewport = rememberChartViewportState(initialViewport = defaultViewport(categorySpec))
    val longCategoryViewport = rememberChartViewportState(initialViewport = defaultViewport(longCategorySpec))
    val timeViewport = rememberChartViewportState(initialViewport = defaultViewport(timeSpec))
    val weekdayTimeViewport = rememberChartViewportState(initialViewport = defaultViewport(weekdayTimeSpec))
    val categorySelection = rememberChartSelectionState()
    val longCategorySelection = rememberChartSelectionState()
    val timeSelection = rememberChartSelectionState()
    val weekdayTimeSelection = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Domain axes showcase: category axes, long label truncation, epoch-millis UTC labels, " +
                        "weekday-only UTC tick policy, and viewport pan and zoom."
                },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Domain axes (category & time)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Category scale uses integer X indices mapped to labels; optional truncation caps tick-label " +
                    "width. Time scale uses Unix epoch milliseconds with nice steps; EpochMillisTickPolicy.WeekdaysUtc " +
                    "omits Saturday/Sunday UTC tick positions only (holidays remain app-level).",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = "Weekday categories", style = MaterialTheme.typography.titleMedium)
        LineChart(
            spec = categorySpec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            viewportState = categoryViewport,
            selectionState = categorySelection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
        Text(text = "Long category labels (truncated)", style = MaterialTheme.typography.titleMedium)
        LineChart(
            spec = longCategorySpec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            viewportState = longCategoryViewport,
            selectionState = longCategorySelection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
        Text(text = "Epoch time (UTC labels)", style = MaterialTheme.typography.titleMedium)
        LineChart(
            spec = timeSpec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            viewportState = timeViewport,
            selectionState = timeSelection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
        Text(text = "Epoch time (weekday ticks, UTC)", style = MaterialTheme.typography.titleMedium)
        LineChart(
            spec = weekdayTimeSpec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            viewportState = weekdayTimeViewport,
            selectionState = weekdayTimeSelection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
    }
}
