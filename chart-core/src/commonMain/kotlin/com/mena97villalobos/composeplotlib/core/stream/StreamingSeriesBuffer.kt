package com.mena97villalobos.composeplotlib.core.stream

import com.mena97villalobos.composeplotlib.core.model.ChartPoint

/**
 * A bounded, append-only buffer of [ChartPoint]s for real-time / streaming charts. Holds at most [capacity] of the
 * most-recently appended points with ring-buffer semantics: once full, each new point drops the oldest. [snapshot]
 * returns an immutable copy safe to hand to a `LineSeries` / `ScatterSeries` — the bound caps per-frame replanning
 * and memory. KMP-safe (pure `kotlin.collections`).
 *
 * This is a **mutable state holder**, not a spec; the immutable spec is rebuilt from [snapshot] each frame. The
 * `chart-compose` `rememberStreamingSeries` helper wraps this with observable snapshot state.
 *
 * @param capacity Maximum retained points; must be positive.
 * @param initial Seed points (the last [capacity] are kept if longer).
 */
class StreamingSeriesBuffer(
    val capacity: Int,
    initial: List<ChartPoint> = emptyList(),
) {
    init {
        require(capacity > 0) { "capacity must be positive, was $capacity" }
    }

    private val points = ArrayDeque<ChartPoint>(minOf(capacity, initial.size).coerceAtLeast(0))

    init {
        appendAll(initial)
    }

    /** Current point count (never exceeds [capacity]). */
    val size: Int get() = points.size

    /** True when no points are buffered. */
    val isEmpty: Boolean get() = points.isEmpty()

    /** Appends [point], dropping the oldest point when already at [capacity]. */
    fun append(point: ChartPoint) {
        points.addLast(point)
        while (points.size > capacity) {
            points.removeFirst()
        }
    }

    /** Appends [newPoints] in order (see [append]); only the last [capacity] survive. */
    fun appendAll(newPoints: Iterable<ChartPoint>) {
        for (p in newPoints) append(p)
    }

    /** The most-recently appended point, or null when empty. */
    fun latest(): ChartPoint? = points.lastOrNull()

    /** Immutable copy of the buffered points in append order (oldest first). */
    fun snapshot(): List<ChartPoint> = points.toList()

    /** Removes all buffered points. */
    fun clear() {
        points.clear()
    }
}
