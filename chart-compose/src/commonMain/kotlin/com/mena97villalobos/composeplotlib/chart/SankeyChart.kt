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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.RequestFocusWhenChartMarkerShown
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.chartMarkerFocusContainer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.rememberChartMarkerFocusRequester
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.sankeyChartAccessibility
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberAnimatedChartViewport
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberSelectionMarkerAppearance
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.ChartFlingRuntime
import com.mena97villalobos.composeplotlib.chart.internal.gesture.cartesianDoubleTapViewportEnabled
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartCartesianKeyboardViewport
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.gesture.effectiveTransformFlags
import com.mena97villalobos.composeplotlib.chart.internal.gesture.handleCartesianChartDoubleTapViewport
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.layout.measuredLayoutInput
import com.mena97villalobos.composeplotlib.chart.internal.marker.ChartMarkerOverlay
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildSankeyMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.render.SankeyChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.SankeyHitResolver
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.max

/**
 * Flow diagram with explicit columns: weighted ribbons route between value-sized node rectangles.
 *
 * Layout, ordering, coloring, and [com.mena97villalobos.composeplotlib.core.spec.SankeyOrientation]
 * (horizontal or vertical flow) come from [SankeyChartSpec.layout]. The default viewport is the
 * normalized `0..1 × 0..1` domain the sankey planner lays out in; pan/zoom explores within it.
 *
 * @param spec nodes, links, and [com.mena97villalobos.composeplotlib.core.spec.SankeyLayoutSpec]
 * @param viewportState pan/zoom state over the normalized `0..1` sankey domain
 * @param selectionState tap selection (nodes and ribbons); cleared when the selected element leaves the viewport
 * @param gestureConfig tap (selection), transform (pan/zoom/double-tap zoom), and optional fling policies
 * @param marker optional composable overlay at the selected node/ribbon; receives [ChartMarkerData]
 * @param markerPlacement flip/slide tooltip placement; see [ChartMarkerPlacement]
 * @param noData custom empty state when the spec has no nodes or no positive-value links; if non-null, takes precedence over [noDataUi]
 * @param noDataUi structured empty copy when [noData] is null; otherwise a plain [DefaultNoDataOverlay]
 * @param accessibilityCustomActions optional TalkBack / VoiceOver actions (e.g. reset viewport)
 * @param animation optional programmatic viewport easing and marker fade-in; [ChartAnimationPolicy.respectSystemReducedMotion] is applied first
 * @param chromeTheme plot frame, legend panel, and clipping; defaults to Material-aware [chartChromeTheme]
 */
@Composable
fun SankeyChart(
    spec: SankeyChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState = rememberChartViewportState(initialViewport = defaultViewport()),
    selectionState: ChartSelectionState = rememberChartSelectionState(),
    gestureConfig: ChartGestureConfig = ChartGestureConfig.CartesianDefault,
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
        remember(labelStyles, chromeResolved) {
            ChartRenderStyles(labelStyles, chromeResolved)
        }
    val hasNoData = spec.nodes.isEmpty() || spec.links.none { it.value > 0.0 }
    val emptyStateDescription: String? =
        if (!hasNoData) {
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

    val plan =
        remember(spec, renderViewport, selectionState.selection, canvasSize, textMeasurer, labelStyles, fontScale, chromeResolved) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                val baseInput = chartLayoutBaseInput(canvasSize, renderViewport, fontScale)
                val preliminaryPlan =
                    ChartPlanner.planSankeyChart(
                        input = baseInput,
                        spec = spec,
                        selection = selectionState.selection,
                    )
                val extraLabels =
                    buildList {
                        if (spec.layout.nodeLabels.enabled) {
                            addAll(spec.nodes.map { it.label })
                        }
                        if (spec.layout.linkLabels.enabled) {
                            val byId = spec.nodes.associateBy { it.id }
                            for (l in spec.links) {
                                if (l.value <= 0.0) continue
                                val s = byId[l.sourceNodeId]?.label ?: l.sourceNodeId
                                val t = byId[l.targetNodeId]?.label ?: l.targetNodeId
                                add(spec.layout.linkLabels.formatter(s, t, l.value))
                            }
                        }
                    }
                ChartPlanner.planSankeyChart(
                    input =
                        measuredLayoutInput(
                            baseInput,
                            preliminaryPlan.layout,
                            textMeasurer,
                            renderStyles,
                            extraTickLabels = extraLabels,
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
                .sankeyChartAccessibility(
                    spec = spec,
                    selectionState = selectionState,
                    plan = plan,
                    emptyStateDescription = emptyStateDescription,
                    customAccessibilityActions = accessibilityCustomActions,
                ).chartCartesianKeyboardViewport(
                    enabled = spec.accessibility.keyboardViewportNudgeEnabled,
                    transformEnabled = gestureConfig.transform.enabled,
                    flags = transformFlags,
                    viewportState = viewportState,
                    tapSelectionEnabled = gestureConfig.tap.selectionEnabled,
                    onClearSelection = { selectionState.clear() },
                ).onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }.chartGestures(
                    onTap =
                        if (gestureConfig.tap.selectionEnabled) {
                            { offset ->
                                val currentPlan = plan ?: return@chartGestures
                                if (hasNoData) {
                                    selectionState.clear()
                                    return@chartGestures
                                }
                                val touch = OffsetD(offset.x.toDouble(), offset.y.toDouble())
                                val sel = SankeyHitResolver.resolve(touch, spec, currentPlan)
                                selectionState.select(sel)
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
            if (hasNoData) return@Canvas
            with(SankeyChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        val markerData = selectionState.selection?.let { sel -> plan?.let { buildSankeyMarkerData(spec, it, sel) } }
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

        if (hasNoData) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }

    LaunchedEffect(viewportState.viewport, plan) {
        val selection = selectionState.selection ?: return@LaunchedEffect
        val currentPlan = plan ?: return@LaunchedEffect
        val bounds = currentPlan.layout.plotBounds
        // Look up the CURRENT pixel geometry of the selected element: the selection's own hitBounds
        // are frozen at tap time and never track pan/zoom.
        val elementBounds =
            when (selection.target.layerId) {
                SankeyHitResolver.LayerNode -> currentPlan.nodes.getOrNull(selection.target.pointIndex)?.pixelBounds
                SankeyHitResolver.LayerLink -> currentPlan.links.getOrNull(selection.target.pointIndex)?.pixelBounds
                else -> null
            }
        if (elementBounds == null || !elementBounds.intersects(bounds)) {
            selectionState.clear()
        }
    }
}

private fun defaultViewport(): ChartViewport =
    ChartViewport(
        xRange = ClosedRangeD(0.0, 1.0),
        yRange = ClosedRangeD(0.0, 1.0),
    )
