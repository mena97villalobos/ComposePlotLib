package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import kotlin.math.sqrt

/**
 * Resolves a tap in plot pixels to the nearest [XyPoint] in a single line series, within [maxDistancePx].
 */
object NearestPointResolver {
    /**
     * Nearest point among indexed samples (for hit-testing the same index set the polyline uses).
     *
     * @param indexedPoints Pairs of `(globalPointIndex, domainPoint)`.
     */
    fun resolveIndexed(
        seriesId: String,
        indexedPoints: List<Pair<Int, XyPoint>>,
        touchPoint: OffsetD,
        transform: ChartTransform,
        maxDistancePx: Double,
        layerId: String? = null,
    ): ChartSelection? {
        if (indexedPoints.isEmpty()) return null
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        if (!maxDistancePx.isFinite() || maxDistancePx < 0.0) return null
        val nearest =
            indexedPoints.asSequence()
                .mapNotNull { (globalIndex, point) ->
                    if (!point.x.isFinite() || !point.y.isFinite()) return@mapNotNull null
                    val pixelPoint = transform.toPixel(point)
                    val dist = pixelDistanceOrInfinite(pixelPoint, touchPoint)
                    IndexedDistance(globalIndex, point, pixelPoint, dist)
                }
                .minWithOrNull(
                    compareBy<IndexedDistance> { it.distance }.thenBy { it.index },
                ) ?: return null
        if (!nearest.distance.isFinite() || nearest.distance > maxDistancePx) return null
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = seriesId,
                    pointIndex = nearest.index,
                    point = nearest.point,
                    layerId = layerId,
                ),
            pixelOffset = nearest.pixelPoint,
        )
    }

    /**
     * @return [ChartSelection] for the closest in-range point, or `null` if none within [maxDistancePx].
     */
    fun resolve(
        seriesId: String,
        points: List<XyPoint>,
        touchPoint: OffsetD,
        transform: ChartTransform,
        maxDistancePx: Double,
    ): ChartSelection? {
        val indexed = points.indices.map { it to points[it] }
        return resolveIndexed(seriesId, indexed, touchPoint, transform, maxDistancePx)
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

    private data class IndexedDistance(
        val index: Int,
        val point: XyPoint,
        val pixelPoint: OffsetD,
        val distance: Double,
    )
}
