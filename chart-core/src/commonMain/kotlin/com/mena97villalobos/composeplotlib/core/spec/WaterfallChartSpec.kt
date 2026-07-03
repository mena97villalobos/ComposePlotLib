package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.WaterfallEntry

/**
 * Immutable configuration for a **waterfall** chart: an ordered [WaterfallEntry] sequence over a category X axis and
 * a value Y axis. Each step's bar floats between the running total before and after it; increases, decreases, and
 * total markers are colored distinctly, with optional connector lines between consecutive bars.
 *
 * @property entries Ordered steps; plotted at integer category positions (x = index).
 * @property increaseColor Bar fill for steps with a non-negative delta.
 * @property decreaseColor Bar fill for steps with a negative delta.
 * @property totalColor Bar fill for [WaterfallEntry.isTotal] markers.
 * @property barWidthPx Bar width in pixels (centered on each category position).
 * @property connectors When true, draws a connector line from each bar's running-total level to the next bar.
 * @property connectorColor Connector line color.
 * @property connectorStrokeWidth Connector line width in pixels.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (category) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (value) axis appearance and tick behavior; see [AxisSpec].
 * @property grid Background grid line configuration; see [GridSpec].
 * @property selectionStyle Highlight styling for the selected bar; see [SelectionStyle].
 * @property contentPadding Minimum padding around chart content in **pixels**.
 */
data class WaterfallChartSpec(
    val entries: List<WaterfallEntry>,
    val increaseColor: ChartColor = ChartColor.Green,
    val decreaseColor: ChartColor = ChartColor.Red,
    val totalColor: ChartColor = ChartColor(0xFF1565C0),
    val barWidthPx: Double = 28.0,
    val connectors: Boolean = true,
    val connectorColor: ChartColor = ChartColor.Gray,
    val connectorStrokeWidth: Double = 1.0,
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val grid: GridSpec = GridSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
)
