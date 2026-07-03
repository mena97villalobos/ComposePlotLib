package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.BoxPlotChart
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.BoxPlotEntry
import com.mena97villalobos.composeplotlib.core.model.BoxPlotSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotChartSpec

@Composable
fun BoxPlotChartShowcaseScreen() {
    val labels = listOf("Q1", "Q2", "Q3", "Q4")
    val spec =
        BoxPlotChartSpec(
            series =
                listOf(
                    BoxPlotSeries(
                        id = "latency",
                        label = "Latency (ms)",
                        entries =
                            listOf(
                                // Explicit five-number summaries, one with an outlier.
                                BoxPlotEntry(x = 0.0, min = 12.0, q1 = 18.0, median = 24.0, q3 = 31.0, max = 40.0),
                                BoxPlotEntry(x = 1.0, min = 15.0, q1 = 22.0, median = 28.0, q3 = 34.0, max = 45.0, outliers = listOf(58.0)),
                                // Built from raw samples via Tukey quartiles + 1.5·IQR whiskers.
                                BoxPlotEntry.fromSamples(
                                    x = 2.0,
                                    values = listOf(20.0, 22.0, 23.0, 25.0, 26.0, 27.0, 29.0, 31.0, 33.0, 70.0),
                                )!!,
                                BoxPlotEntry(x = 3.0, min = 10.0, q1 = 16.0, median = 20.0, q3 = 27.0, max = 36.0),
                            ),
                    ),
                ),
            xAxis = AxisSpec(labelFormatter = { value -> labels.getOrNull(value.toInt()) ?: "" }),
        )
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Box plot showcase. Quarterly latency distributions; tap a box to inspect its five-number summary."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Box Plot Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Each box spans the interquartile range (Q1–Q3) with a median line; whiskers reach the min/max within " +
                    "1.5×IQR and outliers are drawn as points. Q3's box is built from raw samples via " +
                    "BoxPlotEntry.fromSamples. Tap a box to inspect it; pan and zoom with two fingers.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = {
                viewportState.reset()
                selectionState.clear()
            }) {
                Text("Reset")
            }
        }
        BoxPlotChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val series = spec.series.firstOrNull { it.id == selection.target.seriesId }
                    val entry = series?.entries?.getOrNull(selection.target.pointIndex)
                    entry?.let {
                        "${labels.getOrNull(it.x.toInt()) ?: "?"}: median ${it.median}, Q1 ${it.q1}, Q3 ${it.q3}, " +
                            "min ${it.min}, max ${it.max}"
                    } ?: "Selected box ${selection.target.pointIndex + 1}"
                } ?: "Tap a box to inspect its five-number summary.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
