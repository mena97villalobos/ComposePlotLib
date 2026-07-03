package com.mena97villalobos.composeplotlib.core.domain

import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * Axis-aligned bounding box over data in domain coordinates.
 *
 * @property xRange Domain x extent.
 * @property yRange Domain y extent.
 */
data class DataDomain(
    val xRange: ClosedRangeD,
    val yRange: ClosedRangeD,
) {
    companion object {
        /**
         * Bounds over [points], **ignoring non-finite coordinates** so gap markers (`NaN` y, see
         * [ChartPoint.gap][com.mena97villalobos.composeplotlib.core.model.ChartPoint.gap]) do not corrupt the extent.
         * An axis with no finite values collapses to `0.0..0.0`.
         *
         * @throws IllegalArgumentException if [points] is empty.
         */
        fun fromPoints(points: List<XyPoint>): DataDomain {
            require(points.isNotEmpty()) { "Cannot create a data domain from an empty list." }

            var minX = Double.POSITIVE_INFINITY
            var maxX = Double.NEGATIVE_INFINITY
            var minY = Double.POSITIVE_INFINITY
            var maxY = Double.NEGATIVE_INFINITY
            for (p in points) {
                if (p.x.isFinite()) {
                    if (p.x < minX) minX = p.x
                    if (p.x > maxX) maxX = p.x
                }
                if (p.y.isFinite()) {
                    if (p.y < minY) minY = p.y
                    if (p.y > maxY) maxY = p.y
                }
            }
            if (minX > maxX) {
                minX = 0.0
                maxX = 0.0
            }
            if (minY > maxY) {
                minY = 0.0
                maxY = 0.0
            }

            return DataDomain(
                xRange = ClosedRangeD(minX, maxX),
                yRange = ClosedRangeD(minY, maxY),
            )
        }
    }
}
