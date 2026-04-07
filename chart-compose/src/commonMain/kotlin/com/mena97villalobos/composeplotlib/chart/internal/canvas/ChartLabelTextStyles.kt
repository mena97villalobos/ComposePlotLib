package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.ui.text.TextStyle

/**
 * Compose [TextStyle]s used for measurement and drawing so font size/scale match across both passes.
 */
internal data class ChartLabelTextStyles(
    val xAxis: TextStyle,
    val yAxis: TextStyle,
    val legend: TextStyle,
    val pieSliceLabel: TextStyle,
    /** Used when measuring combined (or future) right-hand Y tick strings. */
    val yAxisSecondaryTick: TextStyle,
)
