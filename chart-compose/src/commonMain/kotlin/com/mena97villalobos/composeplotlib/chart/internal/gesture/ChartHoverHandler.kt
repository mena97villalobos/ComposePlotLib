package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Pointer **hover** channel: reports the pointer position on each hover [onMove] and [onExit] when it leaves. Only
 * mouse / stylus devices generate hover events (touch does not), so this is a no-op on phones and additive on
 * desktop / tablet. Follows [chartGestures]' freshness pattern (callbacks via [rememberUpdatedState]).
 *
 * When [enabled] is false this is a no-op.
 */
@Composable
internal fun Modifier.chartHoverGesture(
    enabled: Boolean,
    onMove: (Offset) -> Unit,
    onExit: () -> Unit,
): Modifier {
    if (!enabled) return this
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnExit by rememberUpdatedState(onExit)
    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Move -> event.changes.lastOrNull()?.let { currentOnMove(it.position) }
                    PointerEventType.Exit -> currentOnExit()
                    else -> Unit
                }
            }
        }
    }
}
