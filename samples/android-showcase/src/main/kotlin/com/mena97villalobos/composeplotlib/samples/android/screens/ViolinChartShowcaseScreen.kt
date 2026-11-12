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
import com.mena97villalobos.composeplotlib.chart.ViolinChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.ViolinCategory
import com.mena97villalobos.composeplotlib.core.spec.ViolinChartSpec
import kotlin.math.sin

@Composable
fun ViolinChartShowcaseScreen() {
    val spec =
        remember {
            // Deterministic pseudo-samples with different shapes per group.
            fun samples(
                seed: Int,
                center: Double,
                spread: Double,
            ): List<Double> =
                (0 until 120).map { i ->
                    val u = ((i * 2654435761u.toLong() + seed * 40503L) ushr 10).toInt() and 0xFFF
                    val jitter = (u / 4095.0 - 0.5)
                    center + spread * (sin((i + seed) / 7.0) + 2.0 * jitter)
                }
            ViolinChartSpec(
                categories =
                    listOf(
                        ViolinCategory("Control", samples(1, 50.0, 8.0), ChartColor(0xFF4E79A7)),
                        ViolinCategory("Variant A", samples(2, 58.0, 12.0), ChartColor(0xFF59A14F)),
                        ViolinCategory("Variant B", samples(3, 46.0, 6.0), ChartColor(0xFFF28E2B)),
                    ),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Violin chart showcase. Three sampled distributions compared by shape."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Violin Showcase", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "Each violin is a mirrored kernel-density estimate of a group's samples along the value axis — wider where " +
                    "values cluster. The short line marks each group's median. A shape-focused alternative to the box plot.",
            style = MaterialTheme.typography.bodyLarge,
        )
        ViolinChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
