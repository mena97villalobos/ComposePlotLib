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
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.TreemapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.TreemapNode

@Composable
fun TreemapChartShowcaseScreen() {
    val spec =
        run {
            val data =
                listOf(
                    "Cloud" to 42.0,
                    "Devices" to 28.0,
                    "Services" to 20.0,
                    "Ads" to 16.0,
                    "Gaming" to 11.0,
                    "Other" to 7.0,
                    "Wearables" to 5.0,
                    "Auto" to 3.0,
                )
            TreemapChartSpec(
                nodes =
                    data.mapIndexed { index, (label, value) ->
                        TreemapNode(
                            id = label,
                            label = label,
                            value = value,
                            color = ChartPalettes.cyclic(ChartPalettes.Tableau10, index),
                        )
                    },
                labelColor = ChartColor.White,
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Treemap showcase. Revenue by segment as area-proportional tiles."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Treemap Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Each tile's area is proportional to its value, arranged by a squarified layout that keeps tiles close " +
                    "to square. Labels are drawn when a tile is large enough.",
            style = MaterialTheme.typography.bodyLarge,
        )
        TreemapChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
