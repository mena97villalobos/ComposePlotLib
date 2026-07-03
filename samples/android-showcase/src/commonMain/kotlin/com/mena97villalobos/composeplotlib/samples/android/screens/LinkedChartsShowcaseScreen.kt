package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.BarChart
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSyncAxis
import com.mena97villalobos.composeplotlib.chart.state.SyncChartViewports
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec

@Composable
fun LinkedChartsShowcaseScreen() {
    val months = (0..11).map { it.toDouble() }
    val priceSpec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "price",
                        label = "Price",
                        points = months.map { ChartPoint(it, 40.0 + 18.0 * kotlin.math.sin(it / 2.0)) },
                        lineStyle =
                            com.mena97villalobos.composeplotlib.core.spec
                                .LineStyle(color = ChartColor(0xFF4E79A7)),
                    ),
                ),
        )
    val volumeSpec =
        BarChartSpec(
            groups =
                listOf(
                    BarGroup(
                        id = "volume",
                        label = "Volume",
                        entries = months.map { BarEntry(x = it, y = 20.0 + 12.0 * ((it.toInt() * 7) % 5)) },
                        style = BarStyle(color = ChartColor(0xFFF28E2B)),
                    ),
                ),
        )

    val priceViewport = rememberChartViewportState(initialViewport = defaultViewport(priceSpec))
    val volumeViewport = rememberChartViewportState(initialViewport = defaultViewport(volumeSpec))

    // Keep the two charts aligned on X: panning/zooming either one moves the other.
    SyncChartViewports(priceViewport, volumeViewport, axis = ChartSyncAxis.X)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Linked charts showcase. A price line and a volume bar chart share one X axis window."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Linked Charts (shared X)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Two independent charts wired together with SyncChartViewports. Pan or zoom either chart with two " +
                    "fingers and the other follows on the X axis, keeping the price and volume aligned in time.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = "Price", style = MaterialTheme.typography.titleSmall)
        LineChart(
            spec = priceSpec,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            viewportState = priceViewport,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
        Text(text = "Volume", style = MaterialTheme.typography.titleSmall)
        BarChart(
            spec = volumeSpec,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            viewportState = volumeViewport,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
    }
}
