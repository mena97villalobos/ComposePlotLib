package com.mena97villalobos.composeplotlib.core.layout.planning

import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.AxisTickDensity
import com.mena97villalobos.composeplotlib.core.spec.CompactUtcAxisLabelFormatter
import com.mena97villalobos.composeplotlib.core.spec.DefaultAxisLabelFormatter
import com.mena97villalobos.composeplotlib.core.spec.EpochMillisTickPolicy
import com.mena97villalobos.composeplotlib.core.spec.isFixedOffsetWeekendCalendarDay
import com.mena97villalobos.composeplotlib.core.spec.isUtcMinuteOfDayInSession
import com.mena97villalobos.composeplotlib.core.spec.isUtcWeekendCalendarDay
import com.mena97villalobos.composeplotlib.core.spec.utcMillisToUtcDayIndex
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

private const val MAX_NICE_TICK_ITERATIONS = 50_000

/**
 * Pass 2 — domain tick values (and derived label strings for inset reservation) before final plot sizing.
 *
 * Reuse one instance across repeated planner calls when [ChartViewport] and primary [AxisSpec] values are unchanged
 * (for example a measurement pre-pass and a final pass in Compose).
 */
data class TickValuePass(
    val xValues: List<Double>,
    val yValues: List<Double>,
) {
    fun xLabels(axis: AxisSpec): List<String> = xValues.map { axisTickLabel(axis, it) }

    fun yLabels(axis: AxisSpec): List<String> = yValues.map { axisTickLabel(axis, it) }
}

internal fun axisTickLabel(
    axis: AxisSpec,
    value: Double,
): String {
    when (val s = axis.scale) {
        is AxisScale.Category -> {
            if (s.categories.isEmpty()) return ""
            val idx = value.roundToInt().coerceIn(0, s.categories.lastIndex)
            val raw = s.categories[idx]
            return ellipsizeDisplayLabel(raw, axis.categoryLabelMaxChars)
        }
        is AxisScale.TimeEpochMillis -> {
            val fmt =
                if (axis.labelFormatter === DefaultAxisLabelFormatter) {
                    CompactUtcAxisLabelFormatter
                } else {
                    axis.labelFormatter
                }
            return fmt(value)
        }
        else -> return axis.labelFormatter(value)
    }
}

internal object TickValuePlanner {
    fun compute(
        viewport: ChartViewport,
        xAxis: AxisSpec,
        yAxis: AxisSpec,
    ): TickValuePass {
        val xSearch = axisTickSearchRange(viewport.xRange, xAxis)
        val ySearch = axisTickSearchRange(viewport.yRange, yAxis)
        val xVals = planAxisValues(xAxis, xSearch, viewport.xRange)
        val yVals = planAxisValues(yAxis, ySearch, viewport.yRange)
        return TickValuePass(xVals, yVals)
    }
}

internal fun axisTickSearchRange(
    viewportRange: ClosedRangeD,
    axis: AxisSpec,
): ClosedRangeD {
    val lo = axis.tickMin
    val hi = axis.tickMax
    if (lo == null && hi == null) return viewportRange
    val hintLo = lo ?: viewportRange.start
    val hintHi = hi ?: viewportRange.endInclusive
    val hint = ClosedRangeD(min(hintLo, hintHi), max(hintLo, hintHi))
    return viewportRange.intersection(hint) ?: viewportRange
}

internal fun planAxisValues(
    axis: AxisSpec,
    searchRange: ClosedRangeD,
    viewportRange: ClosedRangeD,
): List<Double> {
    if (!axis.enabled) {
        return emptyList()
    }
    return when (val s = axis.scale) {
        is AxisScale.Category ->
            categoryIndicesInViewport(searchRange, s.categories.size)
        is AxisScale.Linear -> {
            if (axis.tickCount < 1) return emptyList()
            generateNiceNumericTicks(searchRange.start, searchRange.endInclusive, axis.tickCount)
        }
        is AxisScale.TimeEpochMillis ->
            planTimeEpochMillisTicks(
                searchRange.start,
                searchRange.endInclusive,
                axis.tickCount,
                axis.epochMillisTickPolicy,
            )
    }
}

internal fun filterWeekdaysUtcTicks(values: List<Double>): List<Double> = values.filter { !isUtcWeekendCalendarDay(it) }.distinct().sorted()

/**
 * Applies [policy] tick-value filters (see [EpochMillisTickPolicy] KDoc for [EpochMillisTickPolicy.Combined] order).
 */
internal fun applyEpochMillisTickFilters(
    values: List<Double>,
    policy: EpochMillisTickPolicy,
): List<Double> =
    when (policy) {
        is EpochMillisTickPolicy.All -> values
        is EpochMillisTickPolicy.WeekdaysUtc -> filterWeekdaysUtcTicks(values)
        is EpochMillisTickPolicy.WeekdaysFixedOffset ->
            values
                .filter { !isFixedOffsetWeekendCalendarDay(it, policy.offsetMillisFromUtc) }
                .distinct()
                .sorted()
        is EpochMillisTickPolicy.ExcludeUtcDays ->
            values
                .filter { utcMillisToUtcDayIndex(it) !in policy.excludedUtcDayIndices }
                .distinct()
                .sorted()
        is EpochMillisTickPolicy.SessionUtcMinutes ->
            values
                .filter { isUtcMinuteOfDayInSession(it, policy.startMinuteOfDay, policy.endMinuteOfDay) }
                .distinct()
                .sorted()
        is EpochMillisTickPolicy.Combined ->
            policy.policies.fold(values) { acc, p ->
                applyEpochMillisTickFilters(acc, p)
            }
    }

internal fun applyEpochMillisTickPolicyToTimeTicks(
    ticks: List<Double>,
    policy: EpochMillisTickPolicy,
    loRaw: Double,
    hiRaw: Double,
    seedCount: Int,
): List<Double> {
    if (ticks.isEmpty() || policy is EpochMillisTickPolicy.All) return ticks
    val lo = min(loRaw, hiRaw)
    val hi = max(loRaw, hiRaw)
    var out = applyEpochMillisTickFilters(ticks, policy)
    if (out.size >= 2 || ticks.size <= 1) {
        return out.ifEmpty { ticks }
    }
    var count = max(seedCount * 2, 4)
    repeat(12) {
        val dense = generateNiceEpochMillisTicks(lo, hi, count)
        out = applyEpochMillisTickFilters(dense, policy)
        if (out.size >= 2) return out
        count = (count * 2).coerceAtMost(128)
    }
    return out.ifEmpty { ticks }
}

internal fun planTimeEpochMillisTicks(
    searchStart: Double,
    searchEndInclusive: Double,
    tickCount: Int,
    policy: EpochMillisTickPolicy,
): List<Double> {
    if (tickCount < 1) return emptyList()
    val lo = min(searchStart, searchEndInclusive)
    val hi = max(searchStart, searchEndInclusive)
    val base = generateNiceEpochMillisTicks(lo, hi, tickCount)
    return applyEpochMillisTickPolicyToTimeTicks(base, policy, lo, hi, tickCount)
}

private fun categoryIndicesInViewport(
    viewportX: ClosedRangeD,
    categoryCount: Int,
): List<Double> {
    if (categoryCount <= 0) return emptyList()
    if (!viewportX.start.isFinite() || !viewportX.endInclusive.isFinite()) return emptyList()
    val last = categoryCount - 1
    val i0 = floor(viewportX.start).toInt().coerceIn(0, last)
    val i1 = ceil(viewportX.endInclusive).toInt().coerceIn(0, last)
    if (i0 > i1) return emptyList()
    return (i0..i1).map { it.toDouble() }
}

internal fun generateNiceNumericTicks(
    minRaw: Double,
    maxRaw: Double,
    desiredTickCount: Int,
): List<Double> {
    val lo = min(minRaw, maxRaw)
    val hi = max(minRaw, maxRaw)
    if (!lo.isFinite() || !hi.isFinite()) {
        return listOf(0.0)
    }
    if (desiredTickCount <= 1 || FpMath.nearlyEqualRel(lo, hi)) {
        return listOf(lo)
    }

    val rawStep = (hi - lo) / (desiredTickCount - 1)
    val magnitude = 10.0.pow(floor(log10(rawStep.coerceAtLeast(FpMath.EPS))))
    val residual = rawStep / magnitude
    val niceResidual =
        when {
            residual < 1.5 -> 1.0
            residual < 3.0 -> 2.0
            residual < 7.0 -> 5.0
            else -> 10.0
        }
    val step = niceResidual * magnitude
    if (!step.isFinite() || step <= 0.0) {
        return listOf(lo, hi).distinct().sorted()
    }
    val first = floor(lo / step) * step
    val last = ceil(hi / step) * step

    val values = mutableListOf<Double>()
    var current = first
    var guard = 0
    while (current <= last + (step / 2.0) && guard < MAX_NICE_TICK_ITERATIONS) {
        values += round(current * 1000.0) / 1000.0
        current += step
        guard++
    }
    val trimmed =
        values
            .map { v -> v.coerceIn(lo, hi) }
            .distinct()
            .sorted()
    return when {
        trimmed.isEmpty() -> listOf(lo, hi)
        trimmed.size == 1 -> listOf(lo, hi).distinct().sorted()
        else -> trimmed
    }
}

/** Pass 5 — reduce tick count using the resolved plot span (pixels on that axis). */
internal object TickDensityReducer {
    /**
     * @param minimumCapFromMeasuredLabelWidth When non-null (from [AxisSpec.raiseTickDensityCapFromMeasuredLabelWidth]),
     *   the effective cap is `max(computedCap, this)` so narrow measured labels can raise the tick budget.
     */
    fun reduce(
        values: List<Double>,
        axisSpanPx: Double,
        axis: AxisSpec,
        minimumCapFromMeasuredLabelWidth: Int? = null,
    ): List<Double> {
        if (values.size <= 2 || !axis.enabled) return values
        if (!axisSpanPx.isFinite() || axisSpanPx <= FpMath.EPS_SPAN) return values
        val spacingFromAuto = axis.defaultDensitySpacingPx.coerceAtLeast(8.0)
        var cap =
            when (val d = axis.tickDensity) {
                AxisTickDensity.Auto -> {
                    val spacingFit = max(2, (axisSpanPx / spacingFromAuto).toInt())
                    min(spacingFit, axis.tickCount.coerceAtLeast(2))
                }
                is AxisTickDensity.MaxTicks -> d.max.coerceAtLeast(2)
                is AxisTickDensity.MinSpacingPx -> {
                    val sp = d.spacingPx
                    val safeSp = if (sp.isFinite() && sp > 0.0) sp else 1.0
                    max(2, (axisSpanPx / safeSp).toInt())
                }
            }
        minimumCapFromMeasuredLabelWidth?.let { cap = max(cap, it.coerceAtLeast(2)) }
        val target = min(values.size, cap)
        if (values.size <= target) return values
        return when (axis.scale) {
            is AxisScale.Category -> subsampleSortedValues(values, target)
            is AxisScale.Linear -> {
                val lo = values.min()
                val hi = values.max()
                reduceNumericNiceRange(lo, hi, target)
            }
            is AxisScale.TimeEpochMillis -> {
                val lo = values.min()
                val hi = values.max()
                val reduced = reduceEpochMillisNiceRange(lo, hi, target)
                val withPolicy =
                    applyEpochMillisTickPolicyToTimeTicks(
                        reduced,
                        axis.epochMillisTickPolicy,
                        lo,
                        hi,
                        target,
                    )
                if (withPolicy.size > target) subsampleSortedValues(withPolicy, target) else withPolicy
            }
        }
    }
}

private val niceEpochStepCandidatesMs: DoubleArray =
    doubleArrayOf(
        1.0,
        2.0,
        5.0,
        10.0,
        20.0,
        50.0,
        100.0,
        200.0,
        500.0,
        1_000.0,
        2_000.0,
        5_000.0,
        10_000.0,
        15_000.0,
        30_000.0,
        60_000.0,
        120_000.0,
        300_000.0,
        600_000.0,
        900_000.0,
        1_800_000.0,
        3_600_000.0,
        7_200_000.0,
        10_800_000.0,
        21_600_000.0,
        43_200_000.0,
        86_400_000.0,
        172_800_000.0,
        604_800_000.0,
        2_592_000_000.0,
    )

private fun niceEpochStepMs(raw: Double): Double {
    if (!raw.isFinite() || raw <= FpMath.EPS_SPAN) return 1_000.0
    var i = 0
    while (i < niceEpochStepCandidatesMs.size) {
        val s = niceEpochStepCandidatesMs[i]
        if (s >= raw * 0.67) return s
        i++
    }
    var s = niceEpochStepCandidatesMs.last()
    while (s < raw * 0.67 && s.isFinite()) s *= 10.0
    return if (s.isFinite()) s else niceEpochStepCandidatesMs.last()
}

/**
 * Nice tick positions for epoch milliseconds (UTC semantics in data; labeling is separate).
 * Steps favor common durations (seconds, minutes, hours, days) before falling back to powers of ten.
 */
internal fun generateNiceEpochMillisTicks(
    minRaw: Double,
    maxRaw: Double,
    desiredTickCount: Int,
): List<Double> {
    val lo = min(minRaw, maxRaw)
    val hi = max(minRaw, maxRaw)
    if (!lo.isFinite() || !hi.isFinite()) {
        return listOf(0.0)
    }
    if (desiredTickCount <= 1 || FpMath.nearlyEqualRel(lo, hi)) {
        return listOf(lo)
    }
    val rawStep = (hi - lo) / (desiredTickCount - 1)
    val step = niceEpochStepMs(rawStep)
    if (!step.isFinite() || step <= 0.0) {
        return listOf(lo, hi).distinct().sorted()
    }
    val first = floor(lo / step) * step
    val last = ceil(hi / step) * step
    val values = mutableListOf<Double>()
    var current = first
    var guard = 0
    while (current <= last + (step / 2.0) && guard < MAX_NICE_TICK_ITERATIONS) {
        values += round(current * 1000.0) / 1000.0
        current += step
        guard++
    }
    val trimmed =
        values
            .map { v -> v.coerceIn(lo, hi) }
            .distinct()
            .sorted()
    return when {
        trimmed.isEmpty() -> listOf(lo, hi)
        trimmed.size == 1 -> listOf(lo, hi).distinct().sorted()
        else -> trimmed
    }
}

private fun reduceEpochMillisNiceRange(
    loRaw: Double,
    hiRaw: Double,
    maxTicks: Int,
): List<Double> {
    val lo = min(loRaw, hiRaw)
    val hi = max(loRaw, hiRaw)
    val cap = maxTicks.coerceAtLeast(2)
    var desired = cap
    repeat(24) {
        val t = generateNiceEpochMillisTicks(lo, hi, desired)
        when {
            t.size <= cap -> return t
            desired <= 2 -> return if (t.size <= cap) t else subsampleSortedValues(t, cap)
            else -> desired--
        }
    }
    return generateNiceEpochMillisTicks(lo, hi, 2)
}

private fun reduceNumericNiceRange(
    loRaw: Double,
    hiRaw: Double,
    maxTicks: Int,
): List<Double> {
    val lo = min(loRaw, hiRaw)
    val hi = max(loRaw, hiRaw)
    val cap = maxTicks.coerceAtLeast(2)
    var desired = cap
    repeat(24) {
        val t = generateNiceNumericTicks(lo, hi, desired)
        when {
            t.size <= cap -> return t
            desired <= 2 -> return if (t.size <= cap) t else subsampleSortedValues(t, cap)
            else -> desired--
        }
    }
    return generateNiceNumericTicks(lo, hi, 2)
}

private fun subsampleSortedValues(
    values: List<Double>,
    maxCount: Int,
): List<Double> {
    if (values.size <= maxCount || maxCount < 2) return values
    val n = values.size
    val out = ArrayList<Double>(maxCount)
    for (k in 0 until maxCount) {
        val idx = round(k * (n - 1).toDouble() / (maxCount - 1)).toInt().coerceIn(0, n - 1)
        out.add(values[idx])
    }
    return dedupeNearSorted(out)
}

private fun dedupeNearSorted(values: List<Double>): List<Double> {
    if (values.isEmpty()) return values
    val sorted = values.sorted()
    val out = ArrayList<Double>(sorted.size)
    var prev = Double.NaN
    for (v in sorted) {
        if (out.isEmpty() || !FpMath.nearlyEqualRel(v, prev)) {
            out.add(v)
            prev = v
        }
    }
    return out
}
