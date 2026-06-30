package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Maps between domain [XyPoint] and pixel [OffsetD] using independent x/y linear maps.
 *
 * Y is flipped so larger domain values map toward the top of the plot ([RectD.top]).
 */
data class ChartTransform(
    val xTransform: LinearTransform1D,
    val yTransform: LinearTransform1D,
) {
    fun toPixel(point: XyPoint): OffsetD =
        OffsetD(
            x = xTransform.toPixel(point.x),
            y = yTransform.toPixel(point.y),
        )

    fun toDomain(offset: OffsetD): XyPoint =
        XyPoint(
            x = xTransform.toDomain(offset.x),
            y = yTransform.toDomain(offset.y),
        )

    companion object {
        /** Builds transforms that map [viewport] ranges onto [plotBounds] pixel rectangle. */
        fun forPlotBounds(
            viewport: ChartViewport,
            plotBounds: RectD,
        ): ChartTransform =
            ChartTransform(
                xTransform =
                    LinearTransform1D(
                        domain = viewport.xRange,
                        pixelStart = plotBounds.left,
                        pixelEnd = plotBounds.right,
                    ),
                yTransform =
                    LinearTransform1D(
                        domain = viewport.yRange,
                        pixelStart = plotBounds.bottom,
                        pixelEnd = plotBounds.top,
                    ),
            )
    }
}
