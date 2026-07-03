package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedWaterfallBar

/** Resolves a tap to the first [PlannedWaterfallBar] whose [PlannedWaterfallBar.bounds] contains the touch. */
object WaterfallHitResolver {
    /** Series id used in [HighlightTarget] for the single logical waterfall sequence. */
    const val SERIES_ID: String = "waterfall"

    /** Hit-tests bars in **hit-test order** (top bar first), e.g. `WaterfallChartPlan.barsForHitTest()`. */
    fun resolvePlanned(
        barsHitTestFirst: List<PlannedWaterfallBar>,
        touchPoint: OffsetD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        val hit =
            barsHitTestFirst.firstOrNull { bar ->
                bar.bounds.isValidForAxisAlignedHit() && bar.bounds.contains(touchPoint)
            } ?: return null
        val topCenter = OffsetD((hit.bounds.left + hit.bounds.right) / 2.0, hit.bounds.top)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = SERIES_ID,
                    pointIndex = hit.index,
                    point = XyPoint(hit.index.toDouble(), hit.endValue),
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bounds.center,
            hitBounds = hit.bounds,
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
