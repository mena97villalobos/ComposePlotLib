package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint

/**
 * One drawable candle from [ChartPlanner] (pixel geometry) plus logical identity for selection.
 *
 * [index] is the **global** point index within the owning
 * [com.mena97villalobos.composeplotlib.core.model.CandlestickSeries] (preserved across decimation).
 *
 * @property bodyBounds Open/close body rectangle in plot pixels (floored to >= 1px tall for dojis).
 * @property hitBounds Tappable region spanning high..low and at least a minimum width.
 * @property wickTop Pixel point at the session **high** (smaller y in screen space).
 * @property wickBottom Pixel point at the session **low**.
 * @property isBull `true` when `close >= open`.
 * @property wickWidth Stroke width (pixels) for the high-low wick line.
 */
data class PlannedCandle(
    val seriesId: String,
    val index: Int,
    val point: OHLCPoint,
    val bodyBounds: RectD,
    val hitBounds: RectD,
    val wickTop: OffsetD,
    val wickBottom: OffsetD,
    val isBull: Boolean,
    val bodyColor: ChartColor,
    val wickColor: ChartColor,
    val wickWidth: Double,
)

/**
 * Converts **draw-ordered** candles to **hit-test order** (topmost candle first) so the correct candle
 * wins when hit regions overlap in pixel space.
 */
fun List<PlannedCandle>.forHitTestFromDrawOrder(): List<PlannedCandle> = asReversed()
