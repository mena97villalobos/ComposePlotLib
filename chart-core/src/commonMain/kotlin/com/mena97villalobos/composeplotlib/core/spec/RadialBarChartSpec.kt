package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.math.max

/**
 * One concentric ring of a [RadialBarChartSpec]: a labeled value drawn as an arc whose sweep is proportional
 * to `value / maxValue`.
 *
 * @property label ring label (used in the legend).
 * @property value the ring's magnitude; clamped to `0..maxValue` when planned.
 * @property color the ring's foreground (value-arc) color.
 */
data class RadialBarTrack(
    val label: String,
    val value: Double,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **radial bar** (circular progress-bar) chart: a set of concentric rings, one per
 * [RadialBarTrack], each drawn as a background track plus a foreground value arc from [startAngleDegrees] spanning
 * `sweepDegrees * value / maxValue`. The first track is the outermost ring. Read-only (no selection / pan / zoom),
 * like the gauge and pie families; an optional legend labels the rings.
 *
 * @property tracks rings from outermost to innermost.
 * @property maxValue full-scale value each ring is measured against; `null` auto-scales to the largest track value.
 * @property id chart identity string.
 * @property startAngleDegrees arc start angle; default `-90` (12 o'clock).
 * @property sweepDegrees angular extent representing `maxValue`; `360` is a full ring.
 * @property trackThicknessPx ring thickness in pixels.
 * @property trackGapPx radial gap between adjacent rings in pixels.
 * @property backgroundTrackColor color of each ring's background track.
 * @property roundedCaps round the value-arc end caps when true.
 * @property legend legend configuration (ring labels + colors).
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding minimum padding around the chart in pixels.
 */
data class RadialBarChartSpec(
    val tracks: List<RadialBarTrack>,
    val maxValue: Double? = null,
    val id: String = "radialBar",
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    val sweepDegrees: Double = 360.0,
    val trackThicknessPx: Double = 18.0,
    val trackGapPx: Double = 6.0,
    val backgroundTrackColor: ChartColor = ChartColor.LightGray,
    val roundedCaps: Boolean = true,
    val legend: LegendSpec = LegendSpec(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 24.0, right = 24.0, bottom = 24.0),
)

/** Full-scale value each ring is measured against: [RadialBarChartSpec.maxValue], else the largest positive track value, else `1`. */
fun RadialBarChartSpec.resolvedMaxValue(): Double {
    val explicit = maxValue
    if (explicit != null && explicit.isFinite() && explicit > 0.0) return explicit
    val autoMax = tracks.maxOfOrNull { max(it.value, 0.0) } ?: 0.0
    return if (autoMax > 0.0) autoMax else 1.0
}

/** True when the chart has finite, non-zero sweep. */
fun RadialBarChartSpec.hasDrawableSweep(): Boolean = sweepDegrees.isFinite() && sweepDegrees != 0.0
