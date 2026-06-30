package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.RequestFocusWhenChartMarkerShown
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.chartMarkerFocusContainer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.heatmapChartAccessibility
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.rememberChartMarkerFocusRequester
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberAnimatedChartViewport
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberSelectionMarkerAppearance
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.layout.measuredLayoutInput
import com.mena97villalobos.composeplotlib.chart.internal.marker.ChartMarkerOverlay
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildHeatmapMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.render.HeatmapChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.HeatmapHitResolver
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec

/**
 * Heatmap chart: a 2-D grid of colored cells where each cell's color encodes a numeric value via
 * [HeatmapChartSpec.colorScale]. Column labels appear on the X axis; row labels on the Y axis
 * (row 0 at the bottom).
 *
 * Tap a cell to select it; a [marker] overlay will appear if provided. Pan and zoom are disabled by
 * default ([ChartGestureConfig.PieDefault]) since all data is visible at once.
 *
 * @param spec Immutable description of cells, labels, color scale, and axis styling.
 * @param modifier Layout modifier for the outer [Box].
 * @param viewportState Pan/zoom state; default uses [defaultViewport] which fits all cells.
 * @param selectionState Tap selection state.
 * @param gestureConfig Tap, transform, and fling policies; defaults to tap-only (no pan/zoom).
 * @param marker Optional composable tooltip shown above the selected cell; receives [ChartMarkerData].
 * @param markerPlacement Flip/slide placement for the marker overlay; see [ChartMarkerPlacement].
 * @param noData Custom empty-state composable shown when [HeatmapChartSpec.cells] is empty.
 * @param noDataUi Structured empty copy used when [noData] is null.
 * @param accessibilityCustomActions Optional TalkBack / VoiceOver custom actions.
 * @param animation Marker fade-in and viewport animation policy.
 * @param chromeTheme Plot frame, legend panel, and clipping; defaults to Material-aware [chartChromeTheme].
 */
@Composable
fun HeatmapChart(
    spec: HeatmapChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
    selectionState: ChartSelectionState = rememberChartSelectionState(),
    gestureConfig: ChartGestureConfig = ChartGestureConfig.PieDefault,
    marker: (@Composable BoxScope.(ChartMarkerData) -> Unit)? = null,
    markerPlacement: ChartMarkerPlacement = ChartMarkerPlacement.Default,
    noData: (@Composable BoxScope.() -> Unit)? = null,
    noDataUi: ChartNoDataUi? = null,
    accessibilityCustomActions: List<ChartCustomAccessibilityAction> = emptyList(),
    animation: ChartAnimationPolicy = ChartAnimationPolicy.Default,
    chromeTheme: ChartChromeTheme? = null,
) {
    val resolvedAnimation = resolveChartAnimationPolicy(animation)
    val renderViewport = rememberAnimatedChartViewport(viewportState, resolvedAnimation)
    val textMeasurer = rememberTextMeasurer()
    val labelStyles = chartLabelTextStyles(spec)
    val chromeResolved = chromeTheme ?: chartChromeTheme()
    val renderStyles =
        remember(spec, labelStyles, chromeResolved) {
            ChartRenderStyles(labelStyles, chromeResolved)
        }
    val emptyStateDescription: String? =
        if (spec.cells.isNotEmpty()) {
            null
        } else {
            when {
                noData != null -> null
                noDataUi != null ->
                    noDataUi.contentDescription
                        ?: listOfNotNull(noDataUi.title, noDataUi.message).joinToString(". ")

                else -> null
            }
        }

    var canvasSize by remember { mutableStateOf(SizeD.Zero) }
    val fontScale = LocalDensity.current.fontScale

    val xAxisWithCat =
        remember(spec.xAxis, spec.columnLabels) {
            spec.xAxis.copy(scale = AxisScale.Category(spec.columnLabels))
        }
    val yAxisWithCat =
        remember(spec.yAxis, spec.rowLabels) {
            spec.yAxis.copy(scale = AxisScale.Category(spec.rowLabels))
        }
    val cartesianTickPass =
        remember(xAxisWithCat, yAxisWithCat, renderViewport) {
            ChartPlanner.computeCartesianTickValuePass(renderViewport, xAxisWithCat, yAxisWithCat)
        }

    val plan =
        remember(
            spec,
            renderViewport,
            selectionState.selection,
            canvasSize,
            textMeasurer,
            labelStyles,
            fontScale,
            cartesianTickPass,
        ) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                val baseInput = chartLayoutBaseInput(canvasSize, renderViewport, fontScale)
                val preliminaryPlan =
                    ChartPlanner.planHeatmapChart(
                        input = baseInput,
                        spec = spec,
                        selection = selectionState.selection,
                        tickValuePass = cartesianTickPass,
                    )
                ChartPlanner.planHeatmapChart(
                    input =
                        measuredLayoutInput(
                            baseInput,
                            preliminaryPlan.layout,
                            textMeasurer,
                            ChartRenderStyles(labelStyles, chromeResolved),
                        ),
                    spec = spec,
                    selection = selectionState.selection,
                    tickValuePass = cartesianTickPass,
                )
            }
        }

    val markerFocusRequester = rememberChartMarkerFocusRequester()
    val latestPlan = rememberUpdatedState(plan)
    RequestFocusWhenChartMarkerShown(
        enabled = spec.accessibility.requestFocusOnMarker && marker != null,
        selection = selectionState.selection,
        focusRequester = markerFocusRequester,
    )

    Box(
        modifier =
            modifier
                .heatmapChartAccessibility(
                    spec,
                    selectionState,
                    emptyStateDescription,
                    accessibilityCustomActions,
                ).onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }.chartGestures(
                    onTap =
                        if (gestureConfig.tap.selectionEnabled) {
                            { offset ->
                                val currentPlan = latestPlan.value ?: return@chartGestures
                                if (spec.cells.isEmpty()) {
                                    selectionState.clear()
                                    return@chartGestures
                                }
                                val sel =
                                    HeatmapHitResolver.resolve(
                                        touchPoint = OffsetD(offset.x.toDouble(), offset.y.toDouble()),
                                        cells = currentPlan.cellsForHitTest(),
                                        specId = spec.id,
                                    )
                                selectionState.select(sel)
                            }
                        } else {
                            null
                        },
                    onDoubleTap = null,
                    onTransform = null,
                    flingPolicy = gestureConfig.fling,
                    flingRuntime = null,
                    onFlingVelocity = null,
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentPlan = plan ?: return@Canvas
            if (spec.cells.isEmpty()) return@Canvas
            with(HeatmapChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        val markerData = selectionState.selection?.let { buildHeatmapMarkerData(spec, it) }
        val markerAppearance =
            rememberSelectionMarkerAppearance(
                markerData = markerData,
                selectionPresent = selectionState.selection != null,
                enabled = resolvedAnimation.selectionHighlightEnabled,
                durationMillis = resolvedAnimation.selectionDurationMillis,
            )
        val layoutForMarker = plan?.layout
        if (marker != null && markerAppearance.contentData != null && layoutForMarker != null) {
            val overlayData = markerAppearance.contentData
            val anchor = overlayData.selection.markerPosition()
            ChartMarkerOverlay(
                anchorX = anchor.x,
                anchorY = anchor.y,
                plotBounds = layoutForMarker.plotBounds,
                canvasBounds = layoutForMarker.canvasBounds,
                placement = markerPlacement,
                contentAlpha = markerAppearance.alpha,
                markerContainerModifier =
                    Modifier.chartMarkerFocusContainer(
                        enabled = spec.accessibility.requestFocusOnMarker,
                        focusRequester = markerFocusRequester,
                        contentDescription = overlayData.accessibilitySummaryText(),
                    ),
            ) {
                marker(overlayData)
            }
        }

        if (spec.cells.isEmpty()) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }
}
