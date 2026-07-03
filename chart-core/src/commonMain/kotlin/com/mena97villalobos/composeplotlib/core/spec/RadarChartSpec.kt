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
    /** Ordered spoke labels; each [RadarSeries.values] list must match this size to be drawable (see [hasDrawableSeries]). */
    val axes: List<String>,
    /** Polygons to overlay; [RadarSeries.id]s must be unique (checked in `init`). */
    val series: List<RadarSeries>,
    /** Chart identity used for selection matching; see [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]. Defaults to `"radar"`. */
    val id: String = "radar",
    /**
     * Rotation for axis 0 in degrees (same convention as [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Arc]).
     * Default matches [PieChartLayoutMath.START_ANGLE_DEGREES] (12 o'clock).
     */
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    /** Grid rings, spokes, and axis-label toggles; see [RadarGridSpec]. */
    val grid: RadarGridSpec = RadarGridSpec(),
    /** Color for the spoke (axis) labels; see [AxisLabelStyle]. */
    val axisLabelStyle: AxisLabelStyle = AxisLabelStyle(),
    /** Host-agnostic accessibility strings; see [ChartAccessibilitySpec]. */
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    /** Legend visibility and styling; see [LegendSpec]. */
    val legend: LegendSpec = LegendSpec(),
    /** Highlight styling for the selected axis value; see [SelectionStyle]. */
    val selectionStyle: SelectionStyle = SelectionStyle(),
    /** Minimum padding around chart content in **pixels** (reserves space for legend and spoke labels). */
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 48.0, right = 24.0, bottom = 24.0),
    /** Marker / a11y text for a selected axis value. */
    val axisValueFormatter: (axisLabel: String, value: Double) -> String = DefaultRadarAxisValueFormatter,
) {
    init {
        requireUniqueIds(series.map { it.id }, "RadarChartSpec.series ids")
    }
}

/** One radar polygon: a value per axis plus its outline, vertex-marker, and optional fill styling. */
data class RadarSeries(
    /** Stable identifier used by selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]). */
    val id: String,
    /** Legend / accessibility name for the series; defaults to [id]. */
    val label: String = id,
    /** Values aligned to [RadarChartSpec.axes] order. */
    val values: List<Double>,
    /** Outline stroke (and area-fill anchor) for the polygon; see [LineStyle]. */
    val lineStyle: LineStyle = LineStyle(),
    /** Vertex marker glyphs at each axis point; see [PointStyle]. */
    val pointStyle: PointStyle = PointStyle(),
    /**
     * Optional polygon fill. When null, only the series outline is rendered.
     * Prefer encoding alpha in the ARGB color (e.g. `0x66RRGGBB`).
     */
    val fillColor: ChartColor? = null,
)

/** Background grid for a radar chart: concentric rings, radial spokes, and axis labels. */
data class RadarGridSpec(
    /** When false, no rings, spokes, or axis labels are drawn (values overlay bare background). */
    val enabled: Boolean = true,
    /** Number of polygon rings between center and outer radius (>= 0). */
    val ringCount: Int = 4,
    /** When true (default), draws a radial spoke from center to each axis. */
    val showSpokes: Boolean = true,
    /** When true (default), draws the [RadarChartSpec.axes] labels at each spoke tip. */
    val showAxisLabels: Boolean = true,
    /** Stroke for the concentric rings; see [GridLineStyle]. */
    val ringLineStyle: GridLineStyle = GridLineStyle(color = ChartColor.LightGray, strokeWidth = 1.0),
    /** Stroke for the radial spokes; see [GridLineStyle]. */
    val spokeLineStyle: GridLineStyle = GridLineStyle(color = ChartColor.LightGray, strokeWidth = 1.0),
)

/** True when there is at least one axis and one series whose values match [RadarChartSpec.axes] size with a finite entry. */
fun RadarChartSpec.hasDrawableSeries(): Boolean = axes.isNotEmpty() && series.any { it.values.size == axes.size && it.values.any { v -> v.isFinite() } }
