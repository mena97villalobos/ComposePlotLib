package com.mena97villalobos.composeplotlib.chart.internal.canvas

import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme

/** Label typography + chrome passed through measurement and [ComposeCanvasAdapter]. */
internal data class ChartRenderStyles(
    val labels: ChartLabelTextStyles,
    val chrome: ChartChromeTheme,
)
