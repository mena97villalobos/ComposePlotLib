package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
