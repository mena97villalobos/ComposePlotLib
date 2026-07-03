package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Maps between domain [XyPoint] and pixel [OffsetD] using independent x/y maps. Each axis first passes its value
 * through a [AxisValueTransform] (identity for linear axes, `log` for [AxisScale.Log] / [AxisScale.SymLog]) and then
 * a linear pixel projection, so a log axis renders decades evenly.
 *
 * Y is flipped so larger domain values map toward the top of the plot ([RectD.top]).
 *
 * @property xTransform Scaled-space-to-pixel map for the x axis.
 * @property yTransform Scaled-space-to-pixel map for the y axis (inverted so larger values map upward).
 * @property xScale Value transform applied before [xTransform]; [AxisValueTransform.Identity] for linear axes.
 * @property yScale Value transform applied before [yTransform]; [AxisValueTransform.Identity] for linear axes.
 */
data class ChartTransform(
    val xTransform: LinearTransform1D,
    val yTransform: LinearTransform1D,
    val xScale: AxisValueTransform = AxisValueTransform.Identity,
    val yScale: AxisValueTransform = AxisValueTransform.Identity,
) {
    /** Projects a domain [point] to its pixel [OffsetD] (scale forward map then linear projection). */
    fun toPixel(point: XyPoint): OffsetD =
        OffsetD(
            x = xTransform.toPixel(xScale.forward(point.x)),
            y = yTransform.toPixel(yScale.forward(point.y)),
        )

    /** Inverse of [toPixel]: maps a pixel [offset] back to its domain [XyPoint] (linear inverse then scale inverse). */
    fun toDomain(offset: OffsetD): XyPoint =
        XyPoint(
            x = xScale.inverse(xTransform.toDomain(offset.x)),
            y = yScale.inverse(yTransform.toDomain(offset.y)),
        )

    companion object {
        /**
         * Builds transforms that map [viewport] ranges onto the [plotBounds] pixel rectangle. The y map runs from
         * [RectD.bottom] to [RectD.top] so larger domain values render higher on screen. Passing a non-linear
         * [xScale] / [yScale] (e.g. [AxisScale.Log]) projects that axis in scaled space so decades are evenly spaced.
         *
         * @param viewport Visible domain window whose x/y ranges are mapped.
         * @param plotBounds Pixel rectangle the viewport is projected onto.
         * @param xScale X-axis scale; defaults to [AxisScale.Linear] (identity).
         * @param yScale Y-axis scale; defaults to [AxisScale.Linear] (identity).
         */
        fun forPlotBounds(
            viewport: ChartViewport,
            plotBounds: RectD,
            xScale: AxisScale = AxisScale.Linear,
            yScale: AxisScale = AxisScale.Linear,
            invertX: Boolean = false,
            invertY: Boolean = false,
        ): ChartTransform {
            val xv = AxisValueTransform.forScale(xScale)
            val yv = AxisValueTransform.forScale(yScale)
            // Non-inverted X runs left→right; non-inverted Y runs bottom→top (larger values up). Inverting an axis
            // swaps its pixel endpoints so larger domain values map to the opposite edge.
            val xStart = if (invertX) plotBounds.right else plotBounds.left
            val xEnd = if (invertX) plotBounds.left else plotBounds.right
            val yStart = if (invertY) plotBounds.top else plotBounds.bottom
            val yEnd = if (invertY) plotBounds.bottom else plotBounds.top
            return ChartTransform(
                xTransform =
                    LinearTransform1D(
                        domain = scaledDomain(viewport.xRange, xv),
                        pixelStart = xStart,
                        pixelEnd = xEnd,
                    ),
                yTransform =
                    LinearTransform1D(
                        domain = scaledDomain(viewport.yRange, yv),
                        pixelStart = yStart,
                        pixelEnd = yEnd,
                    ),
                xScale = xv,
                yScale = yv,
            )
        }

        /** Default number of decades shown when a [AxisValueTransform.Log] axis' lower bound is non-positive. */
        private const val LOG_FALLBACK_DECADES = 4.0

        /**
         * Maps a domain [range] into scaled space. For identity axes this is the range unchanged. For log axes whose
         * lower bound is non-positive (undefined for `log`), the lower bound is floored to [LOG_FALLBACK_DECADES]
         * below the (positive) upper bound so the transform stays finite.
         */
        private fun scaledDomain(
            range: ClosedRangeD,
            scale: AxisValueTransform,
        ): ClosedRangeD {
            if (scale.isIdentity) return range
            var lo = scale.forward(range.start)
            var hi = scale.forward(range.endInclusive)
            if (!hi.isFinite()) hi = if (lo.isFinite()) lo + LOG_FALLBACK_DECADES else 1.0
            if (!lo.isFinite()) lo = hi - LOG_FALLBACK_DECADES
            if (lo > hi) {
                val t = lo
                lo = hi
                hi = t
            }
            return ClosedRangeD(lo, hi)
        }
    }
}
