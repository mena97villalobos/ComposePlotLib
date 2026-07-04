package com.mena97villalobos.composeplotlib.core.model

import kotlin.math.max
import kotlin.math.min

/**
 * Converts raw OHLC [points] to **Heikin-Ashi** candles, which average price action to reduce noise and make
 * trends easier to read:
 *
 * ```
 * haClose = (open + high + low + close) / 4
 * haOpen  = (prevHaOpen + prevHaClose) / 2   (first bar: (open + close) / 2)
 * haHigh  = max(high, haOpen, haClose)
 * haLow   = min(low,  haOpen, haClose)
 * ```
 *
 * The `x` of each output equals its input. Returns an empty list for empty input.
 */
fun heikinAshi(points: List<OHLCPoint>): List<OHLCPoint> {
    if (points.isEmpty()) return emptyList()
    val out = ArrayList<OHLCPoint>(points.size)
    var prevOpen = (points[0].open + points[0].close) / 2.0
    var prevClose = (points[0].open + points[0].high + points[0].low + points[0].close) / 4.0
    for (index in points.indices) {
        val p = points[index]
        val haClose = (p.open + p.high + p.low + p.close) / 4.0
        val haOpen = if (index == 0) prevOpen else (prevOpen + prevClose) / 2.0
        val haHigh = max(p.high, max(haOpen, haClose))
        val haLow = min(p.low, min(haOpen, haClose))
        out += OHLCPoint(x = p.x, open = haOpen, high = haHigh, low = haLow, close = haClose)
        prevOpen = haOpen
        prevClose = haClose
    }
    return out
}
