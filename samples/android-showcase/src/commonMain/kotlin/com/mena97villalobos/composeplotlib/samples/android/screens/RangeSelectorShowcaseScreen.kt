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
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.ChartRangeSelector
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.sin

@Composable
fun RangeSelectorShowcaseScreen() {
    val spec =
        remember {
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "series",
                            label = "Series",
                            points = (0..119).map { ChartPoint(it.toDouble(), 50.0 + 25.0 * sin(it / 6.0) + (it % 11)) },
                            lineStyle = LineStyle(color = ChartColor(0xFF4E79A7)),
                        ),
                    ),
            )
        }
    val fullViewport = remember { defaultViewport(spec) }
    val initialWindow =
        remember {
            ChartViewport(
                xRange = ClosedRangeD(fullViewport.xRange.start, fullViewport.xRange.midpoint),
                yRange = fullViewport.yRange,
            )
        }
    val viewport = rememberChartViewportState(initialViewport = initialWindow, domain = fullViewport)

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Range selector showcase. A detail chart above a minimap navigator that shares its viewport."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Range Selector (minimap)",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "The strip below is a ChartRangeSelector bound to the same ChartViewportState as the chart. Drag the " +
                    "highlighted window to pan, drag its edges to zoom, or drag outside it to jump — the chart follows.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(240.dp),
            viewportState = viewport,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
        Text(text = "Navigator", style = MaterialTheme.typography.titleSmall)
        ChartRangeSelector(
            spec = spec,
            viewportState = viewport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        )
    }
}
