package com.mena97villalobos.composeplotlib.core.spec

/**
 * Controls how line series are windowed to the viewport and downsampled for planning/drawing.
 *
 * [ChartPlanner][com.mena97villalobos.composeplotlib.core.layout.ChartPlanner] windowing follows the viewport’s
 * **domain** rectangle (x and y ranges), then clips polylines to plot bounds in pixel space; [viewportContinuityPadding]
 * retains extra neighbour samples so entry/exit intersections against the plot edge stay well-defined.
 */
data class LineRenderSpec(
    /**
     * Max number of vertices sent to the renderer **after** the visible window is applied.
     * `0` or negative means **no explicit cap** unless [usePlotWidthPixelBudget] is enabled in the Compose layer,
     * which derives a cap from plot width via [autoMaxPointsForPlotWidthPx].
     */
    val maxPointsPerSeries: Int = 0,
    val decimation: LineDecimationStrategy = LineDecimationStrategy.Uniform,
    /**
     * Extra points to keep on each side of the x-visible index span (for polyline continuity at pan edges).
     */
    val viewportContinuityPadding: Int = 1,
    /**
     * When `true` and [maxPointsPerSeries] `<= 0`, the Compose line chart supplies a cap from the laid-out plot width
     * using [autoMaxPointsForPlotWidthPx] so huge series stay bounded without an explicit number.
     * When [maxPointsPerSeries] is positive, this flag is ignored.
     */
    val usePlotWidthPixelBudget: Boolean = false,
) {
    companion object {
        /** Default vertices-per-plot-pixel budget for [autoMaxPointsForPlotWidthPx] (tuned M8: slightly below 2.5 for large-series cost). */
        const val DEFAULT_PLOT_WIDTH_OVERSAMPLE = 2.25
        const val MIN_AUTO_MAX_POINTS = 256
        const val MAX_AUTO_MAX_POINTS = 16_384

        /**
         * Targets ~ [oversample] polyline vertices per horizontal pixel of plot width (clamped to
         * [MIN_AUTO_MAX_POINTS]..[MAX_AUTO_MAX_POINTS]). Intended for [usePlotWidthPixelBudget] call sites.
         */
        fun autoMaxPointsForPlotWidthPx(
            plotWidthPx: Double,
            oversample: Double = DEFAULT_PLOT_WIDTH_OVERSAMPLE,
        ): Int {
            if (!plotWidthPx.isFinite() || plotWidthPx <= 0.0 || !oversample.isFinite() || oversample <= 0.0) {
                return 4096
            }
            val raw = kotlin.math.round(plotWidthPx * oversample).toInt()
            return raw.coerceIn(MIN_AUTO_MAX_POINTS, MAX_AUTO_MAX_POINTS)
        }
    }
}

/**
 * How to pick a subset of points when [LineRenderSpec.maxPointsPerSeries] is exceeded.
 */
enum class LineDecimationStrategy {
    /** Even index spacing (always keeps first and last of the visible window). */
    Uniform,

    /**
     * Splits the visible x-span into buckets and keeps the points with min and max y in each bucket,
     * preserving spikes better than [Uniform] for large datasets.
     */
    MinMaxY,
}
