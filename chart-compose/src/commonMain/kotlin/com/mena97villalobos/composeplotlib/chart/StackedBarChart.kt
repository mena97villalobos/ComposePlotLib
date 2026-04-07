package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.core.spec.StackedBarChartSpec

/**
 * Stacked bar chart: same pipeline as [BarChart] with [StackedBarChartSpec.toBarChartSpec] (always stacked when valid).
 *
 * @see BarChart for parameters
 */
@Composable
fun StackedBarChart(
    spec: StackedBarChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState =
        rememberChartViewportState(initialViewport = defaultViewport(spec.toBarChartSpec())),
    selectionState: ChartSelectionState = rememberChartSelectionState(),
    gestureConfig: ChartGestureConfig = ChartGestureConfig(),
    marker: (@Composable BoxScope.(ChartMarkerData) -> Unit)? = null,
    markerPlacement: ChartMarkerPlacement = ChartMarkerPlacement.Default,
    noData: (@Composable BoxScope.() -> Unit)? = null,
    noDataUi: ChartNoDataUi? = null,
    accessibilityCustomActions: List<ChartCustomAccessibilityAction> = emptyList(),
    animation: ChartAnimationPolicy = ChartAnimationPolicy.Default,
    chromeTheme: ChartChromeTheme? = null,
) {
    BarChart(
        spec = spec.toBarChartSpec(),
        modifier = modifier,
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
}
