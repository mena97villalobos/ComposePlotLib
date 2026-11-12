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
import com.mena97villalobos.composeplotlib.chart.ParallelCoordinatesChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.ParallelAxis
import com.mena97villalobos.composeplotlib.core.spec.ParallelCoordinatesChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ParallelSeries

@Composable
fun ParallelCoordinatesShowcaseScreen() {
    val spec =
        run {
            val axes =
                listOf(
                    ParallelAxis("MPG", 10.0, 45.0),
                    ParallelAxis("Cyl", 3.0, 8.0),
                    ParallelAxis("HP", 50.0, 340.0),
                    ParallelAxis("Weight", 1500.0, 5200.0),
                    ParallelAxis("0-60s", 8.0, 25.0),
                )
            val cars =
                listOf(
                    Triple("Compact", ChartColor(0xFF4E79A7), listOf(38.0, 4.0, 90.0, 2200.0, 12.0)),
                    Triple("Sedan", ChartColor(0xFF59A14F), listOf(28.0, 4.0, 150.0, 3100.0, 10.0)),
                    Triple("Sports", ChartColor(0xFFE15759), listOf(20.0, 8.0, 320.0, 3400.0, 8.5)),
                    Triple("SUV", ChartColor(0xFFF28E2B), listOf(18.0, 6.0, 240.0, 4600.0, 11.0)),
                    Triple("Truck", ChartColor(0xFFB07AA1), listOf(15.0, 8.0, 280.0, 5000.0, 13.0)),
                )
            ParallelCoordinatesChartSpec(
                axes = axes,
                series = cars.map { (label, color, values) -> ParallelSeries(label, label, values, color, strokeWidth = 2.0) },
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Parallel coordinates showcase. Car classes compared across five specifications."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Parallel Coordinates", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "Each vertical axis is one dimension with its own range; each polyline is one record, crossing every " +
                    "axis at its value. Crossing vs. parallel lines reveal correlations across dimensions.",
            style = MaterialTheme.typography.bodyLarge,
        )
        ParallelCoordinatesChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(300.dp),
        )
    }
}
