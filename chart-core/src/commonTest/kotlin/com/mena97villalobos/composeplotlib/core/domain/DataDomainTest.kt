package com.mena97villalobos.composeplotlib.core.domain

import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An empty series is a benign loading state, not a programming error: domain computation must degrade to a
 * degenerate range rather than throwing and taking the host app down mid data-load.
 */
class DataDomainTest {
    @Test
    fun emptyPointListYieldsDegenerateDomainInsteadOfThrowing() {
        val domain = DataDomain.fromPoints(emptyList())

        assertEquals(0.0, domain.xRange.start)
        assertEquals(0.0, domain.xRange.endInclusive)
        assertEquals(0.0, domain.yRange.start)
        assertEquals(0.0, domain.yRange.endInclusive)
    }

    @Test
    fun emptyLineSeriesDomainDoesNotThrow() {
        val domain = LineSeries(id = "s", label = "S", points = emptyList()).domain()

        assertEquals(0.0, domain.xRange.span)
        assertEquals(0.0, domain.yRange.span)
    }

    @Test
    fun emptyScatterSeriesDomainDoesNotThrow() {
        val domain = ScatterSeries(id = "s", label = "S", points = emptyList()).domain()

        assertEquals(0.0, domain.xRange.span)
        assertEquals(0.0, domain.yRange.span)
    }

    @Test
    fun allNonFinitePointsStillCollapseToZeroDomain() {
        val domain = DataDomain.fromPoints(listOf(XyPoint(Double.NaN, Double.POSITIVE_INFINITY)))

        assertEquals(0.0, domain.xRange.start)
        assertEquals(0.0, domain.xRange.endInclusive)
        assertEquals(0.0, domain.yRange.start)
        assertEquals(0.0, domain.yRange.endInclusive)
    }

    @Test
    fun finitePointsStillProduceRealBounds() {
        val domain = DataDomain.fromPoints(listOf(XyPoint(-1.0, 2.0), XyPoint(3.0, 5.0)))

        assertEquals(-1.0, domain.xRange.start)
        assertEquals(3.0, domain.xRange.endInclusive)
        assertEquals(2.0, domain.yRange.start)
        assertEquals(5.0, domain.yRange.endInclusive)
    }

    @Test
    fun emptySeriesAndAllGapSeriesAgree() {
        val allGaps = LineSeries(id = "s", label = "S", points = listOf(ChartPoint.gap(1.0), ChartPoint.gap(2.0)))

        // A series that is entirely gap markers has no finite y, so it collapses the same way an empty one does.
        assertEquals(0.0, allGaps.domain().yRange.span)
    }
}
