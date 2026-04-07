package com.mena97villalobos.composeplotlib.samples.android.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartCustomAccessibilityAction
import com.mena97villalobos.composeplotlib.chart.ChartNoDataDefaults
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.PieChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.samples.android.data.emptyPieChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleDonutChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.samplePieChartSpec

@Composable
fun PieChartShowcaseScreen() {
    var variant by remember { mutableStateOf(PieShowcaseVariant.Pie) }
    val spec =
        when (variant) {
            PieShowcaseVariant.Pie -> samplePieChartSpec()
            PieShowcaseVariant.Donut -> sampleDonutChartSpec()
            PieShowcaseVariant.Empty -> emptyPieChartSpec()
        }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Pie chart showcase. Pie, donut, and empty variants. Tap a slice to select."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Pie & Donut Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Radial chart: slice labels with leader lines when enabled in the spec; tap inside the ring to select a slice. Pan and zoom are disabled by default.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { variant = PieShowcaseVariant.Pie }) {
                Text("Pie")
            }
            Button(onClick = { variant = PieShowcaseVariant.Donut }) {
                Text("Donut")
            }
            Button(onClick = { variant = PieShowcaseVariant.Empty }) {
                Text("Empty")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                viewportState.reset()
                selectionState.clear()
            }) {
                Text("Reset")
            }
        }
        PieChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            marker = { markerData ->
                DefaultChartMarker(markerData)
            },
            noDataUi =
                if (variant == PieShowcaseVariant.Empty) {
                    ChartNoDataDefaults.pieChart()
                } else {
                    null
                },
            accessibilityCustomActions =
                listOf(
                    ChartCustomAccessibilityAction("Clear selection") {
                        selectionState.clear()
                    },
                ),
        )
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val slice = spec.slices.getOrNull(selection.target.pointIndex)
                    slice?.let { s ->
                        "${s.label}: ${spec.sliceValueFormatter(s)}"
                    } ?: "Selected slice ${selection.target.pointIndex + 1}"
                } ?: "Tap a slice to inspect selection.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class PieShowcaseVariant {
    Pie,
    Donut,
    Empty,
}
