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
import com.mena97villalobos.composeplotlib.chart.ScatterChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.samples.android.data.emptyScatterChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleScatterChartSpec

@Composable
fun ScatterChartShowcaseScreen() {
    var variant by remember { mutableStateOf(ScatterShowcaseVariant.Styled) }
    val spec =
        when (variant) {
            ScatterShowcaseVariant.Styled -> sampleScatterChartSpec()
            ScatterShowcaseVariant.Empty -> emptyScatterChartSpec()
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
                        "Scatter chart showcase. Styled data with two series; empty variant shows no-data UI. " +
                        "Pan, zoom, and tap a point to select."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Scatter Chart Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Point-only cartesian chart: no line segments. Selection uses nearest point in pixel space among " +
                    "markers inside the plot area. Selection tooltips use plot-bound placement (flip/slide) like line charts.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { variant = ScatterShowcaseVariant.Styled }) {
                Text("Styled")
            }
            Button(onClick = { variant = ScatterShowcaseVariant.Empty }) {
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
        ScatterChart(
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
                if (variant == ScatterShowcaseVariant.Empty) {
                    ChartNoDataDefaults.scatterChart()
                } else {
                    null
                },
            accessibilityCustomActions =
                listOf(
                    ChartCustomAccessibilityAction("Reset viewport") {
                        viewportState.reset()
                        selectionState.clear()
                    },
                ),
        )
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val series = spec.series.firstOrNull { it.id == selection.target.seriesId }
                    val point =
                        series?.points?.getOrNull(selection.target.pointIndex)
                            ?: ChartPoint(selection.target.point.x, selection.target.point.y)
                    val seriesLabel = series?.label ?: selection.target.seriesId
                    "Selected $seriesLabel: ${spec.valueFormatter(point)}"
                } ?: "Tap a point to inspect selection.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class ScatterShowcaseVariant {
    Styled,
    Empty,
}
