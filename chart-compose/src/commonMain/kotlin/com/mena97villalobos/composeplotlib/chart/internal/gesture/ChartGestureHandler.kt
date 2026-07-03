package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.mena97villalobos.composeplotlib.chart.ChartFlingGesturePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Tap, optional double-tap, multi-touch transform, and optional fling scheduling (debounced after pan quiescence).
 *
 * The callbacks are captured through [rememberUpdatedState] and the underlying [pointerInput] blocks are keyed only
 * on stable values (whether each handler is present, and fling tuning). This is deliberate: chart callbacks close
 * over per-frame state such as the current layout plan and the animated render viewport, so keying `pointerInput`
 * on the lambdas themselves would restart the gesture detector on every recomposition — cancelling an in-progress
 * pinch/drag and leaving pan/zoom effectively frozen after the first event. Reading the latest lambda via a
 * remembered [androidx.compose.runtime.State] keeps the detector alive for the whole gesture while still invoking
 * up-to-date logic.
 */
@Composable
internal fun Modifier.chartGestures(
    onTap: ((Offset) -> Unit)?,
    onDoubleTap: ((Offset) -> Unit)?,
    onTransform: ((centroid: Offset, pan: Offset, zoom: Float) -> Unit)?,
    flingPolicy: ChartFlingGesturePolicy = ChartFlingGesturePolicy(),
    flingRuntime: ChartFlingRuntime? = null,
    onFlingVelocity: ((velocityPxPerSec: Offset) -> Unit)? = null,
): Modifier {
    var modifier = this

    val tapEnabled = onTap != null
    val doubleTapEnabled = onDoubleTap != null
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)

    if (tapEnabled || doubleTapEnabled) {
        modifier =
            modifier.pointerInput(tapEnabled, doubleTapEnabled) {
                when {
                    tapEnabled && doubleTapEnabled ->
                        detectTapGestures(
                            onTap = { currentOnTap?.invoke(it) },
                            onDoubleTap = { currentOnDoubleTap?.invoke(it) },
                        )
                    doubleTapEnabled -> detectTapGestures(onDoubleTap = { currentOnDoubleTap?.invoke(it) })
                    else -> detectTapGestures(onTap = { currentOnTap?.invoke(it) })
                }
            }
    }

    val transformEnabled = onTransform != null
    val currentOnTransform by rememberUpdatedState(onTransform)
    val currentOnFlingVelocity by rememberUpdatedState(onFlingVelocity)

    if (transformEnabled) {
        val useFling = flingPolicy.panFlingEnabled && flingRuntime != null && onFlingVelocity != null
        modifier =
            modifier.pointerInput(
                useFling,
                flingRuntime,
                flingPolicy.gestureEndDebounceMs,
                flingPolicy.minVelocityPxPerSec,
            ) {
                if (!useFling) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        currentOnTransform?.invoke(centroid, pan, zoom)
                    }
                } else {
                    val estimator = TransformPanVelocityEstimator()
                    var debounceJob: Job? = null
                    val gestureScope = CoroutineScope(currentCoroutineContext())
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        flingRuntime.cancel()
                        debounceJob?.cancel()
                        currentOnTransform?.invoke(centroid, pan, zoom)
                        estimator.onGestureEvent(pan, zoom)
                        debounceJob =
                            gestureScope.launch {
                                delay(flingPolicy.gestureEndDebounceMs)
                                val v = estimator.estimateVelocityPxPerSec()
                                if (hypot(v.x.toDouble(), v.y.toDouble()) >= flingPolicy.minVelocityPxPerSec) {
                                    currentOnFlingVelocity?.invoke(v)
                                }
                                estimator.clear()
                            }
                    }
                }
            }
    }

    return modifier
}
