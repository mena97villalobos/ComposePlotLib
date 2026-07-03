package com.mena97villalobos.composeplotlib.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LinearRegressionTest {
    @Test
    fun perfectLineRecoversSlopeAndIntercept() {
        val fit = LinearRegression.fit(doubleArrayOf(0.0, 1.0, 2.0, 3.0), doubleArrayOf(1.0, 3.0, 5.0, 7.0))!!
        assertEquals(2.0, fit.slope, 1e-9)
        assertEquals(1.0, fit.intercept, 1e-9)
        assertEquals(9.0, fit.predict(4.0), 1e-9)
        assertEquals(1.0, fit.rSquared, 1e-9)
        // Perfect fit => zero residual std dev => zero-width band.
        assertEquals(0.0, fit.meanResponseStandardError(4.0), 1e-9)
    }

    @Test
    fun fitIgnoresNonFinitePairs() {
        val fit =
            LinearRegression.fit(
                doubleArrayOf(0.0, 1.0, Double.NaN, 2.0, 3.0),
                doubleArrayOf(1.0, 3.0, 100.0, 5.0, 7.0),
            )!!
        assertEquals(4, fit.n)
        assertEquals(2.0, fit.slope, 1e-9)
    }

    @Test
    fun degenerateInputsReturnNull() {
        assertNull(LinearRegression.fit(doubleArrayOf(1.0), doubleArrayOf(2.0)))
        // All x identical => no estimable slope.
        assertNull(LinearRegression.fit(doubleArrayOf(2.0, 2.0, 2.0), doubleArrayOf(1.0, 2.0, 3.0)))
    }

    @Test
    fun noisyDataHasPositiveResidualStdError() {
        val fit =
            LinearRegression.fit(
                doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0),
                doubleArrayOf(1.0, 2.5, 4.0, 6.5, 8.0),
            )!!
        assertTrue(fit.slope > 0.0)
        assertTrue(fit.rSquared in 0.9..1.0)
        assertTrue(fit.meanResponseStandardError(2.0) > 0.0)
        // Std error is smallest at the mean x and grows with distance from it.
        assertTrue(fit.meanResponseStandardError(4.0) > fit.meanResponseStandardError(2.0))
    }

    @Test
    fun twoPointsGiveUnestimableStandardError() {
        val fit = LinearRegression.fit(doubleArrayOf(0.0, 4.0), doubleArrayOf(1.0, 9.0))!!
        assertEquals(2, fit.n)
        assertTrue(fit.meanResponseStandardError(2.0).isNaN())
    }
}
