package com.mena97villalobos.composeplotlib.core.spec

/**
 * Major grid lines at axis tick positions. Vertical lines use x ticks; horizontal lines use y ticks.
 */
data class GridSpec(
    val showVerticalLines: Boolean = true,
    val showHorizontalLines: Boolean = true,
    val lineStyle: GridLineStyle = GridLineStyle(),
)
