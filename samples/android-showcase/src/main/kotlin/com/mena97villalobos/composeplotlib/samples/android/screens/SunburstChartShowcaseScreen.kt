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
import com.mena97villalobos.composeplotlib.chart.SunburstChart
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode

@Composable
fun SunburstChartShowcaseScreen() {
    val spec =
        SunburstChartSpec(
            root =
                SunburstNode(
                    label = "Company",
                    children =
                        listOf(
                            SunburstNode(
                                "Engineering",
                                children =
                                    listOf(
                                        SunburstNode("Platform", value = 12.0),
                                        SunburstNode("Product", value = 18.0),
                                        SunburstNode("Data", value = 8.0),
                                    ),
                            ),
                            SunburstNode(
                                "Sales",
                                children =
                                    listOf(
                                        SunburstNode("Enterprise", value = 14.0),
                                        SunburstNode("SMB", value = 9.0),
                                    ),
                            ),
                            SunburstNode(
                                "Operations",
                                children =
                                    listOf(
                                        SunburstNode("Finance", value = 6.0),
                                        SunburstNode("People", value = 5.0),
                                        SunburstNode("Legal", value = 3.0),
                                    ),
                            ),
                        ),
                ),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Sunburst showcase. Company headcount by division and team as concentric rings."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Sunburst Showcase", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "A hierarchy as concentric rings — depth by ring, angular span by value within each parent. The center " +
                    "is the root; top-level branches take palette colors and descendants inherit a lighter shade.",
            style = MaterialTheme.typography.bodyLarge,
        )
        SunburstChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
    }
}
