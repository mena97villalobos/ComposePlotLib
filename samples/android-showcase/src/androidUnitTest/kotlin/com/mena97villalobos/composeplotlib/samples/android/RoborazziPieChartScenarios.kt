package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.PieChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.PieHitResolver
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyPieChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.samplePieChartSpec

private val roborazziPieCanvasSize = SizeD(400.0, 320.0)

internal fun roborazziPieFirstSliceSelection(spec: PieChartSpec): ChartSelection {
    val vp = defaultViewport(spec)
    val plan =
        ChartPlanner.planPieChart(
            input =
                ChartLayoutInput(
                    canvasSize = roborazziPieCanvasSize,
                    viewport = vp,
                    labelLayoutScale = 1.0,
                ),
            spec = spec,
            selection = null,
        )
    return PieHitResolver.selectionForContributingOrdinal(
        spec = spec,
        plotBounds = plan.layout.plotBounds,
        current = null,
        delta = 1,
    )!!
}

@Composable
fun RoborazziPieChartEmptyDefaultOverlay() {
    val spec = remember { emptyPieChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    MaterialTheme {
        PieChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.readOnly(),
            animation = ChartAnimationPolicy.Default,
        )
    }
}

@Composable
fun RoborazziPieChartSelectionHighlighted() {
    val spec = remember { samplePieChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    LaunchedEffect(Unit) {
        selectionState.select(roborazziPieFirstSliceSelection(spec))
    }
    MaterialTheme {
        PieChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.readOnly(),
            marker = { markerData -> DefaultChartMarker(markerData) },
            animation =
                ChartAnimationPolicy(
                    selectionHighlightEnabled = false,
                    selectionDurationMillis = 0,
                ),
        )
    }
}
