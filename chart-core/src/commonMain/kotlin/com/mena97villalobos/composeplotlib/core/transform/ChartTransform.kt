package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Maps between domain [XyPoint] and pixel [OffsetD] using independent x/y linear maps.
 *
 * Y is flipped so larger domain values map toward the top of the plot ([RectD.top]).
 *
 * @property xTransform Domain-to-pixel map for the x axis.
 * @property yTransform Domain-to-pixel map for the y axis (inverted so larger values map upward).
 */
data class ChartTransform(
    val xTransform: LinearTransform1D,
    val yTransform: LinearTransform1D,
) {
    /** Projects a domain [point] to its pixel [OffsetD] using [xTransform] and [yTransform]. */
    fun toPixel(point: XyPoint): OffsetD =
        OffsetD(
            x = xTransform.toPixel(point.x),
            y = yTransform.toPixel(point.y),
        )

    /** Inverse of [toPixel]: maps a pixel [offset] back to its domain [XyPoint]. */
    fun toDomain(offset: OffsetD): XyPoint =
        XyPoint(
            x = xTransform.toDomain(offset.x),
            y = yTransform.toDomain(offset.y),
        )

    companion object {
        /**
         * Builds transforms that map [viewport] ranges onto the [plotBounds] pixel rectangle. The y map runs from
         * [RectD.bottom] to [RectD.top] so larger domain values render higher on screen.
         *
         * @param viewport Visible domain window whose x/y ranges are mapped.
         * @param plotBounds Pixel rectangle the viewport is projected onto.
         */
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
