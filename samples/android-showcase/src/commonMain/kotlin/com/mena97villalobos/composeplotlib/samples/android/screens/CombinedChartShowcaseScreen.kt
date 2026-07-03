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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartCustomAccessibilityAction
import com.mena97villalobos.composeplotlib.chart.CombinedCartesianChart
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

@Composable
fun CombinedChartShowcaseScreen() {
    var denseLegend by remember { mutableStateOf(false) }
    val spec =
        remember(denseLegend) {
            if (denseLegend) combinedShowcaseDenseLegendSpec() else combinedShowcaseDefaultSpec()
        }
    key(denseLegend) {
        val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
        val selectionState = rememberChartSelectionState()
        CombinedChartShowcaseContent(
            spec = spec,
            denseLegend = denseLegend,
            onDenseLegendChange = { denseLegend = it },
            viewportState = viewportState,
            selectionState = selectionState,
        )
    }
}

@Composable
private fun CombinedChartShowcaseContent(
    spec: CombinedCartesianChartSpec,
    denseLegend: Boolean,
    onDenseLegendChange: (Boolean) -> Unit,
    viewportState: ChartViewportState,
    selectionState: ChartSelectionState,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Combined cartesian showcase. Bars use primary Y; line uses secondary Y. Tap to select; top layer wins."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Combined Chart Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                if (denseLegend) {
                    "Dense legend: multiple bar groups, two line series (secondary Y), two scatter series. " +
                        "Legend uses labelMaxChars; tap to select."
                } else {
                    "Bar layer beneath line layer; dual Y scales. Reset viewport clears selection."
                },
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
            Button(onClick = { onDenseLegendChange(!denseLegend) }) {
                Text(if (denseLegend) "Standard legend" else "Dense legend demo")
            }
        }
        CombinedCartesianChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            marker = { markerData -> DefaultChartMarker(markerData) },
            accessibilityCustomActions =
                listOf(
                    ChartCustomAccessibilityAction(label = "Reset viewport") {
                        viewportState.reset()
                        selectionState.clear()
                    },
                ),
        )
    }
}

private fun combinedShowcaseDefaultSpec(): CombinedCartesianChartSpec =
    CombinedCartesianChartSpec(
        layers =
            listOf(
                CombinedCartesianLayer.Bar(
                    id = "volume",
                    yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                    groups =
                        listOf(
                            BarGroup(
                                id = "q",
                                label = "Quarter sales",
                                style = BarStyle(color = ChartColor.Green),
                                entries =
                                    listOf(
                                        BarEntry(0.0, 8.0),
                                        BarEntry(1.0, 14.0),
                                        BarEntry(2.0, 11.0),
                                        BarEntry(3.0, 17.0),
                                    ),
                            ),
                        ),
                ),
                CombinedCartesianLayer.Line(
                    id = "overlay",
                    yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                    series =
                        listOf(
                            LineSeries(
                                id = "idx",
                                label = "Index",
                                lineStyle = LineStyle(color = ChartColor.Blue),
                                pointStyle = PointStyle(radius = 5.0),
                                points =
                                    listOf(
                                        ChartPoint(0.0, 30.0),
                                        ChartPoint(1.0, 48.0),
                                        ChartPoint(2.0, 40.0),
                                        ChartPoint(3.0, 55.0),
                                    ),
                            ),
                        ),
                ),
            ),
        xAxis = AxisSpec(tickCount = 5),
        yAxis = AxisSpec(tickCount = 4),
        yAxisSecondary =
            AxisSpec(
                tickCount = 4,
                labelStyle = AxisLabelStyle(color = ChartColor.Blue),
            ),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
        contentPadding = InsetsD(left = 56.0, top = 48.0, right = 48.0, bottom = 40.0),
    )

private fun combinedShowcaseDenseLegendSpec(): CombinedCartesianChartSpec {
    val barColors = listOf(ChartColor.Green, ChartColor.Red, ChartColor.Blue, ChartColor.Gray)
    val barGroups =
        (0..3).map { q ->
            BarGroup(
                id = "q$q",
                label = "Quarter ${q + 1} revenue",
                style = BarStyle(color = barColors[q]),
                entries =
                    listOf(
                        BarEntry(q.toDouble(), 5.0 + q * 2.0),
                        BarEntry(q + 0.5, 4.0 + q),
                    ),
            )
        }
    return CombinedCartesianChartSpec(
        layers =
            listOf(
                CombinedCartesianLayer.Bar(
                    id = "revenue",
                    yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                    groups = barGroups,
                ),
                CombinedCartesianLayer.Line(
                    id = "indices",
                    yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                    series =
                        listOf(
                            LineSeries(
                                id = "idxA",
                                label = "Market index A (secondary)",
                                lineStyle = LineStyle(color = ChartColor.Blue),
                                pointStyle = PointStyle(radius = 4.0),
                                points =
                                    listOf(
                                        ChartPoint(0.0, 32.0),
                                        ChartPoint(1.0, 50.0),
                                        ChartPoint(2.0, 44.0),
                                        ChartPoint(3.0, 58.0),
                                    ),
                            ),
                            LineSeries(
                                id = "idxB",
                                label = "Market index B (secondary)",
                                lineStyle = LineStyle(color = ChartColor.Red),
                                pointStyle = PointStyle(radius = 4.0),
                                points =
                                    listOf(
                                        ChartPoint(0.0, 28.0),
                                        ChartPoint(1.0, 46.0),
                                        ChartPoint(2.0, 52.0),
                                        ChartPoint(3.0, 49.0),
                                    ),
                            ),
                        ),
                ),
                CombinedCartesianLayer.Scatter(
                    id = "outliers",
                    yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                    series =
                        listOf(
                            ScatterSeries(
                                id = "s1",
                                label = "Outlier probe north",
                                pointStyle = PointStyle(radius = 5.0, color = ChartColor.Red),
                                points = listOf(ChartPoint(0.25, 18.0), ChartPoint(2.25, 16.0)),
                            ),
                            ScatterSeries(
                                id = "s2",
                                label = "Outlier probe south",
                                pointStyle = PointStyle(radius = 5.0, color = ChartColor.Blue),
                                points = listOf(ChartPoint(1.25, 7.0), ChartPoint(3.25, 9.0)),
                            ),
                        ),
                ),
            ),
        xAxis = AxisSpec(tickCount = 6),
        yAxis = AxisSpec(tickCount = 4),
        yAxisSecondary =
            AxisSpec(
                tickCount = 4,
                labelStyle = AxisLabelStyle(color = ChartColor.Blue),
            ),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(style = LegendStyle(labelMaxChars = 14)),
        contentPadding = InsetsD(left = 56.0, top = 48.0, right = 48.0, bottom = 40.0),
    )
}
