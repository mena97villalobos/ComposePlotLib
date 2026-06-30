package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.domain.XyPoint

/**
 * One open/high/low/close sample in **domain** space.
 *
 * @property x Position along the category / time axis (plotted on chart X).
 * @property open Opening price.
 * @property high Session high (must be >= [low] for sensible geometry, but not enforced).
 * @property low Session low.
 * @property close Closing price; `close >= open` marks a bullish candle.
 */
data class OHLCPoint(
    val x: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)

/** Maps OHLC samples to their **close** value as [XyPoint] for shared visibility / decimation helpers. */
fun List<OHLCPoint>.toCloseXyPoints(): List<XyPoint> = map { XyPoint(it.x, it.close) }
