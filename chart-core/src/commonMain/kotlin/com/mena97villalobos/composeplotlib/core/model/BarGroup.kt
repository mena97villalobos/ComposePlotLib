package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.BarStyle

/** A single bar positioned at domain x with height y. */
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
