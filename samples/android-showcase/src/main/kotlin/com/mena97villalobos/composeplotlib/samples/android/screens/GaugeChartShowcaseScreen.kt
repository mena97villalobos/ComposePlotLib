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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.GaugeChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.clampedProgress
import kotlin.math.roundToInt

@Composable
fun GaugeChartShowcaseScreen() {
    var progress by remember { mutableStateOf(0.68) }
    val spec =
        GaugeChartSpec(
            progress = progress,
            id = "gauge_showcase",
            trackColor = ChartColor.LightGray,
            progressColor = ChartColor.Blue,
            strokeWidthPx = 28.0,
            roundedCaps = true,
            sweepDegrees = 270.0,
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Gauge chart showcase. Radial progress ring with an adjustable value; the percentage is shown in the center."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Gauge Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Read-only radial progress ring (270° sweep). centerContent renders the percentage in the middle. " +
                    "There is no selection, pan, or zoom — a gauge shows a single value.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { progress = 0.25 }) {
                Text("25%")
            }
            Button(onClick = { progress = 0.5 }) {
                Text("50%")
            }
            Button(onClick = { progress = 0.68 }) {
                Text("68%")
            }
            Button(onClick = { progress = 1.0 }) {
                Text("100%")
            }
        }
        GaugeChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp),
            centerContent = {
                val percent = (spec.clampedProgress() * 100).roundToInt()
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            },
        )
    }
}
