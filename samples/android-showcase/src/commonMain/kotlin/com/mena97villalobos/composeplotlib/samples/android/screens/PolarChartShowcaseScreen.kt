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
import com.mena97villalobos.composeplotlib.chart.PolarChart
import com.mena97villalobos.composeplotlib.core.spec.ColorGradient
import com.mena97villalobos.composeplotlib.core.spec.PolarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PolarSector

@Composable
fun PolarChartShowcaseScreen() {
    val spec =
        run {
            val directions = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
            val strengths = listOf(9.0, 6.0, 5.0, 4.0, 7.0, 5.0, 8.0, 11.0, 14.0, 10.0, 7.0, 5.0, 6.0, 4.0, 5.0, 7.0)
            PolarChartSpec(
                sectors =
                    directions.mapIndexed { index, dir ->
                        PolarSector(
                            label = dir,
                            value = strengths[index],
                            color = ColorGradient.Viridis.colorAt(strengths[index] / 14.0),
                        )
                    },
                maxValue = 14.0,
                gapDegrees = 1.5,
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Polar chart showcase. A 16-direction wind rose of average wind speed."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Polar / Wind-Rose Showcase", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "Each wedge is one direction; its radius encodes the value (average wind speed here) against concentric " +
                    "grid rings. Colors run along a Viridis gradient by magnitude.",
            style = MaterialTheme.typography.bodyLarge,
        )
        PolarChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
