package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.MarkerShape

/**
 * One sample in a line or scatter series in data (domain) space.
 *
 * The `marker*` fields are **scatter-only** per-point overrides (line charts ignore them). Each falls back to the
 * series [com.mena97villalobos.composeplotlib.core.spec.PointStyle] when null. [markerRadius] also drives bubble sizing.
 *
 * @property markerShape Per-point glyph override, or null to use the series shape.
 * @property markerColor Per-point color override, or null to use the series color.
 * @property markerRadius Per-point radius override (bubble size), or null to use the series radius.
 */
data class ChartPoint(
    val x: Double,
    val y: Double,
    val markerShape: MarkerShape? = null,
    val markerColor: ChartColor? = null,
    val markerRadius: Double? = null,
)
