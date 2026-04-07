package com.mena97villalobos.composeplotlib.core.viewport

/**
 * **Stable public API** — optional axis locking after pan/zoom: keeps one axis pinned to the chart’s full
 * domain range on that axis while the other axis follows gestures.
 *
 * Used by the Compose layer for horizontal-only or vertical-only exploration and by gesture code so pan/zoom
 * on a locked axis are ignored.
 *
 * Update at runtime with [com.mena97villalobos.composeplotlib.chart.state.ChartViewportState.updateAxisPolicy].
 *
 * See **docs/viewport-configuration.md** for presets ([HorizontalExploration], [VerticalExploration]) and examples.
 */
data class ViewportAxisPolicy(
    /**
     * When true, after each update the visible y-range is replaced with [ChartViewportState.domain].yRange.
     */
    val lockYToDomain: Boolean = false,
    /**
     * When true, after each update the visible x-range is replaced with [ChartViewportState.domain].xRange.
     */
    val lockXToDomain: Boolean = false,
) {
    companion object {
        /** Both axes follow gestures (default). */
        val Free = ViewportAxisPolicy()

        /** Y always shows full domain; only X pans/zooms (common for time-series strips). */
        val HorizontalExploration = ViewportAxisPolicy(lockYToDomain = true)

        /** X always shows full domain; only Y pans/zooms. */
        val VerticalExploration = ViewportAxisPolicy(lockXToDomain = true)
    }
}
