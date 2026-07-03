package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.BarStyle

/**
 * One bar sample: **x** is the category position (plotted on chart X for
 * [com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation.Vertical],
 * on chart Y for [com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation.Horizontal]);
 * **y** is the value (plotted on chart Y / X respectively).
 *
 * The `yLow` / `yHigh` fields are optional **error-bar** bounds (absolute value along the value axis), rendered for
 * **vertical** bar charts whose spec carries an enabled
 * [ErrorBarsSpec][com.mena97villalobos.composeplotlib.core.spec.ErrorBarsSpec]. Either may be set independently;
 * both null means no whisker. (Horizontal bar charts do not draw error bars yet.)
 *
 * @property x Category position along the bar's category axis, in domain space.
 * @property y Bar value along the value axis, in domain space.
 * @property yLow Lower error-bar bound (absolute value), or null for no lower whisker.
 * @property yHigh Upper error-bar bound (absolute value), or null for no upper whisker.
 */
data class BarEntry(
    val x: Double,
    val y: Double,
    val yLow: Double? = null,
    val yHigh: Double? = null,
)

/**
 * A grouped series for bar charts (one row in the legend, one [BarStyle]).
 *
 * @property id Used for selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 * @property label Human-readable name shown in the legend for this group.
 * @property entries The bar samples in this group; see [BarEntry].
 * @property style Fill and width for this group's bars; defaults to [BarStyle].
 */
data class BarGroup(
    val id: String,
    val label: String,
    val entries: List<BarEntry>,
    val style: BarStyle = BarStyle(),
)
