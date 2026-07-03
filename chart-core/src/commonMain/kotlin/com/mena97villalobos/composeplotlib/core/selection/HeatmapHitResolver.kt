package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.layout.PlannedHeatmapCell

/**
 * Resolves a tap point to the first [PlannedHeatmapCell] whose [PlannedHeatmapCell.bounds] contains it.
 *
 * Cells should be passed in **hit-test order** (topmost first), e.g.
 * [com.mena97villalobos.composeplotlib.core.layout.HeatmapChartPlan.cellsForHitTest].
 */
object HeatmapHitResolver {
    fun resolve(
        touchPoint: OffsetD,
        cells: List<PlannedHeatmapCell>,
        specId: String,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit = cells.firstOrNull { it.bounds.contains(touchPoint) } ?: return null
        return selectionFor(hit, specId)
    }

    /** Builds the [ChartSelection] for [cell]; shared by tap resolution and keyboard roam so they are identical. */
    internal fun selectionFor(
        cell: PlannedHeatmapCell,
        specId: String,
    ): ChartSelection =
        ChartSelection(
            target =
                HighlightTarget(
                    seriesId = specId,
                    pointIndex = cell.cellIndex,
                    point = XyPoint(cell.col.toDouble(), cell.row.toDouble()),
                ),
            pixelOffset = cell.bounds.center,
            markerAnchor = cell.bounds.center,
            hitBounds = cell.bounds,
        )
}
