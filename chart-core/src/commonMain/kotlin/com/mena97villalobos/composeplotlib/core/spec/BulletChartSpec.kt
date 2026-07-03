package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One qualitative range band of a bullet graph, spanning from the previous band's end (or [BulletChartSpec.domainMin]
 * for the first band) up to [end].
 *
 * @property end Upper domain bound of this band.
 * @property color Fill color (typically light → dark for worse → better, or a traffic-light scheme).
 */
data class BulletBand(
    val end: Double,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **bullet graph** — a compact linear KPI display (Stephen Few's design): qualitative
 * range [bands] behind a featured **measure** bar ([value]) and an optional **target** marker. Horizontal, read-only
 * (no axes / selection / pan-zoom), like the gauge. `chart-compose`'s `BulletChart` shows the title and value.
 *
 * @property value The featured measure (the bar length).
 * @property bands Qualitative background ranges, in ascending [BulletBand.end] order.
 * @property target Optional comparative marker (e.g. a goal) drawn as a perpendicular line; null = none.
 * @property domainMin Lower domain bound of the track; defaults to `0.0`.
 * @property domainMax Upper domain bound of the track.
 * @property measureColor Measure-bar fill; defaults to near-black.
 * @property targetColor Target marker color; defaults to red.
 * @property measureThicknessFraction Measure-bar thickness as a fraction of the track height (0..1).
 * @property targetThicknessPx Target marker stroke width in pixels.
 * @property id Chart identity string.
 * @property showBoundLabels When true, draws [domainMin] / [domainMax] labels under the track ends.
 * @property valueFormatter Formats the bound labels.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property contentPadding Minimum padding around the track in **pixels**.
 */
data class BulletChartSpec(
    val value: Double,
    val bands: List<BulletBand>,
    val domainMax: Double,
    val target: Double? = null,
    val domainMin: Double = 0.0,
    val measureColor: ChartColor = ChartColor(0xFF1C1C1C),
    val targetColor: ChartColor = ChartColor(0xFFC62828),
    val measureThicknessFraction: Double = 0.4,
    val targetThicknessPx: Double = 3.0,
    val id: String = "bullet",
    val showBoundLabels: Boolean = true,
    val valueFormatter: (Double) -> String = DefaultAxisLabelFormatter,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 16.0, top = 16.0, right = 24.0, bottom = 28.0),
)
