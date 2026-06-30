package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.domain.DataDomain
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.spec.MarkerShape
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

/**
 * One logical series inside a [com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec].
 *
 * @property id Stable identifier used by selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 */
data class ScatterSeries(
    val id: String,
    val label: String,
    val points: List<ChartPoint>,
    val pointStyle: PointStyle = PointStyle(),
) {
    fun toXyPoints(): List<XyPoint> = points.map { XyPoint(it.x, it.y) }

    fun domain(): DataDomain = DataDomain.fromPoints(toXyPoints())

    /** Color for legend swatches when [PointStyle.color] is null. */
    fun resolvedPointColor(): ChartColor = pointStyle.color ?: ChartColor.Blue

    /** Per-point radius (bubble size): point override, else series [PointStyle.radius]. Never negative. */
    fun resolvedRadiusAt(index: Int): Double = (points[index].markerRadius ?: pointStyle.radius).coerceAtLeast(0.0)

    /** Per-point color: point override, else series [PointStyle.color], else [ChartColor.Blue]. */
    fun resolvedColorAt(index: Int): ChartColor = points[index].markerColor ?: pointStyle.color ?: ChartColor.Blue

    /** Per-point glyph: point override, else series [PointStyle.shape]. */
    fun resolvedShapeAt(index: Int): MarkerShape = points[index].markerShape ?: pointStyle.shape
}
