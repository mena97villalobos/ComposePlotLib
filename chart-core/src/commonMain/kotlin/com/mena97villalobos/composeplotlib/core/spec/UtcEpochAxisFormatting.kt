package com.mena97villalobos.composeplotlib.core.spec

/**
 * Platform-neutral UTC snapshot of **epoch milliseconds** for axis tick labels.
 *
 * Output is `yyyy-MM-dd HH:mm` (zero-padded). Not calendar-aware for leap seconds; suitable for tick
 * labels when paired with [AxisScale.TimeEpochMillis] and [CompactUtcAxisLabelFormatter]. For
 * locale or time-zone aware labels, pass a custom [AxisSpec.labelFormatter] from the platform
 * (for example `kotlinx-datetime` on JVM/Native).
 */
private const val MS_PER_DAY: Double = 86_400_000.0

/** UTC calendar day index: `floor(epochMillis / 86_400_000)` (same origin as [isUtcWeekendCalendarDay]). */
fun utcMillisToUtcDayIndex(epochMillis: Double): Long {
    if (!epochMillis.isFinite()) return 0L
    return kotlin.math.floor(epochMillis / MS_PER_DAY).toLong()
}

/**
 * True if [epochMillis] falls on Saturday or Sunday in **UTC** (calendar day of the instant).
 * Used by [EpochMillisTickPolicy.WeekdaysUtc]; not holiday-aware.
 */
fun isUtcWeekendCalendarDay(epochMillis: Double): Boolean {
    if (!epochMillis.isFinite()) return false
    val day = utcMillisToUtcDayIndex(epochMillis)
    val weekdayFromThursday1970 = ((day % 7L) + 7L) % 7L
    return weekdayFromThursday1970 == 2L || weekdayFromThursday1970 == 3L
}

/**
 * True if [epochMillis] falls on Saturday or Sunday in the civil day obtained by shifting the instant by
 * [offsetMillisFromUtc] before taking the day boundary (fixed-offset “local” weekend; no DST).
 */
fun isFixedOffsetWeekendCalendarDay(
    epochMillis: Double,
    offsetMillisFromUtc: Long,
): Boolean {
    if (!epochMillis.isFinite()) return false
    val day = kotlin.math.floor((epochMillis + offsetMillisFromUtc.toDouble()) / MS_PER_DAY).toLong()
    val weekdayFromThursday1970 = ((day % 7L) + 7L) % 7L
    return weekdayFromThursday1970 == 2L || weekdayFromThursday1970 == 3L
}

/** Minute-of-day UTC in `0..1439` for finite [epochMillis]. */
fun utcMinuteOfDay(epochMillis: Double): Int {
    if (!epochMillis.isFinite()) return 0
    val sodMs = ((epochMillis % MS_PER_DAY) + MS_PER_DAY) % MS_PER_DAY
    return (sodMs / 60_000.0).toInt().coerceIn(0, 1439)
}

/**
 * True if [utcMinuteOfDay] falls in [[startMinuteOfDay], [endMinuteOfDay]] inclusive (`0..1439`).
 */
fun isUtcMinuteOfDayInSession(
    epochMillis: Double,
    startMinuteOfDay: Int,
    endMinuteOfDay: Int,
): Boolean {
    if (!epochMillis.isFinite()) return false
    val m = utcMinuteOfDay(epochMillis)
    return m in startMinuteOfDay..endMinuteOfDay
}

/**
 * Formats [epochMillis] (Unix epoch milliseconds) as a zero-padded UTC `yyyy-MM-dd HH:mm` string.
 * Proleptic-Gregorian and not leap-second aware; backs [CompactUtcAxisLabelFormatter].
 */
fun formatEpochMillisUtcYmdHm(epochMillis: Long): String {
    val day = epochMillis.floorDiv(86_400_000L)
    val sodMs = epochMillis.mod(86_400_000L)
    val (y, m, d) = civilFromUnixEpochDays(day)
    val hh = (sodMs / 3_600_000L).toInt()
    val mm = ((sodMs % 3_600_000L) / 60_000L).toInt()
    return "${formatYear(y)}-${m.pad2()}-${d.pad2()} ${hh.pad2()}:${mm.pad2()}"
}

/** Human-readable UTC date-time labels for [AxisScale.TimeEpochMillis] ticks. */
val CompactUtcAxisLabelFormatter: AxisLabelFormatter = { v ->
    formatEpochMillisUtcYmdHm(v.toLong())
}

// Howard Hinnant, https://howardhinnant.github.io/date_algorithms.html#civil_from_days
private fun civilFromUnixEpochDays(zDays: Long): Triple<Int, Int, Int> {
    val z = zDays + 719_468L
    val era = if (z >= 0) z / 146_097L else (z - 146_096L) / 146_097L
    val doe = (z - era * 146_097L).toInt().coerceIn(0, 146_096)
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    var y = (yoe.toLong() + era * 400L).toInt()
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    if (m <= 2) y += 1
    return Triple(y, m, d)
}

private fun formatYear(y: Int): String = if (y < 0) "-${(-y).toString().padStart(4, '0')}" else y.toString().padStart(4, '0')

private fun Int.pad2(): String = toString().padStart(2, '0')
