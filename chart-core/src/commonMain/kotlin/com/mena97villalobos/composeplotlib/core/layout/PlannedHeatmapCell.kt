package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Resolved geometry for a single heatmap cell emitted by [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planHeatmapChart].
 *
 * @param col Column index of the original [com.mena97villalobos.composeplotlib.core.model.HeatmapCell].
 * @param row Row index of the original [com.mena97villalobos.composeplotlib.core.model.HeatmapCell].
 * @param value Raw data value (for marker / tooltip formatting).
 * @param color Resolved fill color from the spec's [com.mena97villalobos.composeplotlib.core.spec.HeatmapColorScale].
 * @param bounds Axis-aligned pixel rectangle used for drawing and hit-testing.
 * @param cellIndex Original index in [com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec.cells], used as [com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.pointIndex].
 */
data class PlannedHeatmapCell(
    val col: Int,
    val row: Int,
    val value: Double,
    val color: ChartColor,
    val bounds: RectD,
    val cellIndex: Int,
)
