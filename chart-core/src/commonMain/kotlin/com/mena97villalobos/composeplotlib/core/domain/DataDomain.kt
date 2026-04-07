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
        /** @throws IllegalArgumentException if [points] is empty. */
        fun fromPoints(points: List<XyPoint>): DataDomain {
            require(points.isNotEmpty()) { "Cannot create a data domain from an empty list." }

            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }

            return DataDomain(
                xRange = ClosedRangeD(minX, maxX),
                yRange = ClosedRangeD(minY, maxY),
            )
        }
    }
}
