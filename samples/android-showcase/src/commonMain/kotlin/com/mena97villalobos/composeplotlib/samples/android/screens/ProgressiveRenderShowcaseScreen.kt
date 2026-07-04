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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberProgressiveReveal
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import kotlin.math.sin

@Composable
fun ProgressiveRenderShowcaseScreen() {
    val allPoints =
        remember {
            (0 until 20_000).map { i ->
                ChartPoint(i.toDouble(), 100.0 + 40.0 * sin(i / 260.0) + 12.0 * sin(i / 23.0) + (i % 17))
            }
        }
    val fullViewport = remember { defaultViewport(LineChartSpec(series = listOf(LineSeries("s", "s", allPoints)))) }
    val viewport = rememberChartViewportState(initialViewport = fullViewport, domain = fullViewport)

    // Reveal the 20k-point series over successive frames instead of planning it all in one blocking frame.
    val revealed = rememberProgressiveReveal(total = allPoints.size, pointsPerFrame = 2000)
    val spec =
        remember(revealed) {
            LineChartSpec(
                series =
                    listOf(
                        LineSeries("signal", "Signal", allPoints.take(revealed), lineStyle = LineStyle(color = ChartColor(0xFF4E79A7))),
                    ),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Progressive rendering showcase. A 20,000-point series revealed frame by frame."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Progressive Rendering", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "rememberProgressiveReveal grows the drawn prefix of a 20,000-point series by a chunk each frame, so no " +
                    "single frame plans the whole set at once (a smooth draw-in). Combine with decimation for very large data.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = "Revealed: $revealed / ${allPoints.size}", style = MaterialTheme.typography.titleSmall)
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(280.dp),
            viewportState = viewport,
        )
    }
}
