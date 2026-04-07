package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedBarSegment
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import kotlin.math.max

/**
 * Resolves a tap to the first [BarHitTarget] whose [BarHitTarget.bounds] contains the touch point.
 *
 * Targets are usually axis-aligned bar rectangles in pixel space from the planner/renderer pipeline.
 */
object BarHitResolver {
    /**
     * Hit-tests segments from [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner] in **hit-test order** (top segment first), e.g.
     * [com.mena97villalobos.composeplotlib.core.layout.BarChartPlan.barSegmentsForHitTest] or
     * [com.mena97villalobos.composeplotlib.core.layout.forHitTestFromDrawOrder] on draw-ordered lists.
     */
    fun resolvePlanned(
        segmentsHitTestFirst: List<PlannedBarSegment>,
        touchPoint: OffsetD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit =
            segmentsHitTestFirst.firstOrNull { segment ->
                segment.bounds.isValidForAxisAlignedHit() && segment.bounds.contains(touchPoint)
            } ?: return null
        val topCenter =
            when (hit.orientation) {
                BarChartOrientation.Vertical ->
                    OffsetD((hit.bounds.left + hit.bounds.right) / 2.0, hit.bounds.top)
                BarChartOrientation.Horizontal -> {
                    val outerX = max(hit.bounds.left, hit.bounds.right)
                    OffsetD(outerX, (hit.bounds.top + hit.bounds.bottom) / 2.0)
                }
            }
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = hit.groupId,
                    pointIndex = hit.categoryIndex,
                    point = XyPoint(hit.entry.x, hit.entry.y),
                    layerId = hit.layerId,
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bounds.center,
            hitBounds = hit.bounds,
        )
    }

    /** @return a [ChartSelection] for the hit bar, or `null` if no bar contains [touchPoint]. */
    fun resolve(
        seriesId: String,
        bars: List<BarHitTarget>,
        touchPoint: OffsetD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit =
            bars.firstOrNull { target ->
                target.bounds.isValidForAxisAlignedHit() && target.bounds.contains(touchPoint)
            } ?: return null
        val topCenter =
            OffsetD(
                x = (hit.bounds.left + hit.bounds.right) / 2.0,
                y = hit.bounds.top,
            )
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = seriesId,
                    pointIndex = hit.pointIndex,
                    point = hit.point,
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bounds.center,
            hitBounds = hit.bounds,
        )
    }
}

/** True when [RectD] edges are finite and axis-aligned with `left` ≤ `right`, `top` ≤ `bottom`. */
private fun RectD.isValidForAxisAlignedHit(): Boolean =
    left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite() &&
        left <= right && top <= bottom

/** Precomputed bar geometry and domain values for hit-testing. */
data class BarHitTarget(
    val pointIndex: Int,
    val point: XyPoint,
    val bounds: RectD,
)
