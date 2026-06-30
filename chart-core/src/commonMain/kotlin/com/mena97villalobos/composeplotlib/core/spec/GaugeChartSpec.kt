package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Immutable configuration for a gauge / progress-ring chart (radial; no cartesian axes).
 *
 * Renders a background **track** arc spanning [sweepDegrees] and a foreground **progress** arc covering
 * `sweepDegrees * progress`. A full ring uses `sweepDegrees = 360`; a smaller sweep (e.g. `270`) yields a
 * speedometer-style arc gauge. There is no selection or legend — a gauge shows a single read-only value.
 *
 * @property progress Fraction filled; clamped to `0.0..1.0` when planned.
 * @property strokeWidthPx Ring thickness in **pixels**.
 * @property roundedCaps When true, the progress arc is drawn with round end caps.
 * @property startAngleDegrees Arc start angle; default `-90` (12 o'clock), matching [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Arc].
 * @property sweepDegrees Total angular extent of the track and the span representing 100% progress.
 */
data class GaugeChartSpec(
    val progress: Double,
    val id: String = "gauge",
    val trackColor: ChartColor = ChartColor.LightGray,
    val progressColor: ChartColor = ChartColor.Blue,
    val strokeWidthPx: Double = 24.0,
    val roundedCaps: Boolean = true,
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    val sweepDegrees: Double = 360.0,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 24.0, right = 24.0, bottom = 24.0),
)

/** [GaugeChartSpec.progress] clamped to the valid `0.0..1.0` range. */
fun GaugeChartSpec.clampedProgress(): Double = progress.coerceIn(0.0, 1.0)

/** True when the gauge has a drawable (finite, non-zero) [GaugeChartSpec.sweepDegrees]. */
fun GaugeChartSpec.hasDrawableSweep(): Boolean = sweepDegrees.isFinite() && sweepDegrees != 0.0
