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
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = specId,
                    pointIndex = hit.cellIndex,
                    point = XyPoint(hit.col.toDouble(), hit.row.toDouble()),
                ),
            pixelOffset = hit.bounds.center,
            markerAnchor = hit.bounds.center,
            hitBounds = hit.bounds,
        )
    }
}
