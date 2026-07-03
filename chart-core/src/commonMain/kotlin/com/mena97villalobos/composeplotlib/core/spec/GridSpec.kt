package com.mena97villalobos.composeplotlib.core.spec

/**
 * Major grid lines at axis tick positions. Vertical lines use x ticks; horizontal lines use y ticks.
 *
 * @property showVerticalLines When true (default), draws a vertical grid line at each x tick.
 * @property showHorizontalLines When true (default), draws a horizontal grid line at each y tick.
 * @property lineStyle Stroke color, width, and optional dash pattern for the grid lines; see [GridLineStyle].
 */
data class GridSpec(
    val showVerticalLines: Boolean = true,
    val showHorizontalLines: Boolean = true,
    val lineStyle: GridLineStyle = GridLineStyle(),
)
