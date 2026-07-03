package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.ChartAnnotation
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnnotationTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun plan(annotations: List<ChartAnnotation>): LineChartPlan =
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
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
                annotations = annotations,
            ),
            selection = null,
        )

    private inline fun <reified T : DrawCommand> count(p: LineChartPlan) = p.layout.plotDrawCommands.count { it is T }

    @Test
    fun noAnnotationsByDefault() {
        val p = plan(emptyList())
        assertEquals(0, count<DrawCommand.Rect>(p))
        assertEquals(0, count<DrawCommand.Circle>(p)) // pointStyle invisible => no point circles
        assertEquals(0, count<DrawCommand.Text>(p))
    }

    @Test
    fun pointAnnotationEmitsCircleAndLabel() {
        val p = plan(listOf(ChartAnnotation.Point(x = 2.0, y = 10.0, label = "peak")))
        assertEquals(1, count<DrawCommand.Circle>(p))
        assertTrue(
            p.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Text>()
                .any { it.text == "peak" },
        )
    }

    @Test
    fun boxAnnotationEmitsFilledRect() {
        val p =
            plan(
                listOf(
                    ChartAnnotation.Box(
                        yRange = ClosedRangeD(5.0, 15.0),
                        fillColor = ChartColor(0x33FF0000L),
                    ),
                ),
            )
        assertEquals(1, count<DrawCommand.Rect>(p))
    }

    @Test
    fun segmentAnnotationEmitsLine() {
        val p =
            plan(
                listOf(
                    ChartAnnotation.Segment(x1 = 0.0, y1 = 2.0, x2 = 4.0, y2 = 18.0, color = ChartColor.Red),
                ),
            )
        // Grid lines disabled, so the only Line is the annotation segment.
        assertEquals(1, count<DrawCommand.Line>(p))
    }

    @Test
    fun textAnnotationEmitsText() {
        val p = plan(listOf(ChartAnnotation.Text(x = 2.0, y = 10.0, text = "note")))
        assertEquals(
            listOf("note"),
            p.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Text>()
                .map { it.text },
        )
    }
}
