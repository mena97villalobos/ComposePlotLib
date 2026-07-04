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
import com.mena97villalobos.composeplotlib.chart.CirclePackChart
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.CirclePackChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CirclePackNode

@Composable
fun CirclePackChartShowcaseScreen() {
    val spec =
        run {
            val data =
                listOf(
                    "Kotlin" to 48.0,
                    "Swift" to 32.0,
                    "TypeScript" to 40.0,
                    "Python" to 55.0,
                    "Go" to 24.0,
                    "Rust" to 20.0,
                    "Java" to 30.0,
                    "C++" to 18.0,
                    "Ruby" to 10.0,
                    "Dart" to 14.0,
                )
            CirclePackChartSpec(
                nodes =
                    data.mapIndexed { index, (label, value) ->
                        CirclePackNode(
                            id = label,
                            label = label,
                            value = value,
                            color = ChartPalettes.cyclic(ChartPalettes.Tableau10, index),
                        )
                    },
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Circle-packing showcase. Language popularity as packed area-proportional bubbles."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Circle-Packing Showcase", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "Each bubble's area is proportional to its value; a greedy tangent packing places them without overlap " +
                    "and fits the group to the plot. Labels show when the bubble is large enough.",
            style = MaterialTheme.typography.bodyLarge,
        )
        CirclePackChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
