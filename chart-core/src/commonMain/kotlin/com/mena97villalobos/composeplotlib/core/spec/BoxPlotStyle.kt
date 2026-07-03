package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Appearance of a box-and-whisker series: the Q1–Q3 box (fill + border), the median line, the whiskers with end
 * caps, and outlier points.
 *
 * @property boxColor Fill color of the box; drawn at [boxFillAlpha].
 * @property boxFillAlpha Alpha (0..1) applied to [boxColor] for the box fill.
 * @property borderColor Box border stroke color.
 * @property medianColor Median line color.
 * @property whiskerColor Whisker + cap stroke color.
 * @property strokeWidth Stroke width (pixels) for box border, whiskers, caps, and the median line.
 * @property boxWidthPx Box width in pixels (centered on the category position).
 * @property whiskerCapFraction Whisker cap width as a fraction of [boxWidthPx] (0..1).
 * @property outlierColor Outlier point color.
 * @property outlierRadius Outlier point radius in pixels.
 */
data class BoxPlotStyle(
    val boxColor: ChartColor = ChartColor.Blue,
    val boxFillAlpha: Double = 0.35,
    val borderColor: ChartColor = ChartColor(0xFF1565C0),
    val medianColor: ChartColor = ChartColor.Black,
    val whiskerColor: ChartColor = ChartColor(0xFF616161),
    val strokeWidth: Double = 1.5,
    val boxWidthPx: Double = 24.0,
    val whiskerCapFraction: Double = 0.6,
    val outlierColor: ChartColor = ChartColor.Red,
    val outlierRadius: Double = 2.5,
)
