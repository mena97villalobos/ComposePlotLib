package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.RadarChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarSeries

@Composable
fun RadarChartShowcaseScreen() {
    val spec =
        RadarChartSpec(
            id = "radar_showcase",
            axes = listOf("Speed", "Quality", "Cost", "Reliability", "UX", "Support"),
            series =
                listOf(
                    RadarSeries(
                        id = "current",
                        label = "Current",
                        values = listOf(6.0, 7.0, 5.0, 6.5, 7.5, 6.0),
                        lineStyle = LineStyle(color = ChartColor.Blue, strokeWidth = 2.5),
                        pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Blue),
                        fillColor = ChartColor(0x331565C0L),
                    ),
                    RadarSeries(
                        id = "target",
                        label = "Target",
                        values = listOf(8.0, 8.5, 6.0, 8.0, 9.0, 8.5),
                        lineStyle = LineStyle(color = ChartColor.Green, strokeWidth = 2.5),
                        pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Green),
                        fillColor = ChartColor(0x332E7D32L),
                    ),
                ),
        )
    val viewport = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selection = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        RadarChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            viewportState = viewport,
            selectionState = selection,
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
        Text(
            text =
                selection.selection?.let { sel ->
                    val axis = spec.axes.getOrNull(sel.target.pointIndex)
                    val series = spec.series.firstOrNull { it.id == sel.target.seriesId }
                    val label = series?.label ?: sel.target.seriesId
                    axis?.let { "Selected $label: ${spec.axisValueFormatter(it, sel.target.point.y)}" }
                        ?: "Selected $label"
                } ?: "Tap a vertex to select.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

