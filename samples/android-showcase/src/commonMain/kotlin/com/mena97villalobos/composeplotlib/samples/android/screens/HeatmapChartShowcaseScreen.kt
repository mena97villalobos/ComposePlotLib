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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.HeatmapChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.samples.android.data.emptyHeatmapChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleHeatmapChartSpec

private enum class HeatmapShowcaseVariant { Default, Empty }

@Composable
fun HeatmapChartShowcaseScreen() {
    var variant by remember { mutableStateOf(HeatmapShowcaseVariant.Default) }
    val spec =
        remember(variant) {
            when (variant) {
                HeatmapShowcaseVariant.Default -> sampleHeatmapChartSpec()
                HeatmapShowcaseVariant.Empty -> emptyHeatmapChartSpec()
            }
        }
    val viewport = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selection = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HeatmapShowcaseVariant.entries.forEach { v ->
                FilterChip(
                    selected = variant == v,
                    onClick = { variant = v },
                    label = {
                        Text(
                            when (v) {
                                HeatmapShowcaseVariant.Default -> "Default"
                                HeatmapShowcaseVariant.Empty -> "Empty"
                            },
                        )
                    },
                )
            }
        }
        HeatmapChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            viewportState = viewport,
            selectionState = selection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
        Text(
            text =
                selection.selection?.let { sel ->
                    val cell = spec.cells.getOrNull(sel.target.pointIndex)
                    cell?.let { c ->
                        val col = spec.columnLabels.getOrElse(c.col) { c.col.toString() }
                        val row = spec.rowLabels.getOrElse(c.row) { c.row.toString() }
                        "Selected $col / $row: ${spec.valueFormatter(c)}"
                    } ?: "Selected cell ${sel.target.pointIndex}"
                } ?: "Tap a cell to select it.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
