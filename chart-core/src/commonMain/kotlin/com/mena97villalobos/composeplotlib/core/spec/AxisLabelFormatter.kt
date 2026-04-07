package com.mena97villalobos.composeplotlib.core.spec

/** Maps a numeric axis value to tick label text (categories, dates, compact numbers, etc.). */
typealias AxisLabelFormatter = (Double) -> String

/** Rounds to int when whole, otherwise one decimal place. */
val DefaultAxisLabelFormatter: AxisLabelFormatter = { value ->
    when {
        value % 1.0 == 0.0 -> value.toInt().toString()
        else -> {
            val rounded = kotlin.math.round(value * 10.0) / 10.0
            rounded.toString()
        }
    }
}

/**
 * Raw millis as decimal string — useful in tests; for chart labels prefer [CompactUtcAxisLabelFormatter].
 */
val DefaultEpochMillisAxisLabelFormatter: AxisLabelFormatter = { value ->
    value.toLong().toString()
}
