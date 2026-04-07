package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD

/**
 * User selection result: a logical [target] plus pixel geometry for drawing and overlays.
 *
 * **Bar charts:** [pixelOffset] is the top-center of the selection accent (top edge of the bar segment).
 * [markerAnchor] is usually the **center** of the bar rectangle ([hitBounds]) for tooltips/callouts.
 * [hitBounds] is the bar segment’s axis-aligned bounds in plot pixels (same as [com.mena97villalobos.composeplotlib.core.layout.PlannedBarSegment.bounds]).
 *
 * @param pixelOffset Reference pixel for chart highlights (e.g. line-chart knot, top-center of a bar stroke).
 * @param markerAnchor Optional anchor for tooltips/callouts; defaults to [pixelOffset] when null (see [markerPosition]).
 * @param hitBounds Optional axis-aligned bounds of the hit region in plot pixels (markers, a11y geometry).
 */
data class ChartSelection(
    val target: HighlightTarget,
    val pixelOffset: OffsetD,
    val markerAnchor: OffsetD? = null,
    val hitBounds: RectD? = null,
) {
    fun markerPosition(): OffsetD = markerAnchor ?: pixelOffset
}
