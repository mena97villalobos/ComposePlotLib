package com.mena97villalobos.composeplotlib.core.spec

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Human-friendly numeric label: whole values render without a decimal part (`9.0` → `"9"`),
 * fractional values round to one decimal (`9.25` → `"9.3"`), non-finite values render as `"—"`.
 * Digits are built manually (no `Double.toString` in the common range) so large values never
 * degrade to scientific notation, and rounding runs on `Long` so magnitudes far past `Int.MAX_VALUE`
 * stay exact.
 *
 * KMP-safe (manual string building; no `String.format`). Shared by pie slice labels and sankey link labels.
 */
internal fun formatChartValue(v: Double): String {
    if (!v.isFinite()) return "—"
    // Past this magnitude (v * 10) can no longer be rounded exactly into a Long; fall back to the
    // raw representation rather than printing a silently-wrong number.
    if (abs(v) >= 9.0e17) return v.toString()
    val scaledTenths = (v * 10.0).roundToLong()
    val whole = scaledTenths / 10
    val tenth = abs((scaledTenths % 10).toInt())
    if (tenth == 0) return whole.toString()
    val wholePart = if (scaledTenths < 0 && whole == 0L) "-0" else whole.toString()
    return "$wholePart.$tenth"
}
