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
import com.mena97villalobos.composeplotlib.core.indicators.TechnicalIndicators
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import kotlin.math.sin

@Composable
fun IndicatorsShowcaseScreen() {
    val spec =
        remember {
            val closes = (0 until 60).map { 50.0 + 12.0 * sin(it / 5.0) + (it % 7) - 3.0 }

            fun series(
                id: String,
                label: String,
                values: List<Double?>,
                color: Long,
                width: Double = 1.5,
            ): LineSeries =
                LineSeries(
                    id = id,
                    label = label,
                    points = values.mapIndexedNotNull { i, v -> v?.let { ChartPoint(i.toDouble(), it) } },
                    lineStyle = LineStyle(color = ChartColor(color), strokeWidth = width),
                )

            val bollinger = TechnicalIndicators.bollingerBands(closes, period = 20, stdDevMultiplier = 2.0)
            LineChartSpec(
                series =
                    listOf(
                        series("price", "Close", closes.map { it }, 0xFF4E79A7, width = 2.0),
                        series("sma", "SMA(10)", TechnicalIndicators.sma(closes, 10), 0xFFF28E2B),
                        series("ema", "EMA(10)", TechnicalIndicators.ema(closes, 10), 0xFF59A14F),
                        series("bbUpper", "BB upper", bollinger.upper, 0xFFB07AA1),
                        series("bbLower", "BB lower", bollinger.lower, 0xFFB07AA1),
                    ),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Technical indicators showcase. A price line overlaid with SMA, EMA, and Bollinger bands."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Technical Indicators",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "TechnicalIndicators computes SMA / EMA / RSI / MACD / Bollinger bands over a price series (index-aligned, " +
                    "null until defined). Map any result to a LineSeries to overlay it — here SMA(10), EMA(10), and Bollinger(20, 2σ).",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    }
}
