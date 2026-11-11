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
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisBreak
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle

@Composable
fun BrokenAxisShowcaseScreen() {
    val spec =
        remember {
            // Values cluster near 0–20 and near 980–1000, with nothing in between.
            val points =
                listOf(
                    0.0 to 5.0,
                    1.0 to 12.0,
                    2.0 to 8.0,
                    3.0 to 18.0,
                    4.0 to 15.0,
                    5.0 to 990.0,
                    6.0 to 998.0,
                    7.0 to 985.0,
                    8.0 to 995.0,
                    9.0 to 988.0,
                ).map { (x, y) -> ChartPoint(x, y) }
            LineChartSpec(
                series =
                    listOf(
                        LineSeries("v", "Value", points, lineStyle = LineStyle(color = ChartColor(0xFF4E79A7), strokeWidth = 2.0)),
                    ),
                yAxis = AxisSpec(scale = AxisScale.Broken(breaks = listOf(AxisBreak(from = 20.0, to = 980.0)))),
            )
        }
    val viewport = rememberChartViewportState(initialViewport = defaultViewport(spec))

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Broken axis showcase. A y-axis with a collapsed break between two far-apart data clusters."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Broken Axis", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "AxisScale.Broken collapses empty domain intervals: here a y-axis break from 20 to 980 compresses the " +
                    "gap between the low and high clusters to a sliver, so both stay readable in one plot. Ticks inside " +
                    "the break are dropped.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LineChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            viewportState = viewport,
        )
    }
}
