package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.mena97villalobos.composeplotlib.chart.internal.render.StackedAreaChartRenderer
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.spec.StackedAreaChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import kotlin.math.max

/**
 * Stacked-area chart: one filled band per [StackedAreaSeries], accumulated over the shared x values per the
 * spec's [AreaStackOffset] (absolute / 100% expand / streamgraph silhouette). Cartesian pan / zoom / double-tap /
 * fling; no per-point selection or markers.
 *
 * @param spec immutable stacked-area description (x values, series, offset, axes, grid, legend)
 * @param modifier layout modifier for the outer [Box]
 * @param viewportState pan/zoom state; default uses [defaultViewport] of [spec]
 * @param gestureConfig transform (pan/zoom/double-tap zoom) and optional fling policies (tap selection is ignored)
 * @param noData custom empty state when there are no series/x values; if non-null, takes precedence over [noDataUi]
 * @param noDataUi structured empty copy when [noData] is null; otherwise a plain [DefaultNoDataOverlay]
 * @param animation optional programmatic viewport easing
 * @param chromeTheme plot frame, legend panel, and clipping; defaults to Material-aware [chartChromeTheme]
 */
@Composable
fun StackedAreaChart(
    spec: StackedAreaChartSpec,
    modifier: Modifier = Modifier,
    viewportState: ChartViewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
    gestureConfig: ChartGestureConfig = ChartGestureConfig(),
    noData: (@Composable BoxScope.() -> Unit)? = null,
    noDataUi: ChartNoDataUi? = null,
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
    val isEmpty = spec.series.isEmpty() || spec.xValues.size < 2
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
        remember(spec, renderViewport, canvasSize, textMeasurer, labelStyles, fontScale, cartesianTickPass) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                val baseInput = chartLayoutBaseInput(canvasSize, renderViewport, fontScale)
                val preliminaryPlan = ChartPlanner.planStackedAreaChart(baseInput, spec, cartesianTickPass)
                ChartPlanner.planStackedAreaChart(
                    measuredLayoutInput(baseInput, preliminaryPlan.layout, textMeasurer, ChartRenderStyles(labelStyles, chromeResolved)),
                    spec,
                    cartesianTickPass,
                )
            }
        }

    val latestPlan = rememberUpdatedState(plan)
    val description =
        spec.accessibility.contentDescription
            ?: if (spec.series.isEmpty()) "Stacked area chart with no series." else "Stacked area chart. Series: ${spec.series.joinToString { it.label }}."

    Box(
        modifier =
            modifier
                .semantics { contentDescription = description }
                .chartCartesianKeyboardViewport(
                    enabled = spec.accessibility.keyboardViewportNudgeEnabled,
                    transformEnabled = gestureConfig.transform.enabled,
                    flags = transformFlags,
                    viewportState = viewportState,
                    tapSelectionEnabled = false,
                    onClearSelection = {},
                ).onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }.chartGestures(
                    onTap = null,
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
                                        xScale = spec.xAxis.scale,
                                        yScale = spec.yAxis.scale,
                                        invertX = spec.xAxis.inverted,
                                        invertY = spec.yAxis.inverted,
                                    )
                                val pivot = transform.toDomain(OffsetD(centroid.x.toDouble(), centroid.y.toDouble()))
                                val plotWidth = max(currentPlan.layout.plotBounds.width, 1.0)
                                val plotHeight = max(currentPlan.layout.plotBounds.height, 1.0)
                                val xPanSign = if (spec.xAxis.inverted) 1.0 else -1.0
                                val yPanSign = if (spec.yAxis.inverted) -1.0 else 1.0
                                val deltaX =
                                    if (transformFlags.panX) xPanSign * (pan.x.toDouble() / plotWidth) * renderViewport.xRange.span else 0.0
                                val deltaY =
                                    if (transformFlags.panY) yPanSign * (pan.y.toDouble() / plotHeight) * renderViewport.yRange.span else 0.0
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
            if (isEmpty) return@Canvas
            with(StackedAreaChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        if (isEmpty) {
            when {
                noData != null -> noData()
                noDataUi != null -> ChartNoDataContent(noDataUi)
                else -> DefaultNoDataOverlay()
            }
        }
    }
}
