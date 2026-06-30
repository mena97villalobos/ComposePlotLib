package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint

/** Formats a line point for selection UI (e.g. markers). Pure Kotlin — keep UI types out. */
typealias LineValueFormatter = (ChartPoint) -> String

/** Formats a bar entry for selection UI. */
typealias BarValueFormatter = (BarEntry) -> String

/** Formats a candlestick / OHLC sample for selection UI. */
typealias CandlestickValueFormatter = (OHLCPoint) -> String

/** Default “x=…, y=…” formatting using [DefaultAxisLabelFormatter] for numeric parts. */
val DefaultLineValueFormatter: LineValueFormatter = { point ->
    "x=${DefaultAxisLabelFormatter(point.x)}, y=${DefaultAxisLabelFormatter(point.y)}"
}

/** Default bar entry formatting using [DefaultAxisLabelFormatter]. */
val DefaultBarValueFormatter: BarValueFormatter = { entry ->
    "x=${DefaultAxisLabelFormatter(entry.x)}, y=${DefaultAxisLabelFormatter(entry.y)}"
}

/** Default OHLC formatting (`O … H … L … C …`) using [DefaultAxisLabelFormatter]; string concat only (KMP-safe). */
val DefaultCandlestickValueFormatter: CandlestickValueFormatter = { p ->
    "O ${DefaultAxisLabelFormatter(p.open)}  H ${DefaultAxisLabelFormatter(p.high)}  " +
        "L ${DefaultAxisLabelFormatter(p.low)}  C ${DefaultAxisLabelFormatter(p.close)}"
}
