package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mena97villalobos.composeplotlib.chart.ChartRangeBrush
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.selection.ChartBrushAxis
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RangeBrushShowcaseScreen() {
    val spec =
        remember {
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "signal",
                            label = "Signal",
                            points = (0..47).map { ChartPoint(it.toDouble(), 50.0 + 30.0 * sin(it / 4.0)) },
                            lineStyle = LineStyle(color = ChartColor(0xFF4E79A7)),
                        ),
                    ),
            )
        }
    val fullViewport = remember { defaultViewport(spec) }
    val viewport = rememberChartViewportState(initialViewport = fullViewport, domain = fullViewport)
    var status by remember { mutableStateOf("Drag across the chart to select an x-range to zoom into.") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Range brush showcase. Drag a box across the chart to zoom to the selected x-range."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Range Brush (box selection)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Attach ChartRangeBrush to a chart and drag a rectangle over the plot: the covered domain range is " +
                    "reported to onBrush. Here it drives zoom-to-selection. Use Reset to restore the full view.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(240.dp),
            viewportState = viewport,
            rangeBrush =
                ChartRangeBrush(
                    axis = ChartBrushAxis.Horizontal,
                    onBrush = { selection ->
                        if (selection != null) {
                            viewport.zoomTo(
                                xSpan = selection.xRange.span,
                                ySpan = fullViewport.yRange.span,
                                centerX = selection.xRange.midpoint,
                                centerY = fullViewport.yRange.midpoint,
                            )
                            status = "Zoomed to x ∈ [${selection.xRange.start.roundToInt()}, ${selection.xRange.endInclusive.roundToInt()}]"
                        }
                    },
                ),
        )
        Text(text = status, style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(
            onClick = {
                viewport.reset()
                status = "Drag across the chart to select an x-range to zoom into."
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reset view")
        }
    }
}
