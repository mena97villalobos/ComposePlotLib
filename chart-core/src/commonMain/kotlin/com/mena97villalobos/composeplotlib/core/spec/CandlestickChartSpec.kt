package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries

/**
 * Immutable configuration for a candlestick / OHLC chart: [CandlestickSeries] list, axes, grid, legend,
 * and formatting.
 *
 * @property series Logical series (each with [CandlestickSeries.points]); may be empty for no-data handling upstream.
 *   Series [ids][CandlestickSeries.id] must be unique (checked in `init`).
 * @property candleRender Visibility window and decimation policy (see [CandlestickRenderSpec]).
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X (horizontal) axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y (vertical, price) axis appearance and tick behavior; see [AxisSpec].
 * @property valueFormatter Formats the selected [com.mena97villalobos.composeplotlib.core.model.OHLCPoint] for markers.
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Crosshair / highlight styling for the selected candle; see [SelectionStyle].
 * @property contentPadding Minimum padding around chart content in **pixels** (layout reserves space for axes/legend).
 */
data class CandlestickChartSpec(
    val series: List<CandlestickSeries>,
    val candleRender: CandlestickRenderSpec = CandlestickRenderSpec(),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val valueFormatter: CandlestickValueFormatter = DefaultCandlestickValueFormatter,
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "CandlestickChartSpec.series ids")
    }
}
