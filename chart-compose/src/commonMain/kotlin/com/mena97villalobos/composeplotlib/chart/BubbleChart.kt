package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.internal.canvas.toComposeColor
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BubbleChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BubbleSizeScale

/**
 * Bubble chart: a scatter plot where each point's **size** encodes a third value ([BubblePoint.z]) via an
 * area-proportional [BubbleSizeScale]. Rendering, gestures, selection, and markers reuse [ScatterChart] (through
 * [BubbleChartSpec.toScatterSpec]); an optional **size legend** is overlaid.
 *
 * @param spec immutable bubble description (series, size scale, axes, grid, legend)
 * @param modifier layout modifier for the outer [Box]
 * @param viewportState pan/zoom state; default uses [defaultViewport] of [spec]
 * @param selectionState tap selection (bubbles resolve like scatter points)
 * @param gestureConfig tap / transform / fling policies
 * @param marker optional composable overlay at the selected bubble
 * @param markerPlacement flip/slide tooltip placement
 * @param noData custom empty state when every series has no points
 * @param noDataUi structured empty copy when [noData] is null
 * @param accessibilityCustomActions optional TalkBack / VoiceOver actions
 * @param animation optional programmatic viewport easing and marker fade-in
 * @param chromeTheme plot frame, legend panel, and clipping
 * @param showSizeLegend when true (default), overlays a size legend derived from [BubbleChartSpec.sizeScale]
 * @param sizeLegendValueFormatter formats size-legend value labels
 */
@Composable
fun BubbleChart(
    spec: BubbleChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
    selectionState: ChartSelectionState = rememberChartSelectionState(),
    gestureConfig: ChartGestureConfig = ChartGestureConfig(),
    marker: (@Composable BoxScope.(ChartMarkerData) -> Unit)? = null,
    markerPlacement: ChartMarkerPlacement = ChartMarkerPlacement.Default,
    noData: (@Composable BoxScope.() -> Unit)? = null,
    noDataUi: ChartNoDataUi? = null,
    accessibilityCustomActions: List<ChartCustomAccessibilityAction> = emptyList(),
    animation: ChartAnimationPolicy = ChartAnimationPolicy.Default,
    chromeTheme: ChartChromeTheme? = null,
    showSizeLegend: Boolean = true,
    sizeLegendValueFormatter: (Double) -> String = { defaultBubbleSizeLabel(it) },
) {
    val scatterSpec = remember(spec) { spec.toScatterSpec() }
    Box(modifier = modifier) {
        ScatterChart(
            spec = scatterSpec,
            modifier = Modifier.fillMaxSize(),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = gestureConfig,
            marker = marker,
            markerPlacement = markerPlacement,
            noData = noData,
            noDataUi = noDataUi,
            accessibilityCustomActions = accessibilityCustomActions,
            animation = animation,
            chromeTheme = chromeTheme,
        )
        if (showSizeLegend && spec.series.any { it.points.isNotEmpty() }) {
            val swatchColor = spec.series.firstOrNull()?.color ?: ChartColor.Blue
            BubbleSizeLegend(
                sizeScale = spec.sizeScale,
                swatchColor = swatchColor,
                valueFormatter = sizeLegendValueFormatter,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
    }
}

@Composable
private fun BubbleSizeLegend(
    sizeScale: BubbleSizeScale,
    swatchColor: ChartColor,
    valueFormatter: (Double) -> String,
    modifier: Modifier = Modifier,
) {
    val ticks = sizeScale.legendTicks(3).reversed() // largest first
    if (ticks.isEmpty()) return
    val fill = swatchColor.toComposeColor().copy(alpha = 0.55f)
    val density = LocalDensity.current
    Column(
        modifier = modifier.semantics { contentDescription = "Bubble size legend" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = "Size", style = MaterialTheme.typography.labelSmall)
        for ((value, radiusPx) in ticks) {
            val diameter = with(density) { (2.0 * radiusPx).toFloat().toDp() }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(diameter)
                            .clip(CircleShape)
                            .background(fill),
                )
                Spacer(Modifier.width(6.dp))
                Text(text = valueFormatter(value), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun defaultBubbleSizeLabel(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        (kotlin.math.round(value * 10.0) / 10.0).toString()
    }
