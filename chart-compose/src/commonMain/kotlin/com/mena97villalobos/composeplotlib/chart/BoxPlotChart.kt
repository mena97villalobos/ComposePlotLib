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
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.boxPlotChartAccessibility
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.animation.rememberAnimatedChartViewport
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
import com.mena97villalobos.composeplotlib.chart.internal.render.BoxPlotChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.selection.BoxPlotHitResolver
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import kotlin.math.max

/**
 * Box-and-whisker (boxplot) chart: per category draws whiskers (to min / max) with end caps, a translucent Q1–Q3
 * box with a border, a median line, and outlier points. Plans layout via [ChartPlanner] and wires pan / zoom /
 * double-tap / fling and tap selection to [viewportState] / [selectionState].
 *
 * @param spec immutable description of box series, axes, grid, and legend
 * @param modifier layout modifier for the outer [Box]
 * @param viewportState pan/zoom state; default uses [defaultViewport] of [spec]
 * @param selectionState tap selection; cleared when the selected box leaves the viewport
 * @param gestureConfig tap (selection), transform (pan/zoom/double-tap zoom), and optional fling policies
 * @param noData custom empty state when every series has no entries; if non-null, takes precedence over [noDataUi]
 * @param noDataUi structured empty copy when [noData] is null; otherwise a plain [DefaultNoDataOverlay]
 * @param accessibilityCustomActions optional TalkBack / VoiceOver actions (e.g. reset viewport)
 * @param animation optional programmatic viewport easing; [ChartAnimationPolicy.respectSystemReducedMotion] applies first
 * @param chromeTheme plot frame, legend panel, and clipping; defaults to Material-aware [chartChromeTheme]
 */
@Composable
fun BoxPlotChart(
    spec: BoxPlotChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
    selectionState: ChartSelectionState = rememberChartSelectionState(),
    gestureConfig: ChartGestureConfig = ChartGestureConfig(),
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
        if (!spec.series.all { it.entries.isEmpty() }) {
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
        remember(spec, renderViewport, selectionState.selection, canvasSize, textMeasurer, labelStyles, fontScale, cartesianTickPass) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                val baseInput = chartLayoutBaseInput(canvasSize, renderViewport, fontScale)
                val preliminaryPlan =
                    ChartPlanner.planBoxPlotChart(
                        input = baseInput,
                        spec = spec,
                        selection = selectionState.selection,
                        tickValuePass = cartesianTickPass,
                    )
                ChartPlanner.planBoxPlotChart(
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

    val latestPlan = rememberUpdatedState(plan)

    Box(
        modifier =
            modifier
                .boxPlotChartAccessibility(
                    spec,
                    selectionState,
                    emptyStateDescription,
                    accessibilityCustomActions,
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
                                if (spec.series.all { it.entries.isEmpty() }) {
                                    selectionState.clear()
                                    return@chartGestures
                                }
                                val selection =
                                    BoxPlotHitResolver.resolvePlanned(
                                        boxesHitTestFirst = currentPlan.boxesForHitTest(),
                                        touchPoint = OffsetD(offset.x.toDouble(), offset.y.toDouble()),
                                    )
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
                                    if (transformFlags.panX) -(pan.x.toDouble() / plotWidth) * renderViewport.xRange.span else 0.0
                                val deltaY =
                                    if (transformFlags.panY) (pan.y.toDouble() / plotHeight) * renderViewport.yRange.span else 0.0
                                val scaleX = if (transformFlags.zoomX) zoom.toDouble() else 1.0
                                val scaleY = if (transformFlags.zoomY) zoom.toDouble() else 1.0
                                viewportState.pan(deltaX, deltaY)
                                viewportState.zoom(scaleX = scaleX, scaleY = scaleY, pivotX = pivot.x, pivotY = pivot.y)
                            }
                        } else {
                            null
                        },
                    flingPolicy = gestureConfig.fling,
                    flingRuntime =
                        if (gestureConfig.transform.enabled && gestureConfig.fling.panFlingEnabled) flingRuntime else null,
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
            if (spec.series.all { it.entries.isEmpty() }) return@Canvas
            with(BoxPlotChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        if (spec.series.all { it.entries.isEmpty() }) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }

    LaunchedEffect(viewportState.viewport, plan) {
        val selection = selectionState.selection ?: return@LaunchedEffect
        val bounds = plan?.layout?.plotBounds
        val hb = selection.hitBounds
        if (hb != null && bounds != null) {
            if (!hb.intersects(bounds)) {
                selectionState.clear()
            }
            return@LaunchedEffect
        }
        val visibleX = selection.target.point.x in viewportState.viewport.xRange.start..viewportState.viewport.xRange.endInclusive
        val visibleY = selection.target.point.y in viewportState.viewport.yRange.start..viewportState.viewport.yRange.endInclusive
        if (!visibleX || !visibleY) {
            selectionState.clear()
        }
    }
}
