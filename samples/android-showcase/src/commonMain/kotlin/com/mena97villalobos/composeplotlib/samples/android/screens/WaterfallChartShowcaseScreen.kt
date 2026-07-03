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
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.WaterfallChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.WaterfallEntry
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.WaterfallChartSpec

@Composable
fun WaterfallChartShowcaseScreen() {
    val labels = listOf("Revenue", "COGS", "OpEx", "Other", "Net")
    val spec =
        WaterfallChartSpec(
            entries =
                listOf(
                    WaterfallEntry(delta = 120.0, label = "Revenue"),
                    WaterfallEntry(delta = -45.0, label = "COGS"),
                    WaterfallEntry(delta = -30.0, label = "OpEx"),
                    WaterfallEntry(delta = 8.0, label = "Other"),
                    WaterfallEntry(delta = 0.0, isTotal = true, label = "Net"),
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
                        "Waterfall showcase. A profit bridge from revenue to net; tap a bar to inspect its contribution."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Waterfall Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "A profit bridge: each bar floats from the running total before the step to the total after it. " +
                    "Increases are green, decreases are red, and the final Net bar is a total (spanning zero to the " +
                    "accumulated result). Tap a bar to inspect it; pan and zoom with two fingers.",
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
        WaterfallChart(
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
                    val entry = spec.entries.getOrNull(selection.target.pointIndex)
                    entry?.let {
                        if (it.isTotal) {
                            "${it.label}: total"
                        } else {
                            "${it.label}: ${if (it.delta >= 0) "+" else ""}${it.delta}"
                        }
                    } ?: "Selected bar ${selection.target.pointIndex + 1}"
                } ?: "Tap a bar to inspect its contribution.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
