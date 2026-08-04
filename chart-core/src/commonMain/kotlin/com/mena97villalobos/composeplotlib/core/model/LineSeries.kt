package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.domain.DataDomain
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

/**
 * One logical series inside a [com.mena97villalobos.composeplotlib.core.spec.LineChartSpec].
 *
 * @property id Stable identifier used by selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 * @property label Human-readable name shown in the legend for this series.
 * @property points Ordered samples in domain space; see [ChartPoint].
 * @property lineStyle Stroke and optional area-fill appearance; defaults to [LineStyle].
 * @property pointStyle Vertex marker appearance; defaults to [PointStyle].
 */
data class LineSeries(
    val id: String,
    val label: String,
    val points: List<ChartPoint>,
    val lineStyle: LineStyle = LineStyle(),
    val pointStyle: PointStyle = PointStyle(),
) {
    /** Projects [points] to bare [XyPoint]s for shared geometry helpers (marker fields are dropped). */
    fun toXyPoints(): List<XyPoint> = points.map { XyPoint(it.x, it.y) }

    /**
     * Computes the axis-aligned data bounds of this series via [DataDomain.fromPoints].
     *
     * Safe to call on a series with no [points]: the result is the degenerate domain `0.0..0.0` on both
     * axes rather than an exception, so hosts may call this while data is still loading.
     */
    fun domain(): DataDomain = DataDomain.fromPoints(toXyPoints())
}
