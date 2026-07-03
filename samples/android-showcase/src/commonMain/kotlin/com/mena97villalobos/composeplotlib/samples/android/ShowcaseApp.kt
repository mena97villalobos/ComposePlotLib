package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.samples.android.screens.BarChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BoxPlotChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BubbleChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BulletChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.CandlestickChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.CombinedChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.DomainAxesShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.FunnelChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.GaugeChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.HeatmapChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.ImageExportShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.LineChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.LinkedChartsShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.PieChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.RadarChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.SankeyChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.ScatterChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.StackedAreaChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.WaterfallChartShowcaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ShowcaseApp() {
    MaterialTheme {
        var destination by remember { mutableStateOf(ShowcaseDestination.Home) }

        Scaffold(
            topBar = {
                if (destination != ShowcaseDestination.Home) {
                    TopAppBar(
                        title = { Text(destination.title) },
                        navigationIcon = {
                            TextButton(onClick = { destination = ShowcaseDestination.Home }) {
                                Text("Back")
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            val detailModifier =
                Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            when (destination) {
                ShowcaseDestination.Home ->
                    ShowcaseHomeScreen(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .padding(16.dp)
                                .fillMaxSize()
                                .semantics {
                                    contentDescription =
                                        "ComposePlotLib showcase home. Open line, bar, scatter, pie, radar, combined, sankey, heatmap, " +
                                        "gauge, candlestick, or domain-axis demos."
                                },
                        onOpenDestination = { destination = it },
                    )
                ShowcaseDestination.Line ->
                    Column(detailModifier) {
                        LineChartShowcaseScreen()
                    }
                ShowcaseDestination.Bar ->
                    Column(detailModifier) {
                        BarChartShowcaseScreen()
                    }
                ShowcaseDestination.Scatter ->
                    Column(detailModifier) {
                        ScatterChartShowcaseScreen()
                    }
                ShowcaseDestination.Pie ->
                    Column(detailModifier) {
                        PieChartShowcaseScreen()
                    }
                ShowcaseDestination.Radar ->
                    Column(detailModifier) {
                        RadarChartShowcaseScreen()
                    }
                ShowcaseDestination.Combined ->
                    Column(detailModifier) {
                        CombinedChartShowcaseScreen()
                    }
                ShowcaseDestination.Sankey ->
                    Column(detailModifier) {
                        SankeyChartShowcaseScreen()
                    }
                ShowcaseDestination.Heatmap ->
                    Column(detailModifier) {
                        HeatmapChartShowcaseScreen()
                    }
                ShowcaseDestination.Gauge ->
                    Column(detailModifier) {
                        GaugeChartShowcaseScreen()
                    }
                ShowcaseDestination.Candlestick ->
                    Column(detailModifier) {
                        CandlestickChartShowcaseScreen()
                    }
                ShowcaseDestination.BoxPlot ->
                    Column(detailModifier) {
                        BoxPlotChartShowcaseScreen()
                    }
                ShowcaseDestination.Waterfall ->
                    Column(detailModifier) {
                        WaterfallChartShowcaseScreen()
                    }
                ShowcaseDestination.Bubble ->
                    Column(detailModifier) {
                        BubbleChartShowcaseScreen()
                    }
                ShowcaseDestination.Bullet ->
                    Column(detailModifier) {
                        BulletChartShowcaseScreen()
                    }
                ShowcaseDestination.Funnel ->
                    Column(detailModifier) {
                        FunnelChartShowcaseScreen()
                    }
                ShowcaseDestination.StackedArea ->
                    Column(detailModifier) {
                        StackedAreaChartShowcaseScreen()
                    }
                ShowcaseDestination.LinkedCharts ->
                    Column(detailModifier) {
                        LinkedChartsShowcaseScreen()
                    }
                ShowcaseDestination.ImageExport ->
                    Column(detailModifier) {
                        ImageExportShowcaseScreen()
                    }
                ShowcaseDestination.DomainAxes ->
                    Column(detailModifier) {
                        DomainAxesShowcaseScreen()
                    }
            }
        }
    }
}

@Composable
private fun ShowcaseHomeScreen(
    onOpenDestination: (ShowcaseDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "ComposePlotLib Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Pick a chart family to open its demo screen.",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Line) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Line chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Bar) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Bar chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Scatter) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Scatter chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Pie) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Pie chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Radar) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Radar chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Combined) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Combined (multi-layer)")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Sankey) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sankey chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Heatmap) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Heatmap chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Gauge) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Gauge chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Candlestick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Candlestick chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.BoxPlot) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Box plot chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Waterfall) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Waterfall chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Bubble) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Bubble chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Bullet) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Bullet graph")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.Funnel) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Funnel chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.StackedArea) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Stacked area chart")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.LinkedCharts) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Linked charts (shared X)")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.ImageExport) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Raster export (PNG)")
        }
        OutlinedButton(
            onClick = { onOpenDestination(ShowcaseDestination.DomainAxes) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Domain axes")
        }
    }
}

private enum class ShowcaseDestination(
    val title: String,
) {
    Home(""),
    Line("Line chart"),
    Bar("Bar chart"),
    Scatter("Scatter chart"),
    Pie("Pie chart"),
    Radar("Radar chart"),
    Combined("Combined chart"),
    Sankey("Sankey chart"),
    Heatmap("Heatmap chart"),
    Gauge("Gauge chart"),
    Candlestick("Candlestick chart"),
    BoxPlot("Box plot chart"),
    Waterfall("Waterfall chart"),
    Bubble("Bubble chart"),
    Bullet("Bullet graph"),
    Funnel("Funnel chart"),
    StackedArea("Stacked area chart"),
    LinkedCharts("Linked charts"),
    ImageExport("Raster export"),
    DomainAxes("Domain axes"),
}
