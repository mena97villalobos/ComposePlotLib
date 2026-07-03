package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartPoint

/**
 * Whether `LineChart` can morph between [from] and [to] by linearly interpolating matching series points
 * (same series count, ids, and point counts per series).
 *
 * @param from Source spec of the transition.
 * @param to Target spec of the transition.
 * @return `true` when the two specs are structurally compatible for point-wise interpolation.
 */
fun canMorphLineSpecs(
    from: LineChartSpec,
    to: LineChartSpec,
): Boolean {
    if (from.series.size != to.series.size) return false
    for (i in from.series.indices) {
        val a = from.series[i]
        val b = to.series[i]
        if (a.id != b.id || a.points.size != b.points.size) return false
    }
    return true
}

/**
 * Linearly interpolates series point coordinates from [from] to [to]. Returns null if structure differs.
 *
 * @param from Spec at `t = 0`.
 * @param to Spec at `t = 1`; supplies series metadata (ids, styles) for the interpolated result.
 * @param t Interpolation factor. `t <= 0.0` returns [from] and `t >= 1.0` returns [to]; intermediate values
 * blend each point's x/y linearly.
 * @return The interpolated spec, or `null` when [from] and [to] are not morph-compatible (see [canMorphLineSpecs]).
 */
fun lerpLineChartSpecForDataTransition(
    from: LineChartSpec,
    to: LineChartSpec,
    t: Double,
): LineChartSpec? {
    if (!canMorphLineSpecs(from, to)) return null
    when {
        t <= 0.0 -> return from
        t >= 1.0 -> return to
    }
    val tt = t
    return to.copy(
        series =
            to.series.mapIndexed { i, ts ->
                val fs = from.series[i]
                ts.copy(
                    points =
                        fs.points.zip(ts.points).map { (p, q) ->
                            ChartPoint(
                                x = p.x + (q.x - p.x) * tt,
                                y = p.y + (q.y - p.y) * tt,
                            )
                        },
                )
            },
    )
}
