package com.mena97villalobos.composeplotlib.core.transform

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.DrawCommand
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvertedAxisTest {
    private val viewport = ChartViewport(xRange = ClosedRangeD(0.0, 10.0), yRange = ClosedRangeD(0.0, 10.0))
    private val plot = RectD(left = 0.0, top = 0.0, right = 100.0, bottom = 100.0)

    @Test
    fun invertedXMapsLowValuesToTheRight() {
        val normal = ChartTransform.forPlotBounds(viewport, plot)
        val inverted = ChartTransform.forPlotBounds(viewport, plot, invertX = true)
        // Normal: x=0 -> left (0), x=10 -> right (100).
        assertEquals(0.0, normal.toPixel(XyPoint(0.0, 5.0)).x, 1e-9)
        assertEquals(100.0, normal.toPixel(XyPoint(10.0, 5.0)).x, 1e-9)
        // Inverted: x=0 -> right (100), x=10 -> left (0).
        assertEquals(100.0, inverted.toPixel(XyPoint(0.0, 5.0)).x, 1e-9)
        assertEquals(0.0, inverted.toPixel(XyPoint(10.0, 5.0)).x, 1e-9)
    }

    @Test
    fun invertedYMapsLargeValuesToTheBottom() {
        val normal = ChartTransform.forPlotBounds(viewport, plot)
        val inverted = ChartTransform.forPlotBounds(viewport, plot, invertY = true)
        // Normal: y=10 -> top (0), y=0 -> bottom (100).
        assertEquals(0.0, normal.toPixel(XyPoint(5.0, 10.0)).y, 1e-9)
        assertEquals(100.0, normal.toPixel(XyPoint(5.0, 0.0)).y, 1e-9)
        // Inverted: y=10 -> bottom (100), y=0 -> top (0).
        assertEquals(100.0, inverted.toPixel(XyPoint(5.0, 10.0)).y, 1e-9)
        assertEquals(0.0, inverted.toPixel(XyPoint(5.0, 0.0)).y, 1e-9)
    }

    @Test
    fun toDomainRoundTripsUnderInversion() {
        val inverted = ChartTransform.forPlotBounds(viewport, plot, invertX = true, invertY = true)
        val domain = inverted.toDomain(inverted.toPixel(XyPoint(3.0, 7.0)))
        assertTrue(kotlin.math.abs(domain.x - 3.0) < 1e-9)
        assertTrue(kotlin.math.abs(domain.y - 7.0) < 1e-9)
    }

    @Test
    fun plannerRendersInvertedXWithLowValuesOnTheRight() {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 300.0),
                viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
            )

        fun firstPolylineXs(inverted: Boolean): List<Double> {
            val plan =
                ChartPlanner.planLineChart(
                    input,
                    LineChartSpec(
                        series =
                            listOf(
                                LineSeries(
                                    "s",
                                    "S",
                                    listOf(ChartPoint(0.0, 4.0), ChartPoint(4.0, 12.0)),
                                    pointStyle = PointStyle(visible = false),
                                ),
                            ),
                        xAxis = AxisSpec(showGridLines = false, inverted = inverted),
                        yAxis = AxisSpec(showGridLines = false),
                    ),
                    selection = null,
                )
            return plan.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Polyline>()
                .single()
                .points
                .map { it.x }
        }
        val normal = firstPolylineXs(inverted = false)
        val inverted = firstPolylineXs(inverted = true)
        // Normal: first data point (x=0) is left of the second (x=4). Inverted: reversed.
        assertTrue(normal.first() < normal.last())
        assertTrue(inverted.first() > inverted.last())
    }
}
