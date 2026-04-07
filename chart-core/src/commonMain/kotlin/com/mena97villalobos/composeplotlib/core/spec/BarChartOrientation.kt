package com.mena97villalobos.composeplotlib.core.spec

/**
 * Pixel layout for bar charts. Domain data stays in [com.mena97villalobos.composeplotlib.core.model.BarEntry]
 * (`x` = category position, `y` = value); orientation only swaps which axis is category vs value in the planner.
 */
enum class BarChartOrientation {
    /**
     * Columns: category on the chart X axis, value on Y (bars grow vertically from the viewport Y baseline).
     */
    Vertical,

    /**
     * Rows: category on the chart Y axis, value on X (bars grow horizontally from the viewport X baseline).
     */
    Horizontal,
}
