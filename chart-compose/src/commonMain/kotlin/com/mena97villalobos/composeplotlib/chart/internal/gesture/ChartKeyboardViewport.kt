package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.ViewportChangeOrigin
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

private const val NudgeFraction = 0.10
private const val KeyZoomFactor = 1.15

/**
 * Arrow-key pan, + / − zoom at the visible center, and Escape to clear selection when [tapSelectionEnabled].
 * Only active when the chart node is focused ([keyboardViewportNudgeEnabled] / focus flags on the spec).
 */
internal fun Modifier.chartCartesianKeyboardViewport(
    enabled: Boolean,
    transformEnabled: Boolean,
    flags: EffectiveTransformFlags,
    viewportState: ChartViewportState,
    tapSelectionEnabled: Boolean,
    onClearSelection: () -> Unit,
    /** When true, arrow keys with **Shift** are left to [chartKeyboardSelectionRoam] (selection step). */
    reserveShiftDirectionKeysForSelectionRoam: Boolean = false,
): Modifier {
    if (!enabled || !transformEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) {
            return@onKeyEvent false
        }
        if (reserveShiftDirectionKeysForSelectionRoam && event.isShiftPressed &&
            (
                event.key == Key.DirectionLeft || event.key == Key.DirectionRight ||
                    event.key == Key.DirectionUp || event.key == Key.DirectionDown
            )
        ) {
            return@onKeyEvent false
        }
        val v: ChartViewport = viewportState.viewport
        val panStepX = v.xRange.span * NudgeFraction
        val panStepY = v.yRange.span * NudgeFraction
        when (event.key) {
            Key.DirectionLeft ->
                if (flags.panX) {
                    viewportState.pan(-panStepX, 0.0, ViewportChangeOrigin.Programmatic)
                    true
                } else {
                    false
                }
            Key.DirectionRight ->
                if (flags.panX) {
                    viewportState.pan(panStepX, 0.0, ViewportChangeOrigin.Programmatic)
                    true
                } else {
                    false
                }
            Key.DirectionUp ->
                if (flags.panY) {
                    viewportState.pan(0.0, panStepY, ViewportChangeOrigin.Programmatic)
                    true
                } else {
                    false
                }
            Key.DirectionDown ->
                if (flags.panY) {
                    viewportState.pan(0.0, -panStepY, ViewportChangeOrigin.Programmatic)
                    true
                } else {
                    false
                }
            Key.Equals, Key.Plus ->
                keyZoom(v, flags, viewportState, zoomIn = true)
            Key.Minus, Key.NumPadSubtract ->
                keyZoom(v, flags, viewportState, zoomIn = false)
            Key.Escape ->
                if (tapSelectionEnabled) {
                    onClearSelection()
                    true
                } else {
                    false
                }
            else -> false
        }
    }
}

private fun keyZoom(
    v: ChartViewport,
    flags: EffectiveTransformFlags,
    viewportState: ChartViewportState,
    zoomIn: Boolean,
): Boolean {
    if (!flags.zoomX && !flags.zoomY) return false
    val z = if (zoomIn) KeyZoomFactor else 1.0 / KeyZoomFactor
    val zx = if (flags.zoomX) z else 1.0
    val zy = if (flags.zoomY) z else 1.0
    val cx = v.xRange.midpoint
    val cy = v.yRange.midpoint
    viewportState.zoom(zx, zy, cx, cy, ViewportChangeOrigin.Programmatic)
    return true
}
