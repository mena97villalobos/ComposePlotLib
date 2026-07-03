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
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.StackedAreaChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.StackedAreaSeries
import com.mena97villalobos.composeplotlib.core.spec.AreaStackOffset
import com.mena97villalobos.composeplotlib.core.spec.StackedAreaChartSpec

@Composable
fun StackedAreaChartShowcaseScreen() {
    val xs = listOf(2019.0, 2020.0, 2021.0, 2022.0, 2023.0, 2024.0)
    val series =
        listOf(
            StackedAreaSeries("mobile", "Mobile", listOf(12.0, 18.0, 26.0, 34.0, 41.0, 48.0), ChartColor(0xFF4E79A7)),
            StackedAreaSeries("web", "Web", listOf(30.0, 28.0, 27.0, 25.0, 24.0, 22.0), ChartColor(0xFFF28E2B)),
            StackedAreaSeries("desktop", "Desktop", listOf(20.0, 16.0, 13.0, 10.0, 8.0, 6.0), ChartColor(0xFF59A14F)),
        )
    val absolute =
        StackedAreaChartSpec(xValues = xs, series = series, offset = AreaStackOffset.Zero)
    val percent =
        StackedAreaChartSpec(xValues = xs, series = series, offset = AreaStackOffset.Expand)
    val absoluteViewport = rememberChartViewportState(initialViewport = defaultViewport(absolute))
    val percentViewport = rememberChartViewportState(initialViewport = defaultViewport(percent))

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Stacked area chart showcase. Traffic mix over time, absolute and 100% stacked."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Stacked Area Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Bands stack over shared x values. Top: absolute totals (AreaStackOffset.Zero). Bottom: the same " +
                    "data as a 100% chart (AreaStackOffset.Expand), emphasizing composition. Pan and zoom with two fingers.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = "Absolute (Zero offset)", style = MaterialTheme.typography.titleSmall)
        StackedAreaChart(
            spec = absolute,
            modifier = Modifier.fillMaxWidth().height(240.dp),
            viewportState = absoluteViewport,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
        Text(text = "100% (Expand offset)", style = MaterialTheme.typography.titleSmall)
        StackedAreaChart(
            spec = percent,
            modifier = Modifier.fillMaxWidth().height(240.dp),
            viewportState = percentViewport,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
    }
}
