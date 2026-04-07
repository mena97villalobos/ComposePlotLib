package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform

/**
 * Points eligible for scatter hit-testing: same inclusion rule as plotted circles — the marker disc
 * (center + `radiusPx`) must intersect `plotBounds`, not only lie inside it.
 */
object ScatterPlotPoints {
    fun indexedInPlotBounds(
        points: List<ChartPoint>,
        transform: ChartTransform,
        plotBounds: RectD,
        radiusPx: Double = 0.0,
    ): List<Pair<Int, XyPoint>> =
        points.mapIndexedNotNull { index, p ->
            val xy = XyPoint(p.x, p.y)
            val pixel = transform.toPixel(xy)
            if (plotBounds.intersectsDisc(pixel, radiusPx)) {
                index to xy
            } else {
                null
            }
        }

    /** Keeps only prepared domain points whose marker disc intersects `plotBounds` in pixel space. */
    fun filterPreparedToPlotDisc(
        prepared: List<Pair<Int, XyPoint>>,
        transform: ChartTransform,
        plotBounds: RectD,
        radiusPx: Double,
    ): List<Pair<Int, XyPoint>> {
        if (prepared.isEmpty()) return emptyList()
        val r = radiusPx.coerceAtLeast(0.0)
        val out = ArrayList<Pair<Int, XyPoint>>(prepared.size)
        for (pair in prepared) {
            val pixel = transform.toPixel(pair.second)
            if (plotBounds.intersectsDisc(pixel, r)) {
                out.add(pair)
            }
        }
        return out
    }
}
