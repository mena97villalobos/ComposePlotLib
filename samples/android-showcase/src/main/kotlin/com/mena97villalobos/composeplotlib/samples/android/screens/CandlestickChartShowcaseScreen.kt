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
import com.mena97villalobos.composeplotlib.chart.CandlestickChart
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec

@Composable
fun CandlestickChartShowcaseScreen() {
    val spec =
        CandlestickChartSpec(
            series =
                listOf(
                    CandlestickSeries(
                        id = "acme",
                        label = "ACME",
                        points =
                            listOf(
                                OHLCPoint(x = 0.0, open = 20.0, high = 24.0, low = 19.0, close = 23.0),
                                OHLCPoint(x = 1.0, open = 23.0, high = 25.0, low = 21.5, close = 22.0),
                                OHLCPoint(x = 2.0, open = 22.0, high = 22.5, low = 18.0, close = 19.0),
                                OHLCPoint(x = 3.0, open = 19.0, high = 23.0, low = 18.5, close = 22.5),
                                OHLCPoint(x = 4.0, open = 22.5, high = 27.0, low = 22.0, close = 26.5),
                                OHLCPoint(x = 5.0, open = 26.5, high = 28.0, low = 25.0, close = 25.5),
                                OHLCPoint(x = 6.0, open = 25.5, high = 26.0, low = 22.0, close = 23.0),
                                OHLCPoint(x = 7.0, open = 23.0, high = 25.5, low = 22.5, close = 25.0),
                                OHLCPoint(x = 8.0, open = 25.0, high = 29.0, low = 24.5, close = 28.5),
                                OHLCPoint(x = 9.0, open = 28.5, high = 30.0, low = 27.0, close = 27.5),
                            ),
                    ),
                ),
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
                        "Candlestick chart showcase. OHLC candles; tap a candle to inspect its open, high, low, and close."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Candlestick Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "OHLC candles: bullish candles (close ≥ open) are green, bearish candles are red. The wick spans high–low; " +
                    "the body spans open–close. Tap a candle to inspect its values; pan and zoom with two fingers.",
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
        CandlestickChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
            marker = { markerData ->
                DefaultChartMarker(markerData)
            },
        )
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val series = spec.series.firstOrNull { it.id == selection.target.seriesId }
                    val candle = series?.points?.getOrNull(selection.target.pointIndex)
                    candle?.let { spec.valueFormatter(it) }
                        ?: "Selected candle ${selection.target.pointIndex + 1}"
                } ?: "Tap a candle to inspect its OHLC values.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
