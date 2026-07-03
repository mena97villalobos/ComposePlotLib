package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.MarkerShape

/**
 * One sample in a line or scatter series in data (domain) space.
 *
 * The `marker*` fields are **scatter-only** per-point overrides (line charts ignore them). Each falls back to the
 * series [com.mena97villalobos.composeplotlib.core.spec.PointStyle] when null. [markerRadius] also drives bubble sizing.
 *
 * The `yLow` / `yHigh` fields are optional **error-bar** bounds in domain space (absolute y of the lower / upper
 * whisker end), rendered when the series' spec carries an enabled [ErrorBarsSpec][com.mena97villalobos.composeplotlib.core.spec.ErrorBarsSpec].
 * Either may be set independently (asymmetric or one-sided); both null means no whisker.
 *
 * @property x Position along the X axis, in domain space.
 * @property y Position along the Y axis, in domain space.
 * @property markerShape Per-point glyph override, or null to use the series shape.
 * @property markerColor Per-point color override, or null to use the series color.
 * @property markerRadius Per-point radius override (bubble size), or null to use the series radius.
 * @property yLow Lower error-bar bound (absolute domain y), or null for no lower whisker.
 * @property yHigh Upper error-bar bound (absolute domain y), or null for no upper whisker.
 */
data class ChartPoint(
    val x: Double,
    val y: Double,
    val markerShape: MarkerShape? = null,
    val markerColor: ChartColor? = null,
    val markerRadius: Double? = null,
    val yLow: Double? = null,
    val yHigh: Double? = null,
) {
    /**
     * True when this sample is a **gap** (missing value): its [y] is non-finite (typically `NaN`). Gap points break the
     * line and area fill into separate segments unless [LineRenderSpec][com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec.connectAcrossGaps]
     * is on, and are excluded from data bounds, decimation, and selection.
     */
    val isGap: Boolean get() = !y.isFinite()

    companion object {
        /**
         * A missing-data marker at domain [x] (with `y = NaN`). In a line/area series the stroke and fill break here,
         * unless [LineRenderSpec.connectAcrossGaps][com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec.connectAcrossGaps]
         * bridges across gaps.
         */
        fun gap(x: Double): ChartPoint = ChartPoint(x, Double.NaN)
    }
}
