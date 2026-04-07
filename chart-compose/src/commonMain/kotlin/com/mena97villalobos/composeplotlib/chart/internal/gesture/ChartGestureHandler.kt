package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
 */
internal fun Modifier.chartGestures(
    onTap: ((Offset) -> Unit)?,
    onDoubleTap: ((Offset) -> Unit)?,
    onTransform: ((centroid: Offset, pan: Offset, zoom: Float) -> Unit)?,
    flingPolicy: ChartFlingGesturePolicy = ChartFlingGesturePolicy(),
    flingRuntime: ChartFlingRuntime? = null,
    onFlingVelocity: ((velocityPxPerSec: Offset) -> Unit)? = null,
): Modifier {
    var modifier = this

    if (onTap != null || onDoubleTap != null) {
        modifier =
            modifier.pointerInput(onTap, onDoubleTap) {
                when {
                    onTap != null && onDoubleTap != null ->
                        detectTapGestures(
                            onTap = onTap,
                            onDoubleTap = onDoubleTap,
                        )
                    onDoubleTap != null -> detectTapGestures(onDoubleTap = onDoubleTap)
                    else -> detectTapGestures(onTap = onTap!!)
                }
            }
    }

    if (onTransform != null) {
        modifier =
            modifier.pointerInput(onTransform, flingPolicy, flingRuntime, onFlingVelocity) {
                val fr = flingRuntime
                val fv = onFlingVelocity
                val useFling = flingPolicy.panFlingEnabled && fr != null && fv != null
                if (!useFling) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        onTransform(centroid, pan, zoom)
                    }
                } else {
                    val estimator = TransformPanVelocityEstimator()
                    var debounceJob: Job? = null
                    val gestureScope = CoroutineScope(currentCoroutineContext())
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        fr.cancel()
                        debounceJob?.cancel()
                        onTransform(centroid, pan, zoom)
                        estimator.onGestureEvent(pan, zoom)
                        debounceJob =
                            gestureScope.launch {
                                delay(flingPolicy.gestureEndDebounceMs)
                                val v = estimator.estimateVelocityPxPerSec()
                                if (hypot(v.x.toDouble(), v.y.toDouble()) >= flingPolicy.minVelocityPxPerSec) {
                                    fv(v)
                                }
                                estimator.clear()
                            }
                    }
                }
            }
    }

    return modifier
}
