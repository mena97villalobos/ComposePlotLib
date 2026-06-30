package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

/** Estimates pan velocity (px/s) from recent `detectTransformGestures` pan deltas and timestamps. */
internal class TransformPanVelocityEstimator(
    private val maxSamples: Int = 8,
    private val zoomPanSlop: Float = 0.012f,
) {
    private data class Sample(
        val pan: Offset,
        val mark: ComparableTimeMark,
    )

    private val samples = ArrayDeque<Sample>()
    private var hadZoom: Boolean = false

    fun onGestureEvent(
        pan: Offset,
        zoom: Float,
    ) {
        if (abs(zoom - 1f) > zoomPanSlop) {
            hadZoom = true
            samples.clear()
            return
        }
        if (hadZoom) {
            hadZoom = false
            samples.clear()
        }
        samples.addLast(Sample(pan, TimeSource.Monotonic.markNow()))
        while (samples.size > maxSamples) {
            samples.removeFirst()
        }
    }

    fun clear() {
        samples.clear()
        hadZoom = false
    }

    /**
     * Uses the last two samples for an instantaneous estimate, blended with a short-window average for stability.
     */
    fun estimateVelocityPxPerSec(): Offset {
        if (hadZoom || samples.size < 2) return Offset.Zero
        val a = samples[samples.size - 2]
        val b = samples[samples.size - 1]
        val dtNs = (b.mark - a.mark).inWholeNanoseconds.coerceAtLeast(1L)
        val dtSec = dtNs / 1_000_000_000.0
        if (dtSec < 1e-4) return Offset.Zero
        val instant =
            Offset(
                x = (b.pan.x / dtSec).toFloat(),
                y = (b.pan.y / dtSec).toFloat(),
            )

        val first = samples.first()
        val last = samples.last()
        val spanNs = (last.mark - first.mark).inWholeNanoseconds.coerceAtLeast(1L)
        val spanSec = spanNs / 1_000_000_000.0
        if (spanSec < 1e-4) return instant
        var sx = 0f
        var sy = 0f
        for (i in 1 until samples.size) {
            sx += samples[i].pan.x
            sy += samples[i].pan.y
        }
        val window = Offset(sx / spanSec.toFloat(), sy / spanSec.toFloat())
        val blend = 0.55f
        val out =
            Offset(
                instant.x * blend + window.x * (1f - blend),
                instant.y * blend + window.y * (1f - blend),
            )
        return if (hypot(out.x.toDouble(), out.y.toDouble()) < 1e-3) Offset.Zero else out
    }

    fun speedPxPerSec(): Float {
        val v = estimateVelocityPxPerSec()
        return hypot(v.x, v.y)
    }
}
