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
import com.mena97villalobos.composeplotlib.core.spec.utcMinuteOfDay
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TickValuePlannerTest {
    @Test
    fun categoryScaleEmitsIntegerIndicesInViewport() {
        val axis =
            AxisSpec(
                scale = AxisScale.Category(listOf("a", "b", "c", "d")),
                tickCount = 1,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(1.1, 1.9),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = axis,
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertEquals(listOf(1.0, 2.0), pass.xValues)
        assertEquals(listOf("b", "c"), pass.xLabels(axis))
    }

    @Test
    fun tickMinMaxIntersectsViewportForNumericAxis() {
        val axis = AxisSpec(tickMin = 10.0, tickMax = 40.0, tickCount = 5)
        val search = axisTickSearchRange(ClosedRangeD(0.0, 100.0), axis)
        assertEquals(10.0, search.start)
        assertEquals(40.0, search.endInclusive)
    }

    @Test
    fun categoryAxisUsesTickMinMaxSearchRange() {
        val axis =
            AxisSpec(
                scale = AxisScale.Category(listOf("a", "b", "c", "d", "e")),
                tickMin = 1.0,
                tickMax = 2.0,
            )
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = axis,
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertEquals(listOf(1.0, 2.0), pass.xValues)
    }

    @Test
    fun generateNiceEpochMillisTicks_spansThreeDaysOrderedWithinRange() {
        val day = 86_400_000.0
        val ticks = generateNiceEpochMillisTicks(0.0, 3 * day, 5)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.all { it in 0.0..(3 * day) })
        assertEquals(ticks, ticks.sorted())
        assertEquals(ticks.distinct(), ticks)
        if (ticks.size >= 2) {
            val step = ticks[1] - ticks[0]
            assertTrue(step >= 3_600_000.0)
        }
    }

    @Test
    fun generateNiceEpochMillisTicks_reversedArgsMatchesForward() {
        val day = 86_400_000.0
        val forward = generateNiceEpochMillisTicks(0.0, 2 * day, 4)
        val reversed = generateNiceEpochMillisTicks(2 * day, 0.0, 4)
        assertEquals(forward, reversed)
    }

    @Test
    fun axisTickLabel_timeDefaultFormatterUsesCompactUtc() {
        val axis = AxisSpec(scale = AxisScale.TimeEpochMillis, labelFormatter = DefaultAxisLabelFormatter)
        assertEquals("1970-01-01 00:00", axisTickLabel(axis, 0.0))
    }

    @Test
    fun axisTickLabel_timeCustomFormatterPreserved() {
        val axis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                labelFormatter = { v -> "T:${v.toLong()}" },
            )
        assertEquals("T:0", axisTickLabel(axis, 0.0))
    }

    @Test
    fun axisTickLabel_timeExplicitCompactFormatter() {
        val axis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                labelFormatter = CompactUtcAxisLabelFormatter,
            )
        assertEquals("1970-01-02 06:00", axisTickLabel(axis, 86_400_000.0 + 6 * 3_600_000.0))
    }

    @Test
    fun densityReducer_timeScaleUsesEpochNiceSteps() {
        val day = 86_400_000.0
        val values = generateNiceEpochMillisTicks(0.0, 2 * day, 12)
        assertTrue(values.size > 4)
        val axis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                tickDensity = AxisTickDensity.MaxTicks(4),
                tickCount = 12,
            )
        val reduced = TickDensityReducer.reduce(values, axisSpanPx = 200.0, axis = axis)
        assertTrue(reduced.size <= 4)
        assertEquals(reduced, reduced.sorted())
        assertTrue(reduced.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun densityMaxTicksTrimsValues() {
        val values = (0..19).map { it * 1.0 }
        val axis = AxisSpec(tickDensity = AxisTickDensity.MaxTicks(4), tickCount = 20)
        val reduced = TickDensityReducer.reduce(values, axisSpanPx = 500.0, axis = axis)
        assertTrue(reduced.size <= 4)
        assertTrue(reduced.first() >= 0.0)
        assertTrue(reduced.last() <= 19.0)
        assertTrue(reduced.zipWithNext().all { (u, v) -> u <= v })
    }

    @Test
    fun densityReducer_minimumCapFromMeasuredLabelWidthRaisesEffectiveCap() {
        val values = (0..11).map { it * 1.0 }
        val axis = AxisSpec(tickDensity = AxisTickDensity.Auto, tickCount = 4, defaultDensitySpacingPx = 48.0)
        val withoutRaise = TickDensityReducer.reduce(values, axisSpanPx = 200.0, axis = axis)
        val withRaise =
            TickDensityReducer.reduce(
                values,
                axisSpanPx = 200.0,
                axis = axis,
                minimumCapFromMeasuredLabelWidth = 12,
            )
        assertTrue(withRaise.size > withoutRaise.size)
        assertTrue(withRaise.size <= values.size)
    }

    @Test
    fun generateNiceNumericTicks_nonFiniteDomainReturnsZeroSingleton() {
        assertEquals(listOf(0.0), generateNiceNumericTicks(Double.NaN, 10.0, 5))
        assertEquals(listOf(0.0), generateNiceNumericTicks(0.0, Double.POSITIVE_INFINITY, 5))
    }

    @Test
    fun generateNiceNumericTicks_desiredCountAtMostOneReturnsSingleEndpoint() {
        assertEquals(listOf(-3.0), generateNiceNumericTicks(-3.0, 10.0, 1))
        assertEquals(listOf(7.0), generateNiceNumericTicks(10.0, 7.0, 0)) // min/max swap
    }

    @Test
    fun generateNiceNumericTicks_nearlyEqualSpanReturnsSingleTick() {
        val a = 100.0
        val b = a + FpMath.EPS * 0.5
        assertTrue(FpMath.nearlyEqual(a, b))
        assertEquals(listOf(a), generateNiceNumericTicks(a, b, 6))
    }

    @Test
    fun generateNiceNumericTicks_orderedDistinctWithinSpan() {
        val ticks = generateNiceNumericTicks(0.0, 100.0, 5)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.all { it in 0.0..100.0 })
        assertEquals(ticks, ticks.sorted())
        assertEquals(ticks.distinct(), ticks)
        assertTrue(ticks.zipWithNext().all { (u, v) -> v >= u })
    }

    @Test
    fun generateNiceNumericTicks_reversedArgsMatchesForward() {
        val forward = generateNiceNumericTicks(-20.0, 30.0, 4)
        val reversed = generateNiceNumericTicks(30.0, -20.0, 4)
        assertEquals(forward, reversed)
    }

    @Test
    fun axisTickSearchRange_nonOverlappingHintFallsBackToViewport() {
        val viewport = ClosedRangeD(50.0, 100.0)
        val axis = AxisSpec(tickMin = 0.0, tickMax = 10.0, tickCount = 4)
        assertEquals(viewport, axisTickSearchRange(viewport, axis))
    }

    @Test
    fun axisTickSearchRange_reversedTickMinMaxIsNormalized() {
        val axis = AxisSpec(tickMin = 40.0, tickMax = 10.0, tickCount = 3)
        val search = axisTickSearchRange(ClosedRangeD(0.0, 100.0), axis)
        assertEquals(10.0, search.start)
        assertEquals(40.0, search.endInclusive)
    }

    @Test
    fun linearY_degenerateViewportSpanStillProducesStableTicks() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(7.0, 7.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = AxisSpec(tickCount = 4),
                yAxis = AxisSpec(tickCount = 5),
            )
        assertTrue(pass.yValues.isNotEmpty())
        assertEquals(7.0, pass.yValues.single(), 1e-9)
    }

    @Test
    fun linearX_degenerateViewportSpanStillProducesStableTicks() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(3.0, 3.0),
                yRange = ClosedRangeD(0.0, 10.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = AxisSpec(tickCount = 5),
                yAxis = AxisSpec(tickCount = 4),
            )
        assertTrue(pass.xValues.isNotEmpty())
        assertEquals(3.0, pass.xValues.single(), 1e-9)
    }

    @Test
    fun tickPlanner_numericAxisWithZeroTickCountProducesNoTicks() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10.0),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = AxisSpec(enabled = true, tickCount = 0),
                yAxis = AxisSpec(enabled = true, scale = AxisScale.Linear, tickCount = 2),
            )
        assertEquals(emptyList(), pass.xValues)
        assertTrue(pass.yValues.isNotEmpty())
    }

    @Test
    fun categoryScaleWithNoCategoriesProducesNoTicks() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 5.0),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = AxisSpec(scale = AxisScale.Category(emptyList())),
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertEquals(emptyList(), pass.xValues)
    }

    @Test
    fun densityReducer_disabledAxisReturnsInputUnchanged() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val axis = AxisSpec(enabled = false, tickDensity = AxisTickDensity.MaxTicks(2))
        assertEquals(values, TickDensityReducer.reduce(values, axisSpanPx = 1000.0, axis = axis))
    }

    @Test
    fun categoryViewport_nonFiniteRangeProducesNoTicks() {
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 1.0),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val badX =
            ChartViewport(
                xRange = ClosedRangeD(0.0, Double.POSITIVE_INFINITY),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val passOk =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = AxisSpec(scale = AxisScale.Category(listOf("a", "b"))),
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertTrue(passOk.xValues.isNotEmpty())
        val passBad =
            TickValuePlanner.compute(
                viewport = badX,
                xAxis = AxisSpec(scale = AxisScale.Category(listOf("a", "b"))),
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertEquals(emptyList(), passBad.xValues)
    }

    @Test
    fun generateNiceNumericTicks_hugeDesiredCountTerminatesWithOrderedValues() {
        val ticks = generateNiceNumericTicks(0.0, 1.0, 50_000_000)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.size <= 50_000)
        assertEquals(ticks, ticks.sorted())
        assertEquals(ticks.distinct(), ticks)
        assertTrue(ticks.all { it in 0.0..1.0 })
    }

    @Test
    fun densityReducer_nonFiniteAxisSpanLeavesValuesUnchanged() {
        val values = (0..9).map { it * 1.0 }
        val axis = AxisSpec(tickDensity = AxisTickDensity.MaxTicks(3), tickCount = 10)
        assertEquals(values, TickDensityReducer.reduce(values, axisSpanPx = Double.NaN, axis = axis))
    }

    @Test
    fun densityReducer_minSpacingWithNonFiniteSpacingFallsBackToOnePx() {
        val values = (0..19).map { it * 1.0 }
        val axis =
            AxisSpec(
                tickDensity = AxisTickDensity.MinSpacingPx(Double.NaN),
                tickCount = 20,
            )
        val reduced = TickDensityReducer.reduce(values, axisSpanPx = 100.0, axis = axis)
        assertTrue(reduced.size <= values.size)
        assertTrue(reduced.size >= 2)
    }

    @Test
    fun utcWeekend_helperMatchesSaturdayAndSunday1970() {
        assertFalse(isUtcWeekendCalendarDay(0.0))
        assertTrue(isUtcWeekendCalendarDay(2 * 86_400_000.0))
        assertTrue(isUtcWeekendCalendarDay(3 * 86_400_000.0))
    }

    @Test
    fun categoryAxis_truncatesLongLabelsWhenMaxCharsSet() {
        val axis =
            AxisSpec(
                scale = AxisScale.Category(listOf("Monday-is-long")),
                categoryLabelMaxChars = 8,
            )
        assertEquals("Monday-…", axisTickLabel(axis, 0.0))
    }

    @Test
    fun timeAxis_weekdaysPolicyRemovesUtcWeekendTicks() {
        val day = 86_400_000.0
        val raw = listOf(0.0, 2 * day, 3 * day, 4 * day)
        assertEquals(
            listOf(0.0, 4 * day),
            filterWeekdaysUtcTicks(raw),
        )
    }

    @Test
    fun timeAxis_weekdaysPolicyOmitsWeekendsAcrossMultiDayViewport() {
        val axis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                tickCount = 8,
                epochMillisTickPolicy = EpochMillisTickPolicy.WeekdaysUtc,
            )
        val day = 86_400_000.0
        val viewport =
            ChartViewport(
                xRange = ClosedRangeD(0.0, 10 * day),
                yRange = ClosedRangeD(0.0, 1.0),
            )
        val pass =
            TickValuePlanner.compute(
                viewport = viewport,
                xAxis = axis,
                yAxis = AxisSpec(enabled = false, tickCount = 0),
            )
        assertTrue(pass.xValues.size >= 2)
        assertTrue(pass.xValues.none { isUtcWeekendCalendarDay(it) })
    }

    @Test
    fun excludeUtcDays_removesListedDayIndices() {
        val day = 86_400_000.0
        val raw = listOf(0.0, day, 2 * day, 3 * day)
        val filtered =
            applyEpochMillisTickFilters(
                raw,
                EpochMillisTickPolicy.ExcludeUtcDays(setOf(1L)),
            )
        assertEquals(listOf(0.0, 2 * day, 3 * day), filtered)
    }

    @Test
    fun sessionUtcMinutes_keepsTicksInMinuteWindow() {
        val inside = 12 * 3_600_000.0
        val outside = 20 * 3_600_000.0
        val out =
            applyEpochMillisTickFilters(
                listOf(inside, outside),
                EpochMillisTickPolicy.SessionUtcMinutes(startMinuteOfDay = 600, endMinuteOfDay = 900),
            )
        assertEquals(listOf(inside), out)
    }

    @Test
    fun weekdaysFixedOffset_canDifferFromUtcWeekend() {
        val day = 86_400_000.0
        val nineHours = 9 * 3_600_000L
        val fridayEveningUtc = 1 * day + 18 * 3_600_000.0
        assertFalse(isUtcWeekendCalendarDay(fridayEveningUtc))
        assertTrue(isFixedOffsetWeekendCalendarDay(fridayEveningUtc, nineHours))
    }

    @Test
    fun combined_appliesPoliciesInOrder_weekdaysThenExclude() {
        val day = 86_400_000.0
        val raw = listOf(0.0, 2 * day, 3 * day, 4 * day)
        val out =
            applyEpochMillisTickFilters(
                raw,
                EpochMillisTickPolicy.Combined(
                    listOf(
                        EpochMillisTickPolicy.WeekdaysUtc,
                        EpochMillisTickPolicy.ExcludeUtcDays(setOf(0L)),
                    ),
                ),
            )
        assertEquals(listOf(4 * day), out)
    }

    @Test
    fun utcDayIndex_and_minuteOfDay_helpers() {
        assertEquals(0L, utcMillisToUtcDayIndex(0.0))
        assertEquals(1L, utcMillisToUtcDayIndex(86_400_000.0))
        assertEquals(600, utcMinuteOfDay(10 * 3_600_000.0))
        assertTrue(isUtcMinuteOfDayInSession(12 * 3_600_000.0, 600, 900))
    }
}
