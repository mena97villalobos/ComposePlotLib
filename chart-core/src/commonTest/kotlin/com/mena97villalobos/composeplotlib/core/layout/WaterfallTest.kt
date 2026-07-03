package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.WaterfallEntry
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.WaterfallHitResolver
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.WaterfallChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WaterfallTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(-0.5, 3.5), yRange = ClosedRangeD(0.0, 14.0)),
        )

    private val spec =
        WaterfallChartSpec(
            entries =
                listOf(
                    WaterfallEntry(delta = 10.0),
                    WaterfallEntry(delta = -4.0),
                    WaterfallEntry(delta = 6.0),
                    WaterfallEntry(delta = 0.0, isTotal = true, label = "Total"),
                ),
            xAxis = AxisSpec(showGridLines = false),
            yAxis = AxisSpec(showGridLines = false),
        )

    @Test
    fun runningTotalsAndColors() {
        val plan = ChartPlanner.planWaterfallChart(input, spec)
        assertEquals(4, plan.bars.size)

        assertEquals(0.0, plan.bars[0].startValue, 1e-9)
        assertEquals(10.0, plan.bars[0].endValue, 1e-9)
        assertTrue(plan.bars[0].isIncrease)
        assertEquals(spec.increaseColor, plan.bars[0].color)

        assertEquals(10.0, plan.bars[1].startValue, 1e-9)
        assertEquals(6.0, plan.bars[1].endValue, 1e-9)
        assertTrue(!plan.bars[1].isIncrease)
        assertEquals(spec.decreaseColor, plan.bars[1].color)

        assertEquals(12.0, plan.bars[2].endValue, 1e-9)

        // Total bar spans 0..cumulative and does not change the running total.
        assertTrue(plan.bars[3].isTotal)
        assertEquals(0.0, plan.bars[3].startValue, 1e-9)
        assertEquals(12.0, plan.bars[3].endValue, 1e-9)
        assertEquals(spec.totalColor, plan.bars[3].color)
    }

    @Test
    fun emitsBarRectsAndConnectors() {
        val plan = ChartPlanner.planWaterfallChart(input, spec)
        assertEquals(4, plan.layout.plotDrawCommands.count { it is DrawCommand.Rect })
        assertEquals(3, plan.layout.plotDrawCommands.count { it is DrawCommand.Line }) // connectors between 4 bars
    }

    @Test
    fun connectorsCanBeDisabled() {
        val plan = ChartPlanner.planWaterfallChart(input, spec.copy(connectors = false))
        assertEquals(0, plan.layout.plotDrawCommands.count { it is DrawCommand.Line })
    }

    @Test
    fun tapSelectsBarAndAddsAccent() {
        val prelim = ChartPlanner.planWaterfallChart(input, spec)
        val selection =
            assertNotNull(WaterfallHitResolver.resolvePlanned(prelim.barsForHitTest(), prelim.bars[1].bounds.center))
        assertEquals(1, selection.target.pointIndex)
        assertEquals(WaterfallHitResolver.SERIES_ID, selection.target.seriesId)

        val selected = ChartPlanner.planWaterfallChart(input, spec, selection = selection)
        assertEquals(1, selected.layout.plotDrawCommands.count { it is DrawCommand.RoundRect })
    }

    @Test
    fun tapOutsideResolvesNull() {
        val plan = ChartPlanner.planWaterfallChart(input, spec)
        assertNull(WaterfallHitResolver.resolvePlanned(plan.barsForHitTest(), OffsetD(2.0, 2.0)))
    }
}
