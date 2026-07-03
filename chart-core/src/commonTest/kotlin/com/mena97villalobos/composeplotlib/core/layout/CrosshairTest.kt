package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.HighlightTarget
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.CrosshairSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals

class CrosshairTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private val selection =
        ChartSelection(
            target = HighlightTarget(seriesId = "s", pointIndex = 1, point = XyPoint(2.0, 10.0)),
            pixelOffset = OffsetD(200.0, 150.0),
        )

    private fun plan(crosshair: CrosshairSpec): LineChartPlan =
        ChartPlanner.planLineChart(
            input,
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            "s",
                            "S",
                            listOf(ChartPoint(0.0, 4.0), ChartPoint(4.0, 16.0)),
                            pointStyle = PointStyle(visible = false),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
                crosshair = crosshair,
            ),
            selection = selection,
        )

    private inline fun <reified T : DrawCommand> count(p: LineChartPlan) = p.layout.plotDrawCommands.count { it is T }

    @Test
    fun disabledCrosshairDrawsSingleSelectionLine() {
        val p = plan(CrosshairSpec(enabled = false))
        assertEquals(1, count<DrawCommand.Line>(p)) // vertical selection guideline only
        assertEquals(1, count<DrawCommand.Circle>(p)) // selection knot
        assertEquals(0, count<DrawCommand.RoundRect>(p))
    }

    @Test
    fun enabledCrosshairDrawsBothGuidelines() {
        val p = plan(CrosshairSpec(enabled = true))
        assertEquals(2, count<DrawCommand.Line>(p)) // vertical + horizontal
        assertEquals(1, count<DrawCommand.Circle>(p))
    }

    @Test
    fun enabledCrosshairDrawsAxisChips() {
        val p = plan(CrosshairSpec(enabled = true, showAxisLabels = true))
        assertEquals(2, count<DrawCommand.RoundRect>(p)) // x + y chips
        assertEquals(2, count<DrawCommand.Text>(p)) // x + y labels (axis ticks live in the overlay layer)
    }

    @Test
    fun axisLabelsCanBeDisabled() {
        val p = plan(CrosshairSpec(enabled = true, showAxisLabels = false))
        assertEquals(0, count<DrawCommand.RoundRect>(p))
        assertEquals(0, count<DrawCommand.Text>(p))
    }

    @Test
    fun labelChipBackgroundIsOptional() {
        val p = plan(CrosshairSpec(enabled = true, labelBackground = null))
        assertEquals(0, count<DrawCommand.RoundRect>(p))
        assertEquals(2, count<DrawCommand.Text>(p))
    }

    @Test
    fun onlyHorizontalLineWhenVerticalDisabled() {
        val p = plan(CrosshairSpec(enabled = true, showVerticalLine = false, showAxisLabels = false))
        assertEquals(1, count<DrawCommand.Line>(p))
    }
}
