package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.WaterfallEntry

/**
 * One drawable waterfall bar from [ChartPlanner] (pixel geometry) plus logical identity for selection.
 * [index] is the entry's position in the sequence.
 *
 * @property bounds The floating bar rectangle in plot pixels.
 * @property startValue Running total before this step (bar's baseline value; `0` for totals).
 * @property endValue Running total the bar reaches (`= startValue + delta`, or the accumulated total for a total bar).
 * @property cumulativeAfter Running total after this step (unchanged by total markers); used for connectors.
 * @property isTotal Whether this bar is a total / subtotal marker.
 * @property isIncrease Whether this step increased the running total (`delta >= 0`).
 * @property color Resolved bar fill color.
 */
data class PlannedWaterfallBar(
    val index: Int,
    val entry: WaterfallEntry,
    val bounds: RectD,
    val startValue: Double,
    val endValue: Double,
    val cumulativeAfter: Double,
    val isTotal: Boolean,
    val isIncrease: Boolean,
    val color: ChartColor,
)
