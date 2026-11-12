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
import com.mena97villalobos.composeplotlib.chart.CandlestickChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickVariant

@Composable
fun CandlestickVariantsShowcaseScreen() {
    val points =
        listOf(
            OHLCPoint(0.0, 20.0, 24.0, 19.0, 23.0),
            OHLCPoint(1.0, 23.0, 25.0, 21.5, 22.0),
            OHLCPoint(2.0, 22.0, 22.5, 18.0, 19.0),
            OHLCPoint(3.0, 19.0, 23.0, 18.5, 22.5),
            OHLCPoint(4.0, 22.5, 27.0, 22.0, 26.5),
            OHLCPoint(5.0, 26.5, 28.0, 25.0, 25.5),
            OHLCPoint(6.0, 25.5, 26.0, 22.0, 23.0),
            OHLCPoint(7.0, 23.0, 25.5, 22.5, 25.0),
            OHLCPoint(8.0, 25.0, 29.0, 24.5, 28.5),
            OHLCPoint(9.0, 28.5, 30.0, 27.0, 27.5),
        )

    @Composable
    fun variant(
        title: String,
        variant: CandlestickVariant,
    ) {
        val spec =
            CandlestickChartSpec(
                series = listOf(CandlestickSeries("acme", "ACME", points)),
                variant = variant,
            )
        val viewport = rememberChartViewportState(initialViewport = defaultViewport(spec))
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        CandlestickChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            viewportState = viewport,
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Candlestick variants showcase. Hollow candles, OHLC bars, and Heikin-Ashi on the same data."
                },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = "Candlestick Variants", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "CandlestickChartSpec.variant renders the same OHLC data as hollow candles (bullish outlined), OHLC bars " +
                    "(open tick left / close tick right), or Heikin-Ashi smoothed candles.",
            style = MaterialTheme.typography.bodyLarge,
        )
        variant("Hollow candles", CandlestickVariant.HollowCandles)
        variant("OHLC bars", CandlestickVariant.OhlcBars)
        variant("Heikin-Ashi", CandlestickVariant.HeikinAshi)
    }
}
