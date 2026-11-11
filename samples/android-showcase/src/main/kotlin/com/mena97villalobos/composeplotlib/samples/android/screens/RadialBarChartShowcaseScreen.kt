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
import com.mena97villalobos.composeplotlib.chart.RadialBarChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.RadialBarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadialBarTrack

@Composable
fun RadialBarChartShowcaseScreen() {
    val spec =
        RadialBarChartSpec(
            tracks =
                listOf(
                    RadialBarTrack("Network", 88.0, ChartColor(0xFF4E79A7)),
                    RadialBarTrack("CPU", 72.0, ChartColor(0xFFF28E2B)),
                    RadialBarTrack("Memory", 55.0, ChartColor(0xFF59A14F)),
                    RadialBarTrack("Disk", 40.0, ChartColor(0xFFE15759)),
                ),
            maxValue = 100.0,
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Radial bar chart showcase. Concentric progress rings for four utilization metrics."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Radial Bar Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Each concentric ring is a RadialBarTrack whose arc fills value / maxValue (here 0–100%). " +
                    "The outermost ring is the first track. Read-only, like the gauge — the legend labels the rings.",
            style = MaterialTheme.typography.bodyLarge,
        )
        RadialBarChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
