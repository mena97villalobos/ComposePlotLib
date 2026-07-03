package com.mena97villalobos.composeplotlib.core.spec

/**
 * Flow direction for [SankeyChartSpec].
 *
 * Unlike [BarChartOrientation] (which names the bar growth axis), this names the axis the **flow**
 * travels along. Layout knobs on [SankeyLayoutSpec] are defined in flow/cross-axis terms and are
 * reinterpreted accordingly: `nodeWidth` is the node extent along the flow axis, `nodeGap` separates
 * nodes along the cross axis, and [SankeyColumnAlignment] anchors each column's node stack along the
 * cross axis.
 */
enum class SankeyOrientation {
    /** Flow along the x axis: columns advance left-to-right; nodes in a column stack top-to-bottom. Default. */
    Horizontal,

    /** Flow along the y axis: columns become rows advancing top-to-bottom; nodes in a row stack left-to-right. */
    Vertical,
}
