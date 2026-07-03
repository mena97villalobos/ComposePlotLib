package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.stream.StreamingSeriesBuffer

/**
 * Observable streaming-point state for real-time line / scatter charts. Wraps a bounded [StreamingSeriesBuffer]:
 * reads of [points] subscribe to recomposition, and every mutation publishes a fresh immutable snapshot. Rebuilding
 * a `LineChartSpec` / `ScatterChartSpec` from [points] therefore replans on each append while the ring buffer keeps
 * memory and per-frame cost bounded. Pair with `ChartAnimationPolicy` data-transition for append easing.
 *
 * Obtain one via [rememberStreamingSeries].
 */
class StreamingSeriesState internal constructor(
    capacity: Int,
    initial: List<ChartPoint>,
) {
    private val buffer = StreamingSeriesBuffer(capacity, initial)

    /** Current immutable snapshot (oldest first). Read inside composition to observe appends. */
    var points by mutableStateOf(buffer.snapshot())
        private set

    /** Maximum retained points (ring-buffer bound). */
    val capacity: Int get() = buffer.capacity

    /** Appends [point] (dropping the oldest once at [capacity]) and publishes a new snapshot. */
    fun append(point: ChartPoint) {
        buffer.append(point)
        points = buffer.snapshot()
    }

    /** Appends [newPoints] in order and publishes a new snapshot. */
    fun appendAll(newPoints: Iterable<ChartPoint>) {
        buffer.appendAll(newPoints)
        points = buffer.snapshot()
    }

    /** Removes all points and publishes an empty snapshot. */
    fun clear() {
        buffer.clear()
        points = buffer.snapshot()
    }
}

/**
 * Remembers a [StreamingSeriesState] with a bounded [capacity]. Append from a `LaunchedEffect` or event callback and
 * feed [StreamingSeriesState.points] into a series. Keyed on [capacity], so a fresh buffer (seeded from [initial]) is
 * created only when the capacity changes — passing a new [initial] list literal each recomposition does not reset it.
 */
@Composable
fun rememberStreamingSeries(
    capacity: Int,
    initial: List<ChartPoint> = emptyList(),
): StreamingSeriesState = remember(capacity) { StreamingSeriesState(capacity, initial) }
