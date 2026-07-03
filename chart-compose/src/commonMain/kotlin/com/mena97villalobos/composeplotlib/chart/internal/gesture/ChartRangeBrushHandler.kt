package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Drag-rectangle ("range brush" / box selection) gesture: reports the drag [onStart], each [onUpdate], and the
 * final corners [onFinish] (or [onCancel] if the drag was interrupted). Mirrors [chartGestures]' freshness pattern —
 * callbacks are read through [rememberUpdatedState] and [pointerInput] is keyed on the stable [enabled] flag — so
 * the detector survives recomposition while still invoking up-to-date logic that closes over the current plan.
 *
 * When [enabled] is false this is a no-op, so charts can hand the drag channel to pan/zoom instead.
 */
@Composable
internal fun Modifier.chartRangeBrushGesture(
    enabled: Boolean,
    onStart: (Offset) -> Unit,
    onUpdate: (Offset) -> Unit,
    onFinish: (start: Offset, end: Offset) -> Unit,
    onCancel: () -> Unit,
): Modifier {
    if (!enabled) return this
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnFinish by rememberUpdatedState(onFinish)
    val currentOnCancel by rememberUpdatedState(onCancel)
    return this.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val start = down.position
            currentOnStart(start)
            var current = start
            val completed =
                drag(down.id) { change ->
                    current = change.position
                    currentOnUpdate(current)
                    change.consume()
                }
            if (completed) currentOnFinish(start, current) else currentOnCancel()
        }
    }
}
