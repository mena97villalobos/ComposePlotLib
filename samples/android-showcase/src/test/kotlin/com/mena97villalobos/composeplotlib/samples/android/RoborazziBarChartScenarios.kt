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

/**
 * The canvas the bar scenario actually composes into, measured under Robolectric: the default screen is
 * 320x470 at density 1, and `Modifier.size(400.dp, 720.dp)` on the wrapping Surface is coerced into it.
 *
 * Unlike line/scatter, a bar selection is bar-*rectangle* geometry (`markerAnchor`, `hitBounds`) that has
 * no domain-point equivalent, so it must be planned. Planning against the wrong size produced `hitBounds`
 * that missed the real plot rect, and BarChart's selection-revalidation effect then silently cleared the
 * selection — which is why this golden used to render neither an accent nor a marker.
 */
private val roborazziBarCanvasSize = SizeD(320.0, 470.0)

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
