package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.export.chartCaptureTarget
import com.mena97villalobos.composeplotlib.chart.export.rememberChartCaptureController
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import kotlinx.coroutines.launch

@Composable
fun ImageExportShowcaseScreen() {
    val spec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "sales",
                        label = "Sales",
                        points = (0..9).map { ChartPoint(it.toDouble(), 10.0 + (it % 4) * 6.0) },
                        lineStyle = LineStyle(color = ChartColor(0xFF4E79A7)),
                    ),
                ),
        )
    val capture = rememberChartCaptureController()
    val scope = rememberCoroutineScope()
    var exportedBytes by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Image export showcase. Render a chart and snapshot it to a PNG byte array."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Raster Export (PNG)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Attach Modifier.chartCaptureTarget(controller) to any chart, then call controller.capturePng() " +
                    "to snapshot exactly what was drawn — markers and overlays included — as PNG bytes to save or share.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(220.dp).chartCaptureTarget(capture),
        )
        Button(
            onClick = {
                scope.launch { exportedBytes = capture.capturePng().size }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export to PNG")
        }
        Text(
            text = exportedBytes?.let { "Exported $it bytes" } ?: "Tap to export the chart above as a PNG.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
