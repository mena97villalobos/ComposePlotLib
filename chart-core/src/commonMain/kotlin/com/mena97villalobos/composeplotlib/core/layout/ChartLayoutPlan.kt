package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** One axis tick: tick value, formatted [label], axis line position, and label placement plus metrics. */
data class AxisTick(
    val value: Double,
    val label: String,
    val axisPosition: OffsetD,
    /** Top-left of the label’s measured box (see [labelMetrics]). */
    val position: OffsetD,
    val labelMetrics: TextLayoutMetrics,
    /**
     * When false, grid/tick geometry stays but no [DrawCommand.Text] is emitted for this tick
     * (used when labels would overlap).
     */
    val showLabel: Boolean = true,
) {
    val labelSize: SizeD get() = labelMetrics.size
}

/** Legend row: label text, indicator color/center, and label anchor plus measured metrics. */
data class LegendEntryPlan(
    val label: String,
    val color: ChartColor,
    /** Matches [com.mena97villalobos.composeplotlib.core.spec.LegendStyle.indicatorRadius] used when planning layout. */
    val indicatorRadius: Double,
    val indicatorCenter: OffsetD,
    /** Top-left of the label box; vertical placement uses [labelMetrics] for baseline alignment with the swatch. */
    val labelPosition: OffsetD,
    val labelMetrics: TextLayoutMetrics,
) {
    val labelSize: SizeD get() = labelMetrics.size
}

/** Rectangular regions reserved for chrome (axes, legend) derived from [ChartLayoutPlan.plotBounds] insets. */
data class ChartReservedRegions(
    val contentInsets: InsetsD,
    val legendBounds: RectD?,
    val xAxisBounds: RectD?,
    val yAxisBounds: RectD?,
)

/**
 * Full frame plan: plot area, ticks, legend placement, and ordered [DrawCommand] lists.
 *
 * [plotDrawCommands] are drawn beneath axis labels; [overlayDrawCommands] draw axes and text on top.
 */
data class ChartLayoutPlan(
    val canvasBounds: RectD,
    val plotBounds: RectD,
    val reservedRegions: ChartReservedRegions,
    val xTicks: List<AxisTick>,
    val yTicks: List<AxisTick>,
    val legendEntries: List<LegendEntryPlan>,
    val plotDrawCommands: List<DrawCommand>,
    val overlayDrawCommands: List<DrawCommand>,
)
