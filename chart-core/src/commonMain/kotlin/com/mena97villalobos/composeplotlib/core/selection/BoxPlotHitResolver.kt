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
        val topCenter = OffsetD((hit.boxBounds.left + hit.boxBounds.right) / 2.0, hit.boxBounds.top)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = hit.seriesId,
                    pointIndex = hit.index,
                    point = XyPoint(hit.entry.x, hit.entry.median),
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.boxBounds.center,
            hitBounds = hit.hitBounds,
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
