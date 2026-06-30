package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.spec.CandlestickStyle

/**
 * A candlestick / OHLC series (one row in the legend, one [CandlestickStyle]).
 *
 * Most candlestick charts have a single series, but a list is supported for parity with
 * [BarGroup]-style families.
 *
 * @property id Used for selection ([com.mena97villalobos.composeplotlib.core.selection.HighlightTarget.seriesId]).
 */
data class CandlestickSeries(
    val id: String,
    val label: String,
    val points: List<OHLCPoint>,
    val style: CandlestickStyle = CandlestickStyle(),
)
