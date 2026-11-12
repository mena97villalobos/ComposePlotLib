package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.BarChart
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartDrillState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarStyle

@Composable
fun DrillDownShowcaseScreen() {
    // Level 0 = overview ("All"); deeper levels are a region name.
    val breakdown =
        remember {
            mapOf(
                "All" to listOf("North" to 120.0, "South" to 90.0, "East" to 150.0, "West" to 70.0),
                "North" to listOf("Q1" to 25.0, "Q2" to 30.0, "Q3" to 35.0, "Q4" to 30.0),
                "South" to listOf("Q1" to 20.0, "Q2" to 25.0, "Q3" to 22.0, "Q4" to 23.0),
                "East" to listOf("Q1" to 40.0, "Q2" to 38.0, "Q3" to 36.0, "Q4" to 36.0),
                "West" to listOf("Q1" to 15.0, "Q2" to 18.0, "Q3" to 20.0, "Q4" to 17.0),
            )
        }
    val drill = rememberChartDrillState(root = "All")
    val selection = rememberChartSelectionState()

    val data = breakdown[drill.current] ?: emptyList()
    val spec =
        remember(drill.current) {
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup(
                            id = drill.current,
                            label = drill.current,
                            entries = data.mapIndexed { index, (_, value) -> BarEntry(x = index.toDouble(), y = value) },
                            style = BarStyle(color = ChartColor(0xFF4E79A7)),
                        ),
                    ),
            )
        }
    val viewport = rememberChartViewportState(initialViewport = defaultViewport(spec))

    // Tap a bar at the overview level to drill into that region.
    LaunchedEffect(selection.selection, drill.current) {
        val hit = selection.selection ?: return@LaunchedEffect
        if (!drill.canDrillUp) {
            val label = data.getOrNull(hit.target.pointIndex)?.first
            if (label != null && breakdown.containsKey(label)) drill.drillDown(label)
        }
        selection.clear()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription = "Drill-down showcase. Tap a region bar to drill into its quarterly breakdown."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Drill-Down", style = MaterialTheme.typography.headlineMedium)
        Text(
            text =
                "ChartDrillState is a back-stack over levels. Tap a region bar to drill into its quarters; the Back " +
                    "button pops a level. Wire selection → drillDown; build each level's spec from drill.current.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(text = "Path: ${drill.path.joinToString(" › ")}", style = MaterialTheme.typography.titleSmall)
        BarChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(260.dp),
            viewportState = viewport,
            selectionState = selection,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
        )
        OutlinedButton(
            onClick = { drill.drillUp() },
            enabled = drill.canDrillUp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (drill.canDrillUp) "Back to overview" else "Tap a region bar to drill in")
        }
    }
}
