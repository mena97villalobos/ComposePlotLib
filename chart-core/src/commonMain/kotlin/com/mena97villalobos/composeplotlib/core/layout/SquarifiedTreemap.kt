package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.max

/**
 * Squarified treemap layout (Bruls, Huizing & van Wijk): partitions [bounds] into one rectangle per entry in
 * [values], with area proportional to the value and aspect ratios kept close to 1. Returns rectangles **aligned to
 * the input order** (index `i` → `values[i]`). Non-positive values, or a degenerate [bounds], collapse to
 * zero-area rectangles at the top-left.
 */
fun squarifiedTreemap(
    values: List<Double>,
    bounds: RectD,
): List<RectD> {
    val result = arrayOfNulls<RectD>(values.size)
    val empty = RectD(bounds.left, bounds.top, bounds.left, bounds.top)
    val positive = values.mapIndexed { index, v -> IndexedArea(index, v) }.filter { it.value > 0.0 && it.value.isFinite() }
    val total = positive.sumOf { it.value }
    if (total <= 0.0 || bounds.width <= 0.0 || bounds.height <= 0.0) {
        return values.map { empty }
    }
    val scale = (bounds.width * bounds.height) / total
    val pending = positive.map { IndexedArea(it.index, it.value * scale) }.sortedByDescending { it.value }.toMutableList()

    var free = bounds
    var row = mutableListOf<IndexedArea>()
    while (pending.isNotEmpty()) {
        val next = pending.first()
        val side = minOf(free.width, free.height)
        if (row.isEmpty() || worstAspect(row, side) >= worstAspect(row + next, side)) {
            row.add(next)
            pending.removeAt(0)
        } else {
            free = layoutRow(row, free, result)
            row = mutableListOf()
        }
    }
    if (row.isNotEmpty()) layoutRow(row, free, result)

    return result.map { it ?: empty }
}

private data class IndexedArea(
    val index: Int,
    val value: Double,
)

/** Worst (largest) aspect ratio produced by laying [row] along an edge of length [side]. */
private fun worstAspect(
    row: List<IndexedArea>,
    side: Double,
): Double {
    if (row.isEmpty() || side <= 0.0) return Double.MAX_VALUE
    val sum = row.sumOf { it.value }
    val maxArea = row.maxOf { it.value }
    val minArea = row.minOf { it.value }
    if (sum <= 0.0 || minArea <= 0.0) return Double.MAX_VALUE
    val side2 = side * side
    val sum2 = sum * sum
    return max(side2 * maxArea / sum2, sum2 / (side2 * minArea))
}

/** Lays [row] as a strip along the shorter edge of [free], writing rects into [result]; returns the remaining rect. */
private fun layoutRow(
    row: List<IndexedArea>,
    free: RectD,
    result: Array<RectD?>,
): RectD {
    val sum = row.sumOf { it.value }
    if (sum <= 0.0) return free
    return if (free.width >= free.height) {
        // Vertical strip on the left; items stacked top→bottom.
        val stripWidth = sum / free.height
        var y = free.top
        for (item in row) {
            val h = item.value / stripWidth
            result[item.index] = RectD(free.left, y, free.left + stripWidth, y + h)
            y += h
        }
        RectD(free.left + stripWidth, free.top, free.right, free.bottom)
    } else {
        // Horizontal strip on top; items placed left→right.
        val stripHeight = sum / free.width
        var x = free.left
        for (item in row) {
            val w = item.value / stripHeight
            result[item.index] = RectD(x, free.top, x + w, free.top + stripHeight)
            x += w
        }
        RectD(free.left, free.top + stripHeight, free.right, free.bottom)
    }
}
