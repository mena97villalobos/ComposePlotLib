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
import com.mena97villalobos.composeplotlib.core.export.lineChartToSvg
import com.mena97villalobos.composeplotlib.core.export.toCsv
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.table.lineChartDataTable
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
    var status by remember { mutableStateOf("Export the chart above as a PNG image, an SVG vector, or CSV data.") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Export showcase. Render a chart and export it as PNG, SVG, or CSV."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Export (PNG · SVG · CSV)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Snapshot a chart three ways: Modifier.chartCaptureTarget + capturePng() for a raster PNG, " +
                    "lineChartToSvg(spec, …) for a resolution-independent SVG vector, and lineChartDataTable(spec).toCsv() " +
                    "for the underlying data.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(220.dp).chartCaptureTarget(capture),
        )
        Button(
            onClick = {
                scope.launch { status = "PNG: ${capture.capturePng().size} bytes" }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export to PNG")
        }
        Button(
            onClick = {
                status = "SVG: ${lineChartToSvg(spec, widthPx = 600.0, heightPx = 360.0).length} chars"
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export to SVG")
        }
        Button(
            onClick = {
                status = "CSV: ${lineChartDataTable(spec).toCsv().length} chars"
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Export to CSV")
        }
        Text(text = status, style = MaterialTheme.typography.bodyMedium)
    }
}
