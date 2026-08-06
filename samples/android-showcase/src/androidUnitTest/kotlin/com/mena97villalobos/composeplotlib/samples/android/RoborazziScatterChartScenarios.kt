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
import com.mena97villalobos.composeplotlib.chart.ScatterChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyScatterChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleScatterChartSpec

/** See the note on the line scenarios: naming the datum is the whole selection. */
private val UNPLACED = OffsetD(0.0, 0.0)

internal fun roborazziScatterSelectionHighlight(spec: ScatterChartSpec): ChartSelection {
    val series = spec.series.first { it.points.isNotEmpty() }
    val pointIndex = 0
    val pt = series.points[pointIndex]
    return ChartSelection(
        target =
            HighlightTarget(
                seriesId = series.id,
                pointIndex = pointIndex,
                point = XyPoint(pt.x, pt.y),
            ),
        pixelOffset = UNPLACED,
    )
}

@Composable
fun RoborazziScatterChartEmptyDefaultOverlay() {
    val spec = remember { emptyScatterChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    MaterialTheme {
        ScatterChart(
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
fun RoborazziScatterChartSelectionHighlighted() {
    val spec = remember { sampleScatterChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    LaunchedEffect(Unit) {
        selectionState.select(roborazziScatterSelectionHighlight(spec))
    }
    MaterialTheme {
        ScatterChart(
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
