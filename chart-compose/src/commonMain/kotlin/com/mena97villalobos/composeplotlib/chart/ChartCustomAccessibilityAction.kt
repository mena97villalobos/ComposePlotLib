package com.mena97villalobos.composeplotlib.chart

import com.mena97villalobos.composeplotlib.chart.internal.gesture.effectiveTransformFlags
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.ViewportChangeOrigin

/**
 * Optional TalkBack / VoiceOver **custom action** on the chart container (e.g. “Reset viewport”).
 * Wired through Compose [androidx.compose.ui.semantics.CustomAccessibilityAction].
 */
data class ChartCustomAccessibilityAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Suggested custom actions when [ChartGestureConfig.transform] pan/zoom is enabled: reset viewport and optional
 * zoom in / out at the current window center (respects axis locks via [ChartViewportState.axisPolicy]).
 */
fun chartViewportAccessibilityActions(
    viewportState: ChartViewportState,
    gestureConfig: ChartGestureConfig,
): List<ChartCustomAccessibilityAction> {
    if (!gestureConfig.transform.enabled) return emptyList()
    val flags = effectiveTransformFlags(gestureConfig, viewportState.axisPolicy)
    val actions = ArrayList<ChartCustomAccessibilityAction>(3)
    actions.add(
        ChartCustomAccessibilityAction(label = "Reset chart viewport") {
            viewportState.reset()
        },
    )
    if (flags.zoomX || flags.zoomY) {
        val zoomIn = 1.2
        val zoomOut = 1.0 / zoomIn
        actions.add(
            ChartCustomAccessibilityAction(label = "Zoom in") {
                val v = viewportState.viewport
                val zx = if (flags.zoomX) zoomIn else 1.0
                val zy = if (flags.zoomY) zoomIn else 1.0
                viewportState.zoom(
                    zx,
                    zy,
                    v.xRange.midpoint,
                    v.yRange.midpoint,
                    ViewportChangeOrigin.Programmatic,
                )
            },
        )
        actions.add(
            ChartCustomAccessibilityAction(label = "Zoom out") {
                val v = viewportState.viewport
                val zx = if (flags.zoomX) zoomOut else 1.0
                val zy = if (flags.zoomY) zoomOut else 1.0
                viewportState.zoom(
                    zx,
                    zy,
                    v.xRange.midpoint,
                    v.yRange.midpoint,
                    ViewportChangeOrigin.Programmatic,
                )
            },
        )
    }
    return actions
}

/**
 * Appends [appActions] after [chartViewportAccessibilityActions] so TalkBack / VoiceOver lists **viewport**
 * shortcuts first, then product-specific steps (export, drill-down, etc.).
 */
fun chartAccessibilityActionsMergeViewport(
    viewportState: ChartViewportState,
    gestureConfig: ChartGestureConfig,
    appActions: List<ChartCustomAccessibilityAction>,
): List<ChartCustomAccessibilityAction> = chartViewportAccessibilityActions(viewportState, gestureConfig) + appActions
