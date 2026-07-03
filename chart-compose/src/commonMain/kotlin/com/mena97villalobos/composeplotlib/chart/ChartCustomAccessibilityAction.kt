package com.mena97villalobos.composeplotlib.chart

import com.mena97villalobos.composeplotlib.chart.internal.gesture.effectiveTransformFlags
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.ViewportChangeOrigin

/**
 * Optional TalkBack / VoiceOver **custom action** on the chart container (e.g. “Reset viewport”).
 * Wired through Compose [androidx.compose.ui.semantics.CustomAccessibilityAction].
 *
 * @property label Human-readable action name announced by the screen reader.
 * @property onClick Invoked when the assistive-technology user triggers the action.
 */
data class ChartCustomAccessibilityAction(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * Suggested custom actions when [ChartGestureConfig.transform] pan/zoom is enabled: reset viewport and optional
 * zoom in / out at the current window center (respects axis locks via [ChartViewportState.axisPolicy]).
 *
 * Returns an empty list when transform gestures are disabled. When present, "Reset chart viewport" is always
 * first; "Zoom in" / "Zoom out" (each scaling by a factor of 1.2 on the enabled axes) follow only when the
 * effective flags allow zooming. Zoom is applied programmatically via [ChartViewportState.zoom].
 *
 * @param viewportState Viewport whose [ChartViewportState.reset] / [ChartViewportState.zoom] the actions drive.
 * @param gestureConfig Gesture configuration; [ChartGestureConfig.transform] gates whether any actions are produced.
 * @return Ordered actions to expose as custom accessibility actions, or an empty list when transform is disabled.
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
 *
 * @param viewportState Viewport passed through to [chartViewportAccessibilityActions].
 * @param gestureConfig Gesture configuration passed through to [chartViewportAccessibilityActions].
 * @param appActions Product-specific actions appended after the viewport actions.
 * @return Viewport actions followed by [appActions] (viewport list is empty when transform is disabled).
 */
fun chartAccessibilityActionsMergeViewport(
    viewportState: ChartViewportState,
    gestureConfig: ChartGestureConfig,
    appActions: List<ChartCustomAccessibilityAction>,
): List<ChartCustomAccessibilityAction> = chartViewportAccessibilityActions(viewportState, gestureConfig) + appActions
