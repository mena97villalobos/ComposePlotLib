package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice

/** Default [PieChartSpec.sliceValueFormatter] — label and raw value. */
val DefaultPieSliceValueFormatter: (PieChartSlice) -> String = { s -> "${s.label}: ${s.value}" }

/**
 * Immutable configuration for pie / donut charts (radial; no cartesian axes).
 *
 * **Pie vs donut:** Use [holeRadiusRatio]. `null`, omitted, or `0` yields a full pie (filled to the center).
 * A ratio in `(0..1)` sets the inner hole radius as that fraction of the **outer** plot radius, producing a donut.
 *
 * **Selection:** Use [id] as [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId] and the
 * slice index as [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.pointIndex] (same convention as
 * bar category indices until a dedicated pie target type exists).
 *
 * There are no axis ticks for this family; planner output uses empty tick lists and no axis lines.
 *
 * **Rotation / gaps:** [startAngleDegrees] rotates where the first drawable slice begins; [sliceGapDegrees] inserts
 * empty angular space between adjacent **positive** slices (callout labels in [sliceLabels] still use mid-angle of
 * each drawable wedge; very large gaps shrink wedges—see [com.mena97villalobos.composeplotlib.core.layout.PieSweep]).
 */
data class PieChartSpec(
    /** Wedges to draw in order; only slices with a positive [PieChartSlice.value] contribute span (see [hasDrawableSlices]). */
    val slices: List<PieChartSlice>,
    /** Chart identity used for selection matching; see [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]. Defaults to `"pie"`. */
    val id: String = "pie",
    /**
     * Inner hole size as a fraction of the outer radius in `[0..1)`.
     * `null` or `0` means a standard pie; larger values widen the hole (donut).
     */
    val holeRadiusRatio: Double? = null,
    /**
     * First drawable slice starts at this angle in degrees (same convention as [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Arc]).
     * Default matches [PieChartLayoutMath.START_ANGLE_DEGREES] (12 o'clock).
     */
    val startAngleDegrees: Double = PieChartLayoutMath.START_ANGLE_DEGREES,
    /**
     * Empty degrees between each pair of adjacent **positive** slices (including between last and first on the circle).
     * Coerced per slice so total gaps fit in 360°; `0` keeps a continuous pie.
     */
    val sliceGapDegrees: Double = 0.0,
    /** Host-agnostic accessibility strings; see [ChartAccessibilitySpec]. */
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    /** Legend visibility and styling; see [LegendSpec]. */
    val legend: LegendSpec = LegendSpec(),
    /** Highlight styling for the selected slice; see [SelectionStyle]. */
    val selectionStyle: SelectionStyle = SelectionStyle(),
    /** Minimum padding around chart content in **pixels** (reserves space for legend and callout labels). */
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 48.0, right = 24.0, bottom = 24.0),
    /** Marker / a11y text for the selected slice. */
    val sliceValueFormatter: (PieChartSlice) -> String = DefaultPieSliceValueFormatter,
    /** Optional slice labels and leader polylines drawn on the plot (off by default). */
    val sliceLabels: PieSliceLabelsSpec = PieSliceLabelsSpec(),
)

/** True when at least one slice has a positive [PieChartSlice.value] and can draw wedge span. */
fun PieChartSpec.hasDrawableSlices(): Boolean = slices.any { it.value > 0.0 }
