package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.domain.DataDomain
import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle

/**
 * One logical series inside a [com.mena97villalobos.composeplotlib.core.spec.LineChartSpec].
 *
 * @property id Stable identifier used by selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 */
data class LineSeries(
    val id: String,
    val label: String,
    val points: List<ChartPoint>,
    val lineStyle: LineStyle = LineStyle(),
    val pointStyle: PointStyle = PointStyle(),
) {
    fun toXyPoints(): List<XyPoint> = points.map { XyPoint(it.x, it.y) }

    fun domain(): DataDomain = DataDomain.fromPoints(toXyPoints())
}
