package com.mena97villalobos.composeplotlib.core.export

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import com.mena97villalobos.composeplotlib.core.layout.ChartReservedRegions
import com.mena97villalobos.composeplotlib.core.layout.DrawCommand
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import kotlin.test.Test
import kotlin.test.assertTrue

class ChartSvgExportTest {
    private fun planOf(
        plot: List<DrawCommand>,
        overlay: List<DrawCommand> = emptyList(),
    ): ChartLayoutPlan =
        ChartLayoutPlan(
            canvasBounds = RectD(0.0, 0.0, 200.0, 100.0),
            plotBounds = RectD(10.0, 10.0, 190.0, 90.0),
            reservedRegions = ChartReservedRegions(InsetsD.Zero, null, null, null),
            xTicks = emptyList(),
            yTicks = emptyList(),
            legendEntries = emptyList(),
            plotDrawCommands = plot,
            overlayDrawCommands = overlay,
        )

    @Test
    fun emitsSvgRootWithViewBox() {
        val svg = chartLayoutPlanToSvg(planOf(emptyList()))
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\""))
        assertTrue(svg.contains("viewBox=\"0 0 200 100\""))
        assertTrue(svg.trimEnd().endsWith("</svg>"))
    }

    @Test
    fun clipsPlotCommandsAndSerializesShapes() {
        val svg =
            chartLayoutPlanToSvg(
                planOf(
                    plot =
                        listOf(
                            DrawCommand.Rect(RectD(20.0, 20.0, 40.0, 60.0), ChartColor(0xFF3366CC)),
                            DrawCommand.Circle(OffsetD(50.0, 50.0), 4.0, ChartColor(0xFFFF0000)),
                        ),
                    overlay = listOf(DrawCommand.Text("Hi <there>", OffsetD(5.0, 5.0), ChartColor(0xFF000000))),
                ),
            )
        assertTrue(svg.contains("<clipPath"))
        assertTrue(svg.contains("clip-path=\"url(#clip0)\""))
        assertTrue(svg.contains("<rect x=\"20\" y=\"20\" width=\"20\" height=\"40\""))
        assertTrue(svg.contains("fill=\"#3366cc\""))
        assertTrue(svg.contains("<circle cx=\"50\" cy=\"50\" r=\"4\""))
        // Text is XML-escaped.
        assertTrue(svg.contains("Hi &lt;there&gt;"))
    }

    @Test
    fun serializesGradientPathIntoDefs() {
        val path =
            DrawCommand.Path(
                segments =
                    listOf(
                        PathSegment.MoveTo(OffsetD(0.0, 0.0)),
                        PathSegment.LineTo(OffsetD(10.0, 10.0)),
                        PathSegment.Close,
                    ),
                style =
                    PathDrawStyle.VerticalGradientFill(
                        topColor = ChartColor(0xFF112233),
                        bottomColor = ChartColor(0x00112233),
                        topY = 0.0,
                        bottomY = 90.0,
                    ),
            )
        val svg = chartLayoutPlanToSvg(planOf(listOf(path)))
        assertTrue(svg.contains("<defs>"))
        assertTrue(svg.contains("<linearGradient id=\"grad0\""))
        assertTrue(svg.contains("fill=\"url(#grad0)\""))
        assertTrue(svg.contains("M 0 0 L 10 10 Z"))
    }

    @Test
    fun lineChartConvenienceProducesSvg() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries("a", "A", listOf(ChartPoint(0.0, 0.0), ChartPoint(1.0, 5.0), ChartPoint(2.0, 2.0))),
                    ),
            )
        val svg = lineChartToSvg(spec, widthPx = 300.0, heightPx = 200.0)
        assertTrue(svg.startsWith("<svg"))
        assertTrue(svg.contains("viewBox=\"0 0 300 200\""))
        // The series polyline should be present.
        assertTrue(svg.contains("<polyline"))
    }
}
