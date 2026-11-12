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
import com.mena97villalobos.composeplotlib.chart.BulletChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BulletBand
import com.mena97villalobos.composeplotlib.core.spec.BulletChartSpec

@Composable
fun BulletChartShowcaseScreen() {
    val bands =
        listOf(
            BulletBand(60.0, ChartColor(0xFFE0E0E0)),
            BulletBand(80.0, ChartColor(0xFFBDBDBD)),
            BulletBand(100.0, ChartColor(0xFF9E9E9E)),
        )
    val kpis =
        listOf(
            Triple("Revenue (% of goal)", 82.0, 90.0),
            Triple("Profit (% of goal)", 71.0, 80.0),
            Triple("Satisfaction", 95.0, 85.0),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Bullet graph showcase. KPI measures against qualitative bands and targets."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Bullet Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Bullet graphs pack a KPI into a compact row: qualitative bands (poor → good) behind a measure bar " +
                    "(the value) and a target marker (the red line). Read-only, ideal for dashboards.",
            style = MaterialTheme.typography.bodyLarge,
        )
        for ((label, value, target) in kpis) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$label — ${value.toInt()} (target ${target.toInt()})",
                    style = MaterialTheme.typography.titleSmall,
                )
                BulletChart(
                    spec =
                        BulletChartSpec(
                            value = value,
                            target = target,
                            domainMax = 100.0,
                            bands = bands,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 4.dp),
                )
            }
        }
    }
}
