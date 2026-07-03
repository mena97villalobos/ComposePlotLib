package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Crosshair / axis-pointer styling. When [enabled] and a selection is active, the chart draws guideline(s) through
 * the selected point and, optionally, the selected value on each axis as a small chip at the plot's inner edge
 * (labels are drawn inside the plot so they stay within the plot clip). Attach via `crosshair` on `LineChartSpec` /
 * `ScatterChartSpec`. When enabled it replaces the plain vertical selection guideline; the selection knot still
 * uses [SelectionStyle].
 *
 * @property enabled Master switch; when false the chart keeps the plain vertical selection guideline.
 * @property showVerticalLine Draw the vertical guideline through the selection x.
 * @property showHorizontalLine Draw the horizontal guideline through the selection y.
 * @property showAxisLabels Draw the selected x value at the bottom edge and the y value at the left edge.
 * @property lineColor Guideline stroke color.
 * @property strokeWidth Guideline stroke width in pixels.
 * @property dashIntervals Optional on/off dash lengths; `null` = solid. Defaults to a dashed look.
 * @property labelColor Axis-pointer label text color.
 * @property labelBackground Optional filled chip behind each axis label; `null` draws text only.
 */
data class CrosshairSpec(
    val enabled: Boolean = false,
    val showVerticalLine: Boolean = true,
    val showHorizontalLine: Boolean = true,
    val showAxisLabels: Boolean = true,
    val lineColor: ChartColor = ChartColor(0xFF616161),
    val strokeWidth: Double = 1.0,
    val dashIntervals: List<Double>? = listOf(4.0, 4.0),
    val labelColor: ChartColor = ChartColor.White,
    val labelBackground: ChartColor? = ChartColor(0xFF616161),
)
