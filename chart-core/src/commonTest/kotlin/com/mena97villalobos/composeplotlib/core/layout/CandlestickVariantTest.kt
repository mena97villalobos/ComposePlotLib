package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.model.heikinAshi
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickVariant
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals

class CandlestickVariantTest {
    // Candle 0 is bullish (close > open); candle 1 is bearish (close < open).
    private val series =
        CandlestickSeries(
            id = "s",
            label = "S",
            points =
                listOf(
                    OHLCPoint(x = 0.0, open = 10.0, high = 18.0, low = 9.0, close = 16.0),
                    OHLCPoint(x = 1.0, open = 16.0, high = 17.0, low = 8.0, close = 11.0),
                ),
        )

    private fun plan(variant: CandlestickVariant): ChartLayoutPlan {
        val spec =
            CandlestickChartSpec(
                series = listOf(series),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
                variant = variant,
            )
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(300.0, 200.0),
                viewport = ChartViewport(ClosedRangeD(-0.5, 1.5), ClosedRangeD(6.0, 20.0)),
            )
        return ChartPlanner.planCandlestickChart(input, spec).layout
    }

    @Test
    fun heikinAshiSmoothsTheFirstBar() {
        val ha = heikinAshi(series.points)
        // haOpen[0] = (10+16)/2 = 13; haClose[0] = (10+18+9+16)/4 = 13.25.
        assertEquals(13.0, ha[0].open, 1e-9)
        assertEquals(13.25, ha[0].close, 1e-9)
        // haOpen[1] = (13 + 13.25)/2 = 13.125.
        assertEquals(13.125, ha[1].open, 1e-9)
    }

    @Test
    fun defaultCandlesDrawFilledBodies() {
        val commands = plan(CandlestickVariant.Candles).plotDrawCommands
        assertEquals(2, commands.filterIsInstance<DrawCommand.Rect>().size) // two filled bodies
        assertEquals(2, commands.filterIsInstance<DrawCommand.Line>().size) // two wicks
    }

    @Test
    fun ohlcBarsDrawLinesAndNoBodies() {
        val commands = plan(CandlestickVariant.OhlcBars).plotDrawCommands
        assertEquals(0, commands.filterIsInstance<DrawCommand.Rect>().size)
        assertEquals(0, commands.filterIsInstance<DrawCommand.RoundRect>().size)
        // Three lines per candle: high–low bar + open tick + close tick.
        assertEquals(6, commands.filterIsInstance<DrawCommand.Line>().size)
    }

    @Test
    fun hollowCandlesOutlineBullishAndFillBearish() {
        val commands = plan(CandlestickVariant.HollowCandles).plotDrawCommands
        // Bullish body → stroked RoundRect; bearish body → filled Rect.
        assertEquals(1, commands.filterIsInstance<DrawCommand.RoundRect>().size)
        assertEquals(1, commands.filterIsInstance<DrawCommand.Rect>().size)
    }
}
