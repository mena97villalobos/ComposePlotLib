package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedCandle

/**
 * Resolves a tap to the first [PlannedCandle] whose [PlannedCandle.hitBounds] (spanning high..low)
 * contains the touch point.
 */
object CandlestickHitResolver {
    /**
     * Hit-tests candles from [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner] in **hit-test order**
     * (top candle first), e.g. [com.mena97villalobos.composeplotlib.core.layout.CandlestickChartPlan.candlesForHitTest].
     */
    fun resolvePlanned(
        candlesHitTestFirst: List<PlannedCandle>,
        touchPoint: OffsetD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit =
            candlesHitTestFirst.firstOrNull { candle ->
                candle.hitBounds.isValidForAxisAlignedHit() && candle.hitBounds.contains(touchPoint)
            } ?: return null
        val topCenter = OffsetD((hit.bodyBounds.left + hit.bodyBounds.right) / 2.0, hit.bodyBounds.top)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = hit.seriesId,
                    pointIndex = hit.index,
                    point = XyPoint(hit.point.x, hit.point.close),
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bodyBounds.center,
            hitBounds = hit.hitBounds,
        )
    }
}

/** True when [RectD] edges are finite and axis-aligned with `left` ≤ `right`, `top` ≤ `bottom`. */
private fun RectD.isValidForAxisAlignedHit(): Boolean =
    left.isFinite() &&
        top.isFinite() &&
        right.isFinite() &&
        bottom.isFinite() &&
        left <= right &&
        top <= bottom
