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
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.pieChartAccessibility
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.rememberChartMarkerFocusRequester
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberAnimatedChartViewport
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberSelectionMarkerAppearance
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartPieKeyboardSelectionRoam
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.layout.measuredLayoutInput
import com.mena97villalobos.composeplotlib.chart.internal.marker.ChartMarkerOverlay
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildPieMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.render.PieChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.PieHitResolver
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.hasDrawableSlices

/**
 * Pie / donut chart: radial layout via [ChartPlanner.planPieChart], tap slice selection, optional marker overlay.
 *
 * @param markerPlacement same flip/slide containment as cartesian charts ([ChartMarkerPlacement] defaults to plot bounds).
 *
 * Pan/zoom are **off** by default ([ChartGestureConfig.transform] disabled) because this family does not use the
 * cartesian viewport. You can still enable transforms for future extensions; they only affect the dummy
 * [ChartViewportState] passed into layout measurement.
 */
@Composable
fun PieChart(
    spec: PieChartSpec,
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
        if (spec.hasDrawableSlices()) {
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
    val plan =
        remember(spec, renderViewport, selectionState.selection, canvasSize, textMeasurer, labelStyles, fontScale) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                val baseInput = chartLayoutBaseInput(canvasSize, renderViewport, fontScale)
                val preliminaryPlan =
                    ChartPlanner.planPieChart(
                        input = baseInput,
                        spec = spec,
                        selection = selectionState.selection,
                    )
                ChartPlanner.planPieChart(
                    input =
                        measuredLayoutInput(
                            baseInput,
                            preliminaryPlan.layout,
                            textMeasurer,
                            ChartRenderStyles(labelStyles, chromeResolved),
                            pieChartSpec = spec,
                        ),
                    spec = spec,
                    selection = selectionState.selection,
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
                .pieChartAccessibility(
                    spec,
                    selectionState,
                    emptyStateDescription,
                    accessibilityCustomActions,
                )
                .chartPieKeyboardSelectionRoam(
                    enabled = spec.accessibility.keyboardSelectionRoamEnabled,
                    spec = spec,
                    selectionState = selectionState,
                    tapSelectionEnabled = gestureConfig.tap.selectionEnabled,
                    layoutPlan = { latestPlan.value?.layout },
                )
                .onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }
                .chartGestures(
                    onTap =
                        if (gestureConfig.tap.selectionEnabled) {
                            { offset ->
                                val currentPlan = plan ?: return@chartGestures
                                if (!spec.hasDrawableSlices()) {
                                    selectionState.clear()
                                    return@chartGestures
                                }
                                val touch = OffsetD(offset.x.toDouble(), offset.y.toDouble())
                                val sel = PieHitResolver.resolve(touch, spec, currentPlan.layout.plotBounds)
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
            if (!spec.hasDrawableSlices()) return@Canvas
            with(PieChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        val markerData = selectionState.selection?.let { buildPieMarkerData(spec, it) }
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

        if (!spec.hasDrawableSlices()) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }
}
