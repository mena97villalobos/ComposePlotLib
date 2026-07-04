package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * **Progressive / frame-chunked rendering** for very large series: returns a growing prefix length that starts at
 * [pointsPerFrame] and grows by [pointsPerFrame] each frame until it reaches [total]. Slice your data to the returned
 * count so the first frames plan/draw only a chunk instead of blocking on the whole set at once (also a pleasant
 * draw-in). Restarts whenever [key] or [total] changes.
 *
 * ```
 * val revealed = rememberProgressiveReveal(points.size, key = datasetId)
 * val spec = base.copy(series = listOf(base.series[0].copy(points = points.take(revealed))))
 * ```
 *
 * Complements decimation (`LineDecimationStrategy`) and the plot-width pixel budget: decimation caps how many points
 * a frame *ever* draws; this spreads the ramp-up across frames.
 *
 * @param total the full point count.
 * @param pointsPerFrame how many points to add per frame (coerced to at least 1).
 * @param key restart trigger — change it when the dataset identity changes.
 * @return the number of leading points to render this frame (`total` once fully revealed).
 */
@Composable
fun rememberProgressiveReveal(
    total: Int,
    pointsPerFrame: Int = 1024,
    key: Any? = Unit,
): Int {
    val step = pointsPerFrame.coerceAtLeast(1)
    var revealed by remember(key, total) { mutableIntStateOf(minOf(step, total).coerceAtLeast(0)) }
    LaunchedEffect(key, total, step) {
        var current = minOf(step, total).coerceAtLeast(0)
        revealed = current
        while (current < total) {
            withFrameNanos { }
            current = (current + step).coerceAtMost(total)
            revealed = current
        }
    }
    return revealed
}
