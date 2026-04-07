package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** Platform-agnostic draw instruction emitted by [ChartPlanner] and interpreted by the renderer. */
sealed interface DrawCommand {
    /** Straight segment in pixel space. */
    data class Line(
        val start: OffsetD,
        val end: OffsetD,
        val color: ChartColor,
        val strokeWidth: Double = 1.0,
        val strokeStyle: StrokeStyle? = null,
    ) : DrawCommand

    /** Connected path through [points] in order. */
    data class Polyline(
        val points: List<OffsetD>,
        val color: ChartColor,
        val strokeWidth: Double,
        val strokeStyle: StrokeStyle? = null,
    ) : DrawCommand

    /** Filled axis-aligned rectangle (bars, fills). */
    data class Rect(
        val bounds: RectD,
        val color: ChartColor,
    ) : DrawCommand

    /** Filled circle in pixel space (highlight knot). */
    data class Circle(
        val center: OffsetD,
        val radius: Double,
        val color: ChartColor,
    ) : DrawCommand

    /**
     * Axis tick label; [position] is **top-left** of the measured text box from the planner measurement pass.
     * When [firstBaselineFromTop] is set (from [TextLayoutMetrics]), the Compose renderer aligns the first line’s
     * baseline to `position.y + firstBaselineFromTop` after re-measurement so drawing matches planner geometry.
     * @param isYAxisTick When true, host renderers apply the primary left y-axis tick typography slot; combined charts set [isSecondaryYAxisTick] for the right axis.
     * @param isSecondaryYAxisTick When true, dual-*Y* combined charts: hosts should apply the secondary y-axis tick typography (from [com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec.yAxisSecondary] label style), not the primary y slot.
     */
    data class Text(
        val text: String,
        val position: OffsetD,
        val color: ChartColor,
        val isYAxisTick: Boolean = false,
        val isSecondaryYAxisTick: Boolean = false,
        /** When true, the Compose renderer uses pie slice label typography instead of axis labels. */
        val isPieSliceLabel: Boolean = false,
        val firstBaselineFromTop: Double? = null,
    ) : DrawCommand

    /**
     * Arbitrary path in plot pixels. First segment should be [PathSegment.MoveTo].
     */
    data class Path(
        val segments: List<PathSegment>,
        val style: PathDrawStyle,
    ) : DrawCommand

    /**
     * Clip child commands to [bounds] (does not draw by itself).
     * Nested clips intersect with outer clips from the renderer.
     */
    data class ClipRect(
        val bounds: RectD,
        val children: List<DrawCommand>,
    ) : DrawCommand

    /**
     * Applies [alpha] (0..1) to drawn [children]. By default the Compose renderer multiplies ARGB alpha on leaf
     * colors (nested [Group]s multiply). That approximates uniform transparency but is not identical
     * to layer compositing when overlapping translucent shapes are drawn; hosts may opt into save-layer blending
     * in the renderer (see chrome theme / adapter documentation).
     */
    data class Group(
        val alpha: Double,
        val children: List<DrawCommand>,
    ) : DrawCommand

    /**
     * Rounded rectangle with a uniform [cornerRadius] (same on all four corners).
     */
    data class RoundRect(
        val bounds: RectD,
        val cornerRadius: Double,
        val style: ShapeDrawStyle,
    ) : DrawCommand

    /**
     * Elliptical arc in pixel space; angles in **degrees** (0° = positive x, sweep toward positive y in screen coords).
     *
     * **Donut / ring sector:** When [innerRadius] is non-null and strictly between `0` and [radius], planners and
     * renderers treat this as the filled region between [innerRadius] and [radius] for the given angle span
     * (annulus sector). When [innerRadius] is null, behavior is unchanged: a pie wedge when [useCenter] is true, or
     * an arc perimeter stroke/fill when false.
     */
    data class Arc(
        val center: OffsetD,
        val radius: Double,
        val startAngleDegrees: Double,
        val sweepAngleDegrees: Double,
        val useCenter: Boolean,
        val style: ShapeDrawStyle,
        val innerRadius: Double? = null,
    ) : DrawCommand
}
