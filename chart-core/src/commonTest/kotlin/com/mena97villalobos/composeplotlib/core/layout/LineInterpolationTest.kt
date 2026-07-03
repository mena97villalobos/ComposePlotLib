package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineInterpolation
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineInterpolationTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 4.0), yRange = ClosedRangeD(0.0, 20.0)),
        )

    private fun plan(interpolation: LineInterpolation): LineChartPlan =
        ChartPlanner.planLineChart(
            input,
            LineChartSpec(
                series =
                    listOf(
                        LineSeries(
                            "s",
                            "S",
                            listOf(ChartPoint(0.0, 4.0), ChartPoint(2.0, 12.0), ChartPoint(4.0, 8.0)),
                            lineStyle = LineStyle(interpolation = interpolation),
                            pointStyle = PointStyle(visible = false),
                        ),
                    ),
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            ),
            selection = null,
        )

    // ---- geometry helpers ----

    @Test
    fun stepAfterInsertsHorizontalThenVerticalCorners() {
        val pts = listOf(OffsetD(0.0, 0.0), OffsetD(2.0, 4.0))
        assertEquals(
            listOf(OffsetD(0.0, 0.0), OffsetD(2.0, 0.0), OffsetD(2.0, 4.0)),
            steppedLinePoints(pts, LineInterpolation.StepAfter),
        )
    }

    @Test
    fun stepBeforeInsertsVerticalThenHorizontalCorners() {
        val pts = listOf(OffsetD(0.0, 0.0), OffsetD(2.0, 4.0))
        assertEquals(
            listOf(OffsetD(0.0, 0.0), OffsetD(0.0, 4.0), OffsetD(2.0, 4.0)),
            steppedLinePoints(pts, LineInterpolation.StepBefore),
        )
    }

    @Test
    fun stepMiddleInsertsMidpointCorners() {
        val pts = listOf(OffsetD(0.0, 0.0), OffsetD(2.0, 4.0))
        assertEquals(
            listOf(OffsetD(0.0, 0.0), OffsetD(1.0, 0.0), OffsetD(1.0, 4.0), OffsetD(2.0, 4.0)),
            steppedLinePoints(pts, LineInterpolation.StepMiddle),
        )
    }

    @Test
    fun monotoneCubicIsMoveToPlusCubics() {
        val pts = listOf(OffsetD(0.0, 0.0), OffsetD(1.0, 1.0), OffsetD(2.0, 2.0))
        val segs = monotoneCubicSegments(pts)
        assertTrue(segs.first() is PathSegment.MoveTo)
        assertEquals(2, segs.count { it is PathSegment.CubicTo })
    }

    @Test
    fun monotoneCubicDoesNotOvershootMonotonicData() {
        // Strictly increasing data: control points must stay within the segment's y bounds (no overshoot).
        val pts = listOf(OffsetD(0.0, 0.0), OffsetD(1.0, 1.0), OffsetD(2.0, 10.0), OffsetD(3.0, 11.0))
        val cubics = monotoneCubicSegments(pts).filterIsInstance<PathSegment.CubicTo>()
        // The last segment goes 10 -> 11; its control points must not dip below 10 or exceed 11.
        val last = cubics.last()
        assertTrue(last.control1.y in 10.0..11.0, "control1.y=${last.control1.y}")
        assertTrue(last.control2.y in 10.0..11.0, "control2.y=${last.control2.y}")
    }

    // ---- planner integration ----

    @Test
    fun linearEmitsPolyline() {
        val p = plan(LineInterpolation.Linear)
        assertEquals(1, p.layout.plotDrawCommands.count { it is DrawCommand.Polyline })
        assertEquals(0, p.layout.plotDrawCommands.count { it is DrawCommand.Path })
    }

    @Test
    fun steppedEmitsPolylineWithInsertedVertices() {
        val p = plan(LineInterpolation.StepAfter)
        val polyline =
            p.layout.plotDrawCommands
                .filterIsInstance<DrawCommand.Polyline>()
                .single()
        // 3 raw points -> 5 stepped vertices.
        assertEquals(5, polyline.points.size)
    }

    @Test
    fun monotoneCubicEmitsPathStrokeNotPolyline() {
        val p = plan(LineInterpolation.MonotoneCubic)
        assertEquals(0, p.layout.plotDrawCommands.count { it is DrawCommand.Polyline })
        assertTrue(p.layout.plotDrawCommands.any { it is DrawCommand.Path })
    }
}
