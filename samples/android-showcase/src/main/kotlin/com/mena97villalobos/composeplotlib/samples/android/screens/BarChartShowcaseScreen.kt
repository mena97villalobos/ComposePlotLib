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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.BarChart
import com.mena97villalobos.composeplotlib.chart.ChartCustomAccessibilityAction
import com.mena97villalobos.composeplotlib.chart.ChartNoDataDefaults
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.StackedBarChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.spec.DataLabelsSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyBarChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleBarChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleHorizontalClusteredBarChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleStackedBarChartSpec

@Composable
fun BarChartShowcaseScreen() {
    var variant by remember { mutableStateOf(BarShowcaseVariant.Styled) }
    var showValues by remember { mutableStateOf(false) }
    var hiddenGroups by remember { mutableStateOf(emptySet<String>()) }
    val stackedSpec = remember { sampleStackedBarChartSpec() }
    val baseSpec =
        remember(variant) {
            when (variant) {
                BarShowcaseVariant.Styled -> sampleBarChartSpec()
                BarShowcaseVariant.Empty -> emptyBarChartSpec()
                BarShowcaseVariant.Stacked -> stackedSpec.toBarChartSpec()
                BarShowcaseVariant.Horizontal -> sampleHorizontalClusteredBarChartSpec()
            }
        }
    val spec =
        baseSpec.copy(
            dataLabels = if (showValues) DataLabelsSpec(enabled = true) else baseSpec.dataLabels,
            hiddenSeriesIds = hiddenGroups,
        )
    val viewportState =
        rememberChartViewportState(
            initialViewport = defaultViewport(spec),
            domain = defaultViewport(spec),
        )
    val selectionState = rememberChartSelectionState()

    LaunchedEffect(variant) {
        selectionState.clear()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Bar chart showcase. Choose styled or empty data, use viewport actions, tap chart to select a bar. " +
                        "Styled chart is focusable and exposes a reset custom action for TalkBack."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Bar Chart Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "This screen validates the bar-chart slice: pan, zoom, selection, legend, and stronger axis planning. " +
                    "Use Stacked or Horizontal to exercise those layouts; Empty uses noDataUi presets. " +
                    "Styled data opts into focus for keyboard/switch navigation.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { variant = BarShowcaseVariant.Styled }) {
                Text("Styled")
            }
            Button(onClick = { variant = BarShowcaseVariant.Stacked }) {
                Text("Stacked")
            }
            Button(onClick = { variant = BarShowcaseVariant.Horizontal }) {
                Text("Horizontal")
            }
            Button(onClick = { variant = BarShowcaseVariant.Empty }) {
                Text("Empty")
            }
            Button(onClick = { showValues = !showValues }) {
                Text(if (showValues) "Hide values" else "Show values")
            }
        }
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
            Button(onClick = {
                viewportState.zoomTo(xSpan = 2.5, ySpan = 10.0, centerX = 2.0, centerY = 10.0)
            }) {
                Text("Zoom To")
            }
            Button(onClick = {
                viewportState.centerOn(x = 3.0, y = 12.0)
            }) {
                Text("Center On")
            }
        }
        if (variant == BarShowcaseVariant.Stacked) {
            StackedBarChart(
                spec = stackedSpec,
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
                accessibilityCustomActions =
                    listOf(
                        ChartCustomAccessibilityAction("Reset viewport") {
                            viewportState.reset()
                            selectionState.clear()
                        },
                    ),
            )
        } else {
            BarChart(
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
                    if (variant == BarShowcaseVariant.Empty) {
                        ChartNoDataDefaults.barChart()
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
                onLegendItemClick = { id ->
                    hiddenGroups = if (id in hiddenGroups) hiddenGroups - id else hiddenGroups + id
                },
            )
        }
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val group = spec.groups.firstOrNull { it.id == selection.target.seriesId }
                    val entry =
                        group?.entries?.getOrNull(selection.target.pointIndex)
                            ?: BarEntry(selection.target.point.x, selection.target.point.y)
                    val groupLabel = group?.label ?: selection.target.seriesId
                    "Selected $groupLabel: ${spec.valueFormatter(entry)}"
                } ?: "Tap a bar to inspect selection.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class BarShowcaseVariant {
    Styled,
    Stacked,
    Horizontal,
    Empty,
}
