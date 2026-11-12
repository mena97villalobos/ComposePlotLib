package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.FunnelChart
import com.mena97villalobos.composeplotlib.core.spec.FunnelChartSpec
import com.mena97villalobos.composeplotlib.core.spec.FunnelStage

@Composable
fun FunnelChartShowcaseScreen() {
    val spec =
        FunnelChartSpec(
            stages =
                listOf(
                    FunnelStage("Visits", 10000.0),
                    FunnelStage("Product views", 6200.0),
                    FunnelStage("Add to cart", 3100.0),
                    FunnelStage("Checkout", 1400.0),
                    FunnelStage("Purchase", 820.0),
                ),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Funnel chart showcase. A conversion funnel from visits to purchases."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Funnel Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "A conversion funnel: each stage's width is proportional to its value, tapering from visits down to " +
                    "purchases. Stages are colored from the Tableau-10 palette with centered labels.",
            style = MaterialTheme.typography.bodyLarge,
        )
        FunnelChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(top = 8.dp),
        )
    }
}
