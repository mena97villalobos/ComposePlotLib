package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** Default marker / a11y formatter for one radar axis value. */
val DefaultRadarAxisValueFormatter: (axisLabel: String, value: Double) -> String = { axis, v -> "$axis: $v" }

/**
 * Immutable configuration for radar / spider charts (radial; no cartesian axes).
 *
 * **Axes:** [axes] defines the ordered spokes. Each [RadarSeries.values] list must match [axes] size.
 *
 * **Selection:** Uses [RadarSeries.id] as [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId] and
 * the axis index as [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.pointIndex] (same convention as
 * pie slices and bar categories until a dedicated radar target type exists).
 */
data class RadarChartSpec(
    val axes: List<String>,
    val series: List<RadarSeries>,
    val id: String = "radar",
    /**
     * Rotation for axis 0 in degrees (same convention as [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Arc]).
     * Default matches [PieChartLayoutMath.START_ANGLE_DEGREES] (12 o'clock).
     */
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    val grid: RadarGridSpec = RadarGridSpec(),
    val axisLabelStyle: AxisLabelStyle = AxisLabelStyle(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 48.0, right = 24.0, bottom = 24.0),
    /** Marker / a11y text for a selected axis value. */
    val axisValueFormatter: (axisLabel: String, value: Double) -> String = DefaultRadarAxisValueFormatter,
)

data class RadarSeries(
    /** Stable identifier used by selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]). */
    val id: String,
    val label: String = id,
    /** Values aligned to [RadarChartSpec.axes] order. */
    val values: List<Double>,
    val lineStyle: LineStyle = LineStyle(),
    val pointStyle: PointStyle = PointStyle(),
    /**
     * Optional polygon fill. When null, only the series outline is rendered.
     * Prefer encoding alpha in the ARGB color (e.g. `0x66RRGGBB`).
     */
    val fillColor: ChartColor? = null,
)

data class RadarGridSpec(
    val enabled: Boolean = true,
    /** Number of polygon rings between center and outer radius (>= 0). */
    val ringCount: Int = 4,
    val showSpokes: Boolean = true,
    val showAxisLabels: Boolean = true,
    val ringLineStyle: GridLineStyle = GridLineStyle(color = ChartColor.LightGray, strokeWidth = 1.0),
    val spokeLineStyle: GridLineStyle = GridLineStyle(color = ChartColor.LightGray, strokeWidth = 1.0),
)

fun RadarChartSpec.hasDrawableSeries(): Boolean =
    axes.isNotEmpty() && series.any { it.values.size == axes.size && it.values.any { v -> v.isFinite() } }

