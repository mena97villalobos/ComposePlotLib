package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Styling for **error bars** (uncertainty whiskers): a vertical line spanning a datum's low/high bounds with a
 * short horizontal cap at each supplied end.
 *
 * Whiskers are drawn for line / scatter points that set
 * [yLow][com.mena97villalobos.composeplotlib.core.model.ChartPoint.yLow] /
 * [yHigh][com.mena97villalobos.composeplotlib.core.model.ChartPoint.yHigh] and for **vertical** bar entries that set
 * [yLow][com.mena97villalobos.composeplotlib.core.model.BarEntry.yLow] /
 * [yHigh][com.mena97villalobos.composeplotlib.core.model.BarEntry.yHigh]. A datum with neither bound draws nothing,
 * so attaching a default `ErrorBarsSpec` to a spec is a no-op until points carry bounds. Horizontal bar charts do
 * not draw error bars yet.
 *
 * @property enabled When false, no whiskers are drawn regardless of per-datum bounds.
 * @property color Whisker + cap stroke color; defaults to a dark neutral gray.
 * @property strokeWidth Stroke width in pixels for the whisker line and caps.
 * @property capWidthPx Total width in pixels of each horizontal end cap (centered on the whisker).
 */
data class ErrorBarsSpec(
    val enabled: Boolean = true,
    val color: ChartColor = ChartColor(0xFF424242),
    val strokeWidth: Double = 1.5,
    val capWidthPx: Double = 8.0,
)
