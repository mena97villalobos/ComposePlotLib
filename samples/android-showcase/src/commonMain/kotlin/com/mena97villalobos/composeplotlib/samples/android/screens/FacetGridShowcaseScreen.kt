package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.mena97villalobos.composeplotlib.chart.ChartFacetGrid
import com.mena97villalobos.composeplotlib.chart.Sparkline
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import kotlin.math.sin

private data class RegionSeries(
    val name: String,
    val points: List<ChartPoint>,
)

@Composable
fun FacetGridShowcaseScreen() {
    val regions =
        remember {
            listOf("North", "South", "East", "West", "Central", "Online").mapIndexed { r, name ->
                RegionSeries(
                    name = name,
                    points = (0 until 24).map { i -> ChartPoint(i.toDouble(), 50.0 + 20.0 * sin((i + r * 3) / 4.0) + (i % 5)) },
                )
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Faceting showcase. Small-multiple sparklines of sales per region on a shared scale."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Faceting / Small Multiples", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "ChartFacetGrid lays out one chart per item in a responsive grid with per-facet titles. Here six regions " +
                    "share a common y range (via a fixed sparkline scale) so their trends are directly comparable.",
            style = MaterialTheme.typography.bodyLarge,
        )
        ChartFacetGrid(
            items = regions,
            columns = 2,
            facetHeight = 90.dp,
            facetTitle = { it.name },
        ) { region ->
            Sparkline(
                points = region.points,
                modifier = Modifier.fillMaxSize(),
                color = ChartColor(0xFF4E79A7),
                areaFill = ChartColor(0x334E79A7),
            )
        }
    }
}
