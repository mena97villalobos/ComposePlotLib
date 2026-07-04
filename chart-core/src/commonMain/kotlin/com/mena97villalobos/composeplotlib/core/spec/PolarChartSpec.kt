package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.math.max

/** One sector (direction / category) of a [PolarChartSpec]: a wedge whose radius is proportional to [value]. */
data class PolarSector(
    val label: String,
    val value: Double,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **polar bar / wind-rose** chart: [sectors] divide the circle into equal angular
 * slices, each drawn as a wedge whose radius is proportional to its value (`value / maxValue`). Optional concentric
 * grid rings aid reading. Read-only (no pan/zoom/selection).
 *
 * @property sectors the directions/categories, in angular order from [startAngleDegrees].
 * @property maxValue full-scale radius value; `null` auto-scales to the largest sector.
 * @property id chart identity string.
 * @property startAngleDegrees angle of the first sector's leading edge; default `-90` (12 o'clock).
 * @property gapDegrees angular gap between adjacent wedges.
 * @property showRings draw concentric grid rings.
 * @property ringCount number of grid rings when [showRings].
 * @property ringColor grid ring color.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding padding around the chart in pixels.
 */
data class PolarChartSpec(
    val sectors: List<PolarSector>,
    val maxValue: Double? = null,
    val id: String = "polar",
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    val gapDegrees: Double = 1.0,
    val showRings: Boolean = true,
    val ringCount: Int = 4,
    val ringColor: ChartColor = ChartColor(0xFFE0E0E0),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 16.0, top = 16.0, right = 16.0, bottom = 16.0),
)

/** Full-scale radius value: [PolarChartSpec.maxValue], else the largest positive sector value, else `1`. */
fun PolarChartSpec.resolvedMaxValue(): Double {
    val explicit = maxValue
    if (explicit != null && explicit.isFinite() && explicit > 0.0) return explicit
    val autoMax = sectors.maxOfOrNull { max(it.value, 0.0) } ?: 0.0
    return if (autoMax > 0.0) autoMax else 1.0
}
