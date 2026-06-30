package com.mena97villalobos.composeplotlib.core.spec

/**
 * Controls **domain-visible** candle planning and optional decimation for dense series.
 *
 * Candles are filtered to the viewport window (with [viewportContinuityPadding] extra candles on each side
 * so edge candles stay drawable), then uniformly subsampled when [maxCandles] caps the visible subset.
 * Decimation preserves the **global** point index so tap selection and markers bind to the right candle.
 *
 * @param maxCandles Max candles drawn/hit-tested per series after the viewport window. `0` (default) or
 * negative means no cap.
 * @param viewportContinuityPadding Extra candles kept on each side of the visible window (default 1).
 */
data class CandlestickRenderSpec(
    val maxCandles: Int = 0,
    val viewportContinuityPadding: Int = 1,
)
