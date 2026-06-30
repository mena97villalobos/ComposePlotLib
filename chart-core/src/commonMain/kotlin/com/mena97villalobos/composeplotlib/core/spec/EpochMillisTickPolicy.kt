package com.mena97villalobos.composeplotlib.core.spec

/**
 * Post-processing for [AxisScale.TimeEpochMillis] tick **values** after nice-step placement.
 *
 * **Filter order for [Combined]:** each child policy is applied in list order to the previous result.
 *
 * IANA time zones and DST are **not** modeled; use [WeekdaysFixedOffset] only for fixed offsets, or keep
 * tick policy [All] and handle business rules in app data / custom label formatters.
 */
sealed interface EpochMillisTickPolicy {
    /** No tick-value filtering. */
    data object All : EpochMillisTickPolicy

    /** Removes ticks that fall on a Saturday or Sunday in the **UTC** calendar day. */
    data object WeekdaysUtc : EpochMillisTickPolicy

    /**
     * Like [WeekdaysUtc] but weekends are computed in a **fixed** offset from UTC (shifted civil day index).
     * Does **not** model DST; for real zones like `America/New_York`, pre-shift data or use [All] + app logic.
     */
    data class WeekdaysFixedOffset(
        val offsetMillisFromUtc: Long,
    ) : EpochMillisTickPolicy

    /**
     * Drops ticks whose **UTC** calendar day index `floor(epochMillis / 86_400_000)` is in the set.
     * Apps can pass holidays or other excluded UTC days without embedding exchange calendars in the library.
     */
    data class ExcludeUtcDays(
        val excludedUtcDayIndices: Set<Long>,
    ) : EpochMillisTickPolicy

    /**
     * Keeps ticks whose **UTC** time-of-day falls in [[startMinuteOfDay], [endMinuteOfDay]] inclusive
     * (minutes from midnight UTC, `0..1439`). Same-calendar-day only (no overnight session split).
     */
    data class SessionUtcMinutes(
        val startMinuteOfDay: Int,
        val endMinuteOfDay: Int,
    ) : EpochMillisTickPolicy {
        init {
            require(startMinuteOfDay in 0..1439 && endMinuteOfDay in 0..1439) {
                "startMinuteOfDay and endMinuteOfDay must be in 0..1439"
            }
            require(startMinuteOfDay <= endMinuteOfDay) {
                "SessionUtcMinutes requires startMinuteOfDay <= endMinuteOfDay (no overnight range in v1)"
            }
        }
    }

    /**
     * Chains multiple policies (e.g. [WeekdaysUtc] then [ExcludeUtcDays] for weekday + holiday exclusions).
     */
    data class Combined(
        val policies: List<EpochMillisTickPolicy>,
    ) : EpochMillisTickPolicy {
        init {
            require(policies.isNotEmpty()) { "Combined.policies must not be empty" }
        }
    }
}
