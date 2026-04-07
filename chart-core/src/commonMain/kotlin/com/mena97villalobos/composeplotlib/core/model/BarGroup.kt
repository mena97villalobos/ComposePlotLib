package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.BarStyle

/**
 * One bar sample: **x** is the category position (plotted on chart X for
 * [com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation.Vertical],
 * on chart Y for [com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation.Horizontal]);
 * **y** is the value (plotted on chart Y / X respectively).
 */
data class BarEntry(
    val x: Double,
    val y: Double,
)

/**
 * A grouped series for bar charts (one row in the legend, one [BarStyle]).
 *
 * @property id Used for selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 */
data class BarGroup(
    val id: String,
    val label: String,
    val entries: List<BarEntry>,
    val style: BarStyle = BarStyle(),
)
