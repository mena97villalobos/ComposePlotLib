package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode

/**
 * One drawable bar segment from [ChartPlanner] (pixel [bounds]) plus logical identity for selection.
 *
 * [categoryIndex] is the entry index within the owning [com.mena97villalobos.composeplotlib.core.model.BarGroup].
 */
data class PlannedBarSegment(
    val groupId: String,
    val categoryIndex: Int,
    val entry: BarEntry,
    val bounds: RectD,
    val fillColor: ChartColor,
    val orientation: BarChartOrientation = BarChartOrientation.Vertical,
    val barLayoutMode: BarLayoutMode = BarLayoutMode.Independent,
    val layerId: String? = null,
)

/**
 * Converts **draw-ordered** bar segments to **hit-test order** (topmost segment first).
 *
 * The list [ChartPlanner] produces for drawing is **bottom → top** at overlaps (e.g. stacked bars:
 * lower slice first in the list, upper slice last). [BarHitResolver.resolvePlanned] must receive
 * segments in **top-first** order so the correct slice wins when rects overlap in pixel space.
 *
 * @see BarChartPlan.barSegments
 * @see CombinedHitTestLayer.Bars.segmentsDrawOrder
 */
fun List<PlannedBarSegment>.forHitTestFromDrawOrder(): List<PlannedBarSegment> = asReversed()
