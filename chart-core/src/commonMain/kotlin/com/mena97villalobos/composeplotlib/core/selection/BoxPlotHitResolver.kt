package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedBox

/** Resolves a tap to the first [PlannedBox] whose [PlannedBox.hitBounds] (spanning min..max) contains the touch. */
object BoxPlotHitResolver {
    /** Hit-tests boxes in **hit-test order** (top box first), e.g. `BoxPlotChartPlan.boxesForHitTest()`. */
    fun resolvePlanned(
        boxesHitTestFirst: List<PlannedBox>,
        touchPoint: OffsetD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit =
            boxesHitTestFirst.firstOrNull { box ->
                box.hitBounds.isValidForAxisAlignedHit() && box.hitBounds.contains(touchPoint)
            } ?: return null
        return selectionFor(hit)
    }

    /** Builds the [ChartSelection] for [box]; shared by tap resolution and keyboard roam so they are identical. */
    internal fun selectionFor(box: PlannedBox): ChartSelection {
        val topCenter = OffsetD((box.boxBounds.left + box.boxBounds.right) / 2.0, box.boxBounds.top)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = box.seriesId,
                    pointIndex = box.index,
                    point = XyPoint(box.entry.x, box.entry.median),
                ),
            pixelOffset = topCenter,
            markerAnchor = box.boxBounds.center,
            hitBounds = box.hitBounds,
        )
    }
}

private fun RectD.isValidForAxisAlignedHit(): Boolean =
    left.isFinite() &&
        top.isFinite() &&
        right.isFinite() &&
        bottom.isFinite() &&
        left <= right &&
        top <= bottom
