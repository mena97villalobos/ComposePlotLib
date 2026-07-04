package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** One category of a [ViolinChartSpec]: a sample of raw values whose distribution is drawn as a violin. */
data class ViolinCategory(
    val label: String,
    val samples: List<Double>,
    val color: ChartColor,
)

/**
 * Immutable configuration for a **violin** chart: for each [ViolinCategory], a mirrored kernel-density shape drawn
 * along the value (y) axis, centered on the category's x slot, with half-width proportional to the estimated density.
 * A distribution-shape alternative to the box plot. Cartesian (categorical x, numeric y); read-only (no selection).
 *
 * @property categories the sampled categories, in x order.
 * @property resolution number of density evaluation points per violin (vertical smoothness).
 * @property violinWidth full violin width as a fraction of the category slot (`0..1`).
 * @property showMedian draw a short median line across each violin.
 * @property medianColor color of the median line.
 * @property id chart identity string.
 * @property xAxis / @property yAxis axis styling (x is forced to a category scale from the labels).
 * @property grid grid styling.
 * @property accessibility host-agnostic accessibility strings.
 * @property contentPadding space reserved for axis labels in pixels.
 */
data class ViolinChartSpec(
    val categories: List<ViolinCategory>,
    val resolution: Int = 48,
    val violinWidth: Double = 0.8,
    val showMedian: Boolean = true,
    val medianColor: ChartColor = ChartColor(0xFF37474F),
    val id: String = "violin",
    val xAxis: AxisSpec = AxisSpec(showGridLines = false),
    val yAxis: AxisSpec = AxisSpec(),
    val grid: GridSpec = GridSpec(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 24.0, right = 24.0, bottom = 40.0),
)
