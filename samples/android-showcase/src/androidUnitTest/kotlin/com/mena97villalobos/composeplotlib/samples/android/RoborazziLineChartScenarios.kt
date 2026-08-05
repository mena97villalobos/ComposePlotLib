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
import com.mena97villalobos.composeplotlib.chart.ChartNoDataDefaults
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleLineChartSpec

/**
 * Selects a point by identity only. The chart projects `target.point` with its own render transform,
 * so this deliberately carries no pixel geometry — a harness cannot know the composed canvas size, and
 * guessing one used to float the crosshair and marker off the series.
 */
internal fun roborazziLineSelectionHighlight(
    spec: LineChartSpec,
    pointIndex: Int = 3,
): ChartSelection {
    val series = spec.series.first { it.points.isNotEmpty() }
    val chartPoint = series.points[pointIndex]
    return ChartSelection(
        target =
            HighlightTarget(
                seriesId = series.id,
                pointIndex = pointIndex,
                point = XyPoint(chartPoint.x, chartPoint.y),
            ),
        pixelOffset = OffsetD(0.0, 0.0),
    )
}

/** Empty series: library default no-data overlay (no [com.mena97villalobos.composeplotlib.chart.ChartNoDataUi]). */
@Composable
fun RoborazziLineChartEmptyDefaultOverlay() {
    val spec = remember { emptyLineChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    MaterialTheme {
        LineChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.readOnly(),
            noDataUi = null,
            animation = ChartAnimationPolicy.Default,
        )
    }
}

/** Structured no-data preset ([ChartNoDataDefaults]). */
@Composable
fun RoborazziLineChartNoDataUiPreset() {
    val spec = remember { emptyLineChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    MaterialTheme {
        LineChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.readOnly(),
            noDataUi = ChartNoDataDefaults.lineChart(),
            animation = ChartAnimationPolicy.Default,
        )
    }
}

/** Deterministic tap selection + marker (animation off for stable pixels). */
@Composable
fun RoborazziLineChartSelectionHighlighted() {
    val spec = remember { sampleLineChartSpec() }
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = remember { ChartSelectionState() }
    LaunchedEffect(Unit) {
        selectionState.select(roborazziLineSelectionHighlight(spec))
    }
    MaterialTheme {
        LineChart(
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
