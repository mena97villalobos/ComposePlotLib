package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedCandle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.normalizedNonDegenerate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KeyboardSelectionRoamTest {
    private val viewport =
        ChartViewport(
            xRange = ClosedRangeD(0.0, 10.0),
            yRange = ClosedRangeD(0.0, 10.0),
        )
    private val plotBounds = RectD(0.0, 0.0, 100.0, 100.0)

    @Test
    fun lineChartRoam_firstStepFromNull_selectsFirstPoint() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "a",
                            label = "A",
                            points =
                                listOf(
                                    ChartPoint(0.0, 1.0),
                                    ChartPoint(1.0, 2.0),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(),
                yAxis = AxisSpec(),
            )
        val sel =
            KeyboardSelectionRoam.lineChartRoam(
                spec = spec,
                viewport = viewport,
                plotBounds = plotBounds,
                current = null,
                deltaPoint = 1,
                deltaSeries = 0,
            )
        assertNotNull(sel)
        assertEquals(0, sel.target.pointIndex)
        assertEquals("a", sel.target.seriesId)
    }

    @Test
    fun lineChartRoam_stepsAlongPoints() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            id = "a",
                            label = "A",
                            points =
                                listOf(
                                    ChartPoint(0.0, 1.0),
                                    ChartPoint(1.0, 2.0),
                                    ChartPoint(2.0, 3.0),
                                ),
                        ),
                    ),
                xAxis = AxisSpec(),
                yAxis = AxisSpec(),
            )
        val first =
            KeyboardSelectionRoam.lineChartRoam(
                spec,
                viewport,
                plotBounds,
                null,
                deltaPoint = 1,
                deltaSeries = 0,
            )!!
        assertEquals(0, first.target.pointIndex)
        val second =
            KeyboardSelectionRoam.lineChartRoam(
                spec,
                viewport,
                plotBounds,
                first,
                deltaPoint = 1,
                deltaSeries = 0,
            )!!
        assertEquals(1, second.target.pointIndex)
        assertEquals(XyPoint(1.0, 2.0), second.target.point)
    }

    private fun candle(
        seriesId: String,
        index: Int,
        x: Double,
        close: Double,
    ): PlannedCandle {
        val cx = x * 10.0
        return PlannedCandle(
            seriesId = seriesId,
            index = index,
            point = OHLCPoint(x = x, open = close - 1.0, high = close + 1.0, low = close - 2.0, close = close),
            bodyBounds = RectD(cx - 3.0, 20.0, cx + 3.0, 60.0),
            hitBounds = RectD(cx - 5.0, 0.0, cx + 5.0, 100.0),
            wickTop = OffsetD(cx, 0.0),
            wickBottom = OffsetD(cx, 100.0),
            isBull = true,
            bodyColor = ChartColor.Green,
            wickColor = ChartColor.Black,
            wickWidth = 1.0,
        )
    }

    @Test
    fun candlestickRoam_rightFromNull_thenStepsAndClampsAtEnd() {
        val candles = listOf(candle("a", 0, 0.0, 5.0), candle("a", 1, 1.0, 6.0))
        val first = KeyboardSelectionRoam.candlestickChartRoam(candles, null, deltaPoint = 1, deltaSeries = 0)!!
        assertEquals("a", first.target.seriesId)
        assertEquals(0, first.target.pointIndex)
        assertEquals(XyPoint(0.0, 5.0), first.target.point)
        val second = KeyboardSelectionRoam.candlestickChartRoam(candles, first, deltaPoint = 1, deltaSeries = 0)!!
        assertEquals(1, second.target.pointIndex)
        val clamped = KeyboardSelectionRoam.candlestickChartRoam(candles, second, deltaPoint = 1, deltaSeries = 0)!!
        assertEquals(1, clamped.target.pointIndex) // clamp, no wrap
    }

    @Test
    fun candlestickRoam_leftFromNull_selectsLastCandle() {
        val candles = listOf(candle("a", 0, 0.0, 5.0), candle("a", 1, 1.0, 6.0))
        val last = KeyboardSelectionRoam.candlestickChartRoam(candles, null, deltaPoint = -1, deltaSeries = 0)!!
        assertEquals(1, last.target.pointIndex)
    }

    @Test
    fun candlestickRoam_downSwitchesSeries_resetsToFirstCandle() {
        val candles =
            listOf(
                candle("a", 0, 0.0, 5.0),
                candle("a", 1, 1.0, 6.0),
                candle("b", 0, 0.0, 9.0),
                candle("b", 1, 1.0, 8.0),
            )
        val onA = KeyboardSelectionRoam.candlestickChartRoam(candles, null, deltaPoint = 1, deltaSeries = 0)!!
        val onB = KeyboardSelectionRoam.candlestickChartRoam(candles, onA, deltaPoint = 0, deltaSeries = 1)!!
        assertEquals("b", onB.target.seriesId)
        assertEquals(0, onB.target.pointIndex)
    }

    @Test
    fun candlestickRoam_singleSeries_seriesStepIsNoOp() {
        val candles = listOf(candle("a", 0, 0.0, 5.0), candle("a", 1, 1.0, 6.0))
        val onSecond = KeyboardSelectionRoam.candlestickChartRoam(candles, null, deltaPoint = -1, deltaSeries = 0)!!
        val afterUp = KeyboardSelectionRoam.candlestickChartRoam(candles, onSecond, deltaPoint = 0, deltaSeries = -1)!!
        assertEquals("a", afterUp.target.seriesId)
        assertEquals(1, afterUp.target.pointIndex) // series step clamps to the only series; point unchanged
    }

    @Test
    fun candlestickRoam_emptyCandles_returnsNull() {
        assertNull(KeyboardSelectionRoam.candlestickChartRoam(emptyList(), null, deltaPoint = 1, deltaSeries = 0))
    }

    @Test
    fun candlestickRoam_currentDecimatedAway_degradesGracefully() {
        // Select global index 1, then simulate the next frame decimating index 1 out of the planned candles.
        val full = listOf(candle("a", 0, 0.0, 5.0), candle("a", 1, 1.0, 6.0), candle("a", 2, 2.0, 7.0))
        val onIndex1 =
            KeyboardSelectionRoam.candlestickChartRoam(
                full,
                KeyboardSelectionRoam.candlestickChartRoam(full, null, deltaPoint = 1, deltaSeries = 0),
                deltaPoint = 1,
                deltaSeries = 0,
            )!!
        assertEquals(1, onIndex1.target.pointIndex)

        val decimated = listOf(candle("a", 0, 0.0, 5.0), candle("a", 2, 2.0, 7.0))
        // index 1 is gone -> indexOfFirst == -1 -> right falls back to the first visible candle (index 0).
        val recovered = KeyboardSelectionRoam.candlestickChartRoam(decimated, onIndex1, deltaPoint = 1, deltaSeries = 0)!!
        assertEquals(0, recovered.target.pointIndex)
    }

    // --- Roam must project through the spec's own axis scale / inversion, not the linear default. ---

    private fun singlePointSpec(
        xAxis: AxisSpec,
        yAxis: AxisSpec,
    ) = LineChartSpec(
        series = listOf(LineSeries(id = "a", label = "A", points = listOf(ChartPoint(10.0, 10.0)))),
        xAxis = xAxis,
        yAxis = yAxis,
    )

    private fun scatterSinglePointSpec(
        xAxis: AxisSpec,
        yAxis: AxisSpec,
    ) = ScatterChartSpec(
        series = listOf(ScatterSeries(id = "a", label = "A", points = listOf(ChartPoint(10.0, 10.0)))),
        xAxis = xAxis,
        yAxis = yAxis,
    )

    /** Decade viewport so a log axis and a linear axis disagree unmistakably. */
    private val decadeViewport =
        ChartViewport(
            xRange = ClosedRangeD(1.0, 100.0),
            yRange = ClosedRangeD(1.0, 100.0),
        )

    private fun expectedPixel(
        spec: LineChartSpec,
        point: XyPoint,
    ): OffsetD =
        ChartTransform
            .forPlotBounds(
                decadeViewport.normalizedNonDegenerate(),
                plotBounds,
                xScale = spec.xAxis.scale,
                yScale = spec.yAxis.scale,
                invertX = spec.xAxis.inverted,
                invertY = spec.yAxis.inverted,
            ).toPixel(point)

    @Test
    fun lineChartRoam_logAxesProjectThroughScaleNotLinearDefault() {
        val spec = singlePointSpec(AxisSpec(scale = AxisScale.Log()), AxisSpec(scale = AxisScale.Log()))
        val sel =
            KeyboardSelectionRoam.lineChartRoam(
                spec = spec,
                viewport = decadeViewport,
                plotBounds = plotBounds,
                current = null,
                deltaPoint = 1,
                deltaSeries = 0,
            )
        assertNotNull(sel)
        // x=10 is the midpoint of decades 1..100, so a log axis puts it at the plot centre (50),
        // while the linear default lands it near the left/bottom edge (~9).
        val expected = expectedPixel(spec, XyPoint(10.0, 10.0))
        assertEquals(expected.x, sel.pixelOffset.x, 1e-6)
        assertEquals(expected.y, sel.pixelOffset.y, 1e-6)
    }

    @Test
    fun lineChartRoam_invertedAxesProjectThroughInversion() {
        val spec = singlePointSpec(AxisSpec(inverted = true), AxisSpec(inverted = true))
        val sel =
            KeyboardSelectionRoam.lineChartRoam(
                spec = spec,
                viewport = decadeViewport,
                plotBounds = plotBounds,
                current = null,
                deltaPoint = 1,
                deltaSeries = 0,
            )
        assertNotNull(sel)
        val expected = expectedPixel(spec, XyPoint(10.0, 10.0))
        assertEquals(expected.x, sel.pixelOffset.x, 1e-6)
        assertEquals(expected.y, sel.pixelOffset.y, 1e-6)
    }

    @Test
    fun scatterChartRoam_logAxesProjectThroughScaleNotLinearDefault() {
        val spec = scatterSinglePointSpec(AxisSpec(scale = AxisScale.Log()), AxisSpec(scale = AxisScale.Log()))
        val sel =
            KeyboardSelectionRoam.scatterChartRoam(
                spec = spec,
                viewport = decadeViewport,
                plotBounds = plotBounds,
                current = null,
                deltaPoint = 1,
                deltaSeries = 0,
            )
        assertNotNull(sel)
        val expected =
            expectedPixel(singlePointSpec(spec.xAxis, spec.yAxis), XyPoint(10.0, 10.0))
        assertEquals(expected.x, sel.pixelOffset.x, 1e-6)
        assertEquals(expected.y, sel.pixelOffset.y, 1e-6)
    }
}
