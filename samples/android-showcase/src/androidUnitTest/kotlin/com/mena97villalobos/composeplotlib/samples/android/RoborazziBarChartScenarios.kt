package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.BarChart
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.BarHitResolver
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyBarChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleBarChartSpec

private val roborazziBarCanvasSize = SizeD(400.0, 320.0)

internal fun roborazziBarSelectionHighlight(spec: BarChartSpec): ChartSelection {
    val vp = defaultViewport(spec)
    val plan =
        ChartPlanner.planBarChart(
            input =
                ChartLayoutInput(
                    canvasSize = roborazziBarCanvasSize,
                    viewport = vp,
                    labelLayoutScale = 1.0,
                ),
            spec = spec,
            selection = null,
        )
    val seg = plan.barSegmentsForHitTest().first()
    val touch =
        OffsetD(
            (seg.bounds.left + seg.bounds.right) / 2.0,
            (seg.bounds.top + seg.bounds.bottom) / 2.0,
        )
    return BarHitResolver.resolvePlanned(plan.barSegmentsForHitTest(), touch)!!
}

@Composable
fun RoborazziBarChartEmptyDefaultOverlay() {
    val spec = remember { emptyBarChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    MaterialTheme {
        BarChart(
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
fun RoborazziBarChartSelectionHighlighted() {
    val spec = remember { sampleBarChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    LaunchedEffect(Unit) {
        selectionState.select(roborazziBarSelectionHighlight(spec))
    }
    MaterialTheme {
        BarChart(
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
