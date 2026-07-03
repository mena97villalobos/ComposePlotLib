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
import com.mena97villalobos.composeplotlib.chart.BubbleChart
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.BubblePoint
import com.mena97villalobos.composeplotlib.core.model.BubbleSeries
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BubbleChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BubbleSizeScale

@Composable
fun BubbleChartShowcaseScreen() {
    val emerging =
        BubbleSeries(
            id = "emerging",
            label = "Emerging",
            color = ChartColor(0xFF4E79A7),
            points =
                listOf(
                    BubblePoint(x = 4.0, y = 62.0, z = 210.0),
                    BubblePoint(x = 6.0, y = 66.0, z = 55.0),
                    BubblePoint(x = 9.0, y = 71.0, z = 1400.0),
                    BubblePoint(x = 12.0, y = 74.0, z = 90.0),
                ),
        )
    val developed =
        BubbleSeries(
            id = "developed",
            label = "Developed",
            color = ChartColor(0xFFE15759),
            points =
                listOf(
                    BubblePoint(x = 30.0, y = 79.0, z = 60.0),
                    BubblePoint(x = 45.0, y = 81.0, z = 330.0),
                    BubblePoint(x = 55.0, y = 82.0, z = 125.0),
                    BubblePoint(x = 65.0, y = 83.0, z = 38.0),
                ),
        )
    val allZ = (emerging.points + developed.points).map { it.z }
    val spec =
        BubbleChartSpec(
            series = listOf(emerging, developed),
            sizeScale = BubbleSizeScale.auto(allZ, minRadiusPx = 5.0, maxRadiusPx = 34.0),
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
                        "Bubble chart showcase. Income vs life expectancy; bubble size encodes population. Tap a bubble to inspect it."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Bubble Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Each bubble plots income (X) against life expectancy (Y); its area is proportional to population " +
                    "via BubbleSizeScale. The size legend (top-right) shows representative magnitudes. Tap a bubble " +
                    "to inspect it; pan and zoom with two fingers.",
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
        BubbleChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
            marker = { markerData -> DefaultChartMarker(markerData) },
        )
    }
}
