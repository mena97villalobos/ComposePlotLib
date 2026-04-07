package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.layout.RadarChartLayoutMath
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Resolves a tap in plot pixels to the nearest radar vertex among all series, within [maxDistancePx].
 *
 * Selection target conventions:
 * - [HighlightTarget.seriesId] is the matching [com.mena97villalobos.composeplotlib.core.spec.RadarSeries.id]
 * - [HighlightTarget.pointIndex] is the axis index (spoke index) in [RadarChartSpec.axes]
 * - [HighlightTarget.point] encodes `(x=axisIndex, y=value)` in domain-like terms for marker formatting.
 */
object RadarHitResolver {
    fun resolve(
        touchPoint: OffsetD,
        spec: RadarChartSpec,
        plotBounds: RectD,
        maxDistancePx: Double = 24.0,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        if (!maxDistancePx.isFinite() || maxDistancePx < 0.0) return null
        val axisCount = spec.axes.size
        if (axisCount < 3) return null

        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        if (radial.outerRadius <= 0.0) return null
        val angles = RadarChartLayoutMath.spokeAnglesDegrees(axisCount, spec.startAngleDegrees)

        val allValues = buildList {
            for (s in spec.series) {
                if (s.values.size != axisCount) continue
                for (v in s.values) if (v.isFinite()) add(v)
            }
        }
        val minV0 = allValues.minOrNull() ?: 0.0
        val maxV0 = allValues.maxOrNull() ?: 1.0
        val minV = min(0.0, minV0)
        val maxV = if (maxV0 == minV) minV + FpMath.DEFAULT_DEGENERATE_PAD else maxV0
        val span = max(maxV - minV, FpMath.EPS_SPAN)

        fun valueToRadiusPx(v: Double): Double {
            val safe = if (v.isFinite()) v else minV
            val t = ((safe - minV) / span).coerceIn(0.0, 1.0)
            return radial.outerRadius * t
        }

        var best: ChartSelection? = null
        var bestDist = Double.POSITIVE_INFINITY

        for (s in spec.series) {
            if (s.values.size != axisCount) continue
            for (i in 0 until axisCount) {
                val v = s.values[i]
                val px =
                    RadarChartLayoutMath.polarToPixel(
                        center = radial.center,
                        radiusPx = valueToRadiusPx(v),
                        angleDegrees = angles[i],
                    )
                val d = pixelDistanceOrInfinite(px, touchPoint)
                if (d < bestDist) {
                    bestDist = d
                    best =
                        ChartSelection(
                            target =
                                HighlightTarget(
                                    seriesId = s.id,
                                    pointIndex = i,
                                    point = XyPoint(i.toDouble(), v),
                                ),
                            pixelOffset = px,
                        )
                }
            }
        }

        if (best == null) return null
        if (!bestDist.isFinite() || bestDist > maxDistancePx) return null
        return best
    }

    private fun pixelDistanceOrInfinite(
        a: OffsetD,
        b: OffsetD,
    ): Double {
        if (!a.x.isFinite() || !a.y.isFinite()) return Double.POSITIVE_INFINITY
        val dx = a.x - b.x
        val dy = a.y - b.y
        val d = sqrt((dx * dx) + (dy * dy))
        return if (d.isFinite()) d else Double.POSITIVE_INFINITY
    }
}

