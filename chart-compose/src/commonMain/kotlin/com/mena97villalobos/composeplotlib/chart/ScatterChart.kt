package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.RequestFocusWhenChartMarkerShown
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.chartMarkerFocusContainer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.rememberChartMarkerFocusRequester
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.scatterChartAccessibility
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberAnimatedChartViewport
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberSelectionMarkerAppearance
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.ChartFlingRuntime
import com.mena97villalobos.composeplotlib.chart.internal.gesture.cartesianDoubleTapViewportEnabled
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartCartesianKeyboardViewport
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartScatterKeyboardSelectionRoam
import com.mena97villalobos.composeplotlib.chart.internal.gesture.effectiveTransformFlags
import com.mena97villalobos.composeplotlib.chart.internal.gesture.handleCartesianChartDoubleTapViewport
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.layout.measuredLayoutInput
import com.mena97villalobos.composeplotlib.chart.internal.marker.ChartMarkerOverlay
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildScatterMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.render.ScatterChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.NearestPointResolver
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.visible.ScatterPlotPoints
import com.mena97villalobos.composeplotlib.core.visible.ScatterRenderPipeline
import kotlin.math.max

/**
 * Scatter chart: draws [spec] as point markers only (no connecting segments), plans layout via [ChartPlanner], and wires
 * gestures to [viewportState] / [selectionState].
 *
 * @param spec immutable description of point series, axes, grid, and legend
 * @param modifier layout modifier for the outer [Box]
 * @param viewportState pan/zoom state; default uses [defaultViewport] of [spec]
 * @param selectionState tap selection; cleared when the selected point leaves the viewport
 * @param gestureConfig tap (selection), transform (pan/zoom/double-tap zoom), and optional fling policies
 * @param marker optional composable overlay at the selected point; receives [ChartMarkerData]
 * @param markerPlacement flip/slide tooltip placement; see [ChartMarkerPlacement]
 * @param noData custom empty state when every series has no points; if non-null, takes precedence over [noDataUi]
 * @param noDataUi structured empty copy when [noData] is null; otherwise a plain [DefaultNoDataOverlay]
 * @param accessibilityCustomActions optional TalkBack / VoiceOver actions (e.g. reset viewport)
 * @param animation optional programmatic viewport easing and marker fade-in; [ChartAnimationPolicy.respectSystemReducedMotion] is applied first
 * @param chromeTheme plot frame, legend panel, and clipping; defaults to Material-aware [chartChromeTheme]
 */
@Composable
fun ScatterChart(
    spec: ScatterChartSpec,
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
        if (!spec.series.all { it.points.isEmpty() }) {
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
    val transformFlags = effectiveTransformFlags(gestureConfig, viewportState.axisPolicy)
    val coroutineScope = rememberCoroutineScope()
    val flingRuntime = remember(coroutineScope) { ChartFlingRuntime(coroutineScope) }

    val cartesianTickPass =
        remember(spec.xAxis, spec.yAxis, renderViewport) {
            ChartPlanner.computeCartesianTickValuePass(renderViewport, spec.xAxis, spec.yAxis)
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
                val specPass1 = spec.withPlanningScatterRenderBudget(canvasSize, plotWidthPx = null)
                val preliminaryPlan =
                    ChartPlanner.planScatterChart(
                        input = baseInput,
                        spec = specPass1,
                        selection = selectionState.selection,
                        tickValuePass = cartesianTickPass,
                    )
                val specPass2 =
                    spec.withPlanningScatterRenderBudget(
                        canvasSize,
                        plotWidthPx = preliminaryPlan.layout.plotBounds.width,
                    )
                ChartPlanner.planScatterChart(
                    input =
                        measuredLayoutInput(
                            baseInput,
                            preliminaryPlan.layout,
                            textMeasurer,
                            ChartRenderStyles(labelStyles, chromeResolved),
                        ),
                    spec = specPass2,
                    selection = selectionState.selection,
                    tickValuePass = cartesianTickPass,
                )
            }
        }

    val latestPlan = rememberUpdatedState(plan)
    val markerFocusRequester = rememberChartMarkerFocusRequester()
    RequestFocusWhenChartMarkerShown(
        enabled = spec.accessibility.requestFocusOnMarker && marker != null,
        selection = selectionState.selection,
        focusRequester = markerFocusRequester,
    )

    Box(
        modifier =
            modifier
                .scatterChartAccessibility(
                    spec,
                    selectionState,
                    emptyStateDescription,
                    accessibilityCustomActions,
                ).chartScatterKeyboardSelectionRoam(
                    enabled = spec.accessibility.keyboardSelectionRoamEnabled,
                    spec = spec,
                    renderViewport = renderViewport,
                    selectionState = selectionState,
                    tapSelectionEnabled = gestureConfig.tap.selectionEnabled,
                    layoutPlan = { latestPlan.value?.layout },
                ).chartCartesianKeyboardViewport(
                    enabled = spec.accessibility.keyboardViewportNudgeEnabled,
                    transformEnabled = gestureConfig.transform.enabled,
                    flags = transformFlags,
                    viewportState = viewportState,
                    tapSelectionEnabled = gestureConfig.tap.selectionEnabled,
                    onClearSelection = { selectionState.clear() },
                    reserveShiftDirectionKeysForSelectionRoam = spec.accessibility.keyboardSelectionRoamEnabled,
                ).onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }.chartGestures(
                    onTap =
                        if (gestureConfig.tap.selectionEnabled) {
                            { offset ->
                                val currentPlan = plan ?: return@chartGestures
                                if (spec.series.all { it.points.isEmpty() }) {
                                    selectionState.clear()
                                    return@chartGestures
                                }
                                val transform =
                                    ChartTransform.forPlotBounds(
                                        viewport = renderViewport,
                                        plotBounds = currentPlan.layout.plotBounds,
                                    )
                                val touch = OffsetD(offset.x.toDouble(), offset.y.toDouble())
                                val specForHit =
                                    spec.withPlanningScatterRenderBudget(
                                        canvasSize,
                                        plotWidthPx = currentPlan.layout.plotBounds.width,
                                    )
                                val selection =
                                    specForHit.series
                                        .mapNotNull { series ->
                                            val rPx = series.pointStyle.radius.coerceAtLeast(0.0)
                                            val indexed =
                                                if (specForHit.scatterRender.maxPointsPerSeries <= 0) {
                                                    ScatterPlotPoints.indexedInPlotBounds(
                                                        series.points,
                                                        transform,
                                                        currentPlan.layout.plotBounds,
                                                        radiusPx = rPx,
                                                    )
                                                } else {
                                                    val prepared =
                                                        ScatterRenderPipeline.prepareIndexed(
                                                            series.toXyPoints(),
                                                            renderViewport,
                                                            specForHit.scatterRender,
                                                        )
                                                    ScatterPlotPoints.filterPreparedToPlotDisc(
                                                        prepared,
                                                        transform,
                                                        currentPlan.layout.plotBounds,
                                                        radiusPx = rPx,
                                                    )
                                                }
                                            if (indexed.isEmpty()) return@mapNotNull null
                                            NearestPointResolver.resolveIndexed(
                                                seriesId = series.id,
                                                indexedPoints = indexed,
                                                touchPoint = touch,
                                                transform = transform,
                                                maxDistancePx = 28.0,
                                            )
                                        }.minByOrNull { selectionDistance(it.pixelOffset, offset) }
                                selectionState.select(selection)
                            }
                        } else {
                            null
                        },
                    onDoubleTap =
                        if (cartesianDoubleTapViewportEnabled(gestureConfig)) {
                            { offset ->
                                handleCartesianChartDoubleTapViewport(
                                    offset = offset,
                                    gestureConfig = gestureConfig,
                                    transformFlags = transformFlags,
                                    viewportState = viewportState,
                                    renderViewport = renderViewport,
                                    layoutPlan = plan?.layout,
                                )
                            }
                        } else {
                            null
                        },
                    onTransform =
                        if (gestureConfig.transform.enabled) {
                            { centroid, pan, zoom ->
                                val currentPlan = plan ?: return@chartGestures
                                if (!currentPlan.layout.plotBounds.contains(OffsetD(centroid.x.toDouble(), centroid.y.toDouble()))) {
                                    return@chartGestures
                                }

                                val transform =
                                    ChartTransform.forPlotBounds(
                                        viewport = renderViewport,
                                        plotBounds = currentPlan.layout.plotBounds,
                                    )
                                val pivot = transform.toDomain(OffsetD(centroid.x.toDouble(), centroid.y.toDouble()))
                                val plotWidth = max(currentPlan.layout.plotBounds.width, 1.0)
                                val plotHeight = max(currentPlan.layout.plotBounds.height, 1.0)
                                val deltaX =
                                    if (transformFlags.panX) {
                                        -(pan.x.toDouble() / plotWidth) * renderViewport.xRange.span
                                    } else {
                                        0.0
                                    }
                                val deltaY =
                                    if (transformFlags.panY) {
                                        (pan.y.toDouble() / plotHeight) * renderViewport.yRange.span
                                    } else {
                                        0.0
                                    }
                                val scaleX = if (transformFlags.zoomX) zoom.toDouble() else 1.0
                                val scaleY = if (transformFlags.zoomY) zoom.toDouble() else 1.0

                                viewportState.pan(deltaX, deltaY)
                                viewportState.zoom(
                                    scaleX = scaleX,
                                    scaleY = scaleY,
                                    pivotX = pivot.x,
                                    pivotY = pivot.y,
                                )
                            }
                        } else {
                            null
                        },
                    flingPolicy = gestureConfig.fling,
                    flingRuntime =
                        if (gestureConfig.transform.enabled && gestureConfig.fling.panFlingEnabled) {
                            flingRuntime
                        } else {
                            null
                        },
                    onFlingVelocity =
                        if (gestureConfig.transform.enabled && gestureConfig.fling.panFlingEnabled) {
                            { velocity ->
                                val p = latestPlan.value
                                if (p != null) {
                                    flingRuntime.startFling(
                                        velocityPxPerSec = velocity,
                                        policy = gestureConfig.fling,
                                        flags = transformFlags,
                                        viewportState = viewportState,
                                        layoutPlan = p.layout,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentPlan = plan ?: return@Canvas
            if (spec.series.all { it.points.isEmpty() }) return@Canvas
            with(ScatterChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        val markerData = selectionState.selection?.let { buildScatterMarkerData(spec, it) }
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

        if (spec.series.all { it.points.isEmpty() }) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }

    LaunchedEffect(viewportState.viewport) {
        val selection = selectionState.selection ?: return@LaunchedEffect
        val visibleX = selection.target.point.x in viewportState.viewport.xRange.start..viewportState.viewport.xRange.endInclusive
        val visibleY = selection.target.point.y in viewportState.viewport.yRange.start..viewportState.viewport.yRange.endInclusive
        if (!visibleX || !visibleY) {
            selectionState.clear()
        }
    }
}

private fun selectionDistance(
    selectionOffset: OffsetD,
    touchOffset: Offset,
): Double {
    val dx = selectionOffset.x - touchOffset.x
    val dy = selectionOffset.y - touchOffset.y
    return (dx * dx) + (dy * dy)
}

private fun ScatterChartSpec.withPlanningScatterRenderBudget(
    canvasSize: SizeD,
    plotWidthPx: Double?,
): ScatterChartSpec {
    val sr = scatterRender
    if (!sr.usePlotWidthPixelBudget || sr.maxPointsPerSeries > 0) return this
    val w =
        when {
            plotWidthPx != null && plotWidthPx.isFinite() && plotWidthPx > 0.0 -> plotWidthPx
            canvasSize.width.isFinite() && canvasSize.width > 0.0 -> canvasSize.width
            else -> return this
        }
    val cap = LineRenderSpec.autoMaxPointsForPlotWidthPx(w)
    return copy(scatterRender = sr.copy(maxPointsPerSeries = cap))
}
