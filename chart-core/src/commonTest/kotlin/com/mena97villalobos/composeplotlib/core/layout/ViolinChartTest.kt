package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.ViolinCategory
import com.mena97villalobos.composeplotlib.core.spec.ViolinChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolinChartTest {
    private val spec =
        ViolinChartSpec(
            categories =
                listOf(
                    ViolinCategory("A", (1..40).map { it.toDouble() }, ChartColor(0xFF4E79A7)),
                    ViolinCategory("B", (1..40).map { (it % 10).toDouble() + 20.0 }, ChartColor(0xFFF28E2B)),
                ),
            xAxis = AxisSpec(showGridLines = false),
            yAxis = AxisSpec(showGridLines = false),
        )

    private fun plan(spec: ViolinChartSpec): ChartLayoutPlan {
        val input =
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 300.0),
                viewport = ChartViewport(ClosedRangeD(-0.5, 1.5), ClosedRangeD(0.0, 40.0)),
            )
        return ChartPlanner.planViolinChart(input, spec).layout
    }

    @Test
    fun emitsAFilledPathAndMedianLinePerCategory() {
        val layout = plan(spec)
        val paths = layout.plotDrawCommands.filterIsInstance<DrawCommand.Path>().filter { it.style is PathDrawStyle.Fill }
        assertEquals(2, paths.size)
        val medians = layout.plotDrawCommands.filterIsInstance<DrawCommand.Line>()
        assertEquals(2, medians.size)
    }

    @Test
    fun violinPathIsClosedAndUsesResolution() {
        val custom = spec.copy(resolution = 20)
        val path = plan(custom).plotDrawCommands.filterIsInstance<DrawCommand.Path>().first()
        // right edge (resolution+1) + left edge (resolution+1) points + MoveTo/Close.
        val expectedPoints = (custom.resolution + 1) * 2
        val lineTos = path.segments.count { it is com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment.LineTo }
        val closes = path.segments.count { it is com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment.Close }
        assertEquals(1, closes)
        assertTrue(lineTos >= expectedPoints - 2)
    }

    @Test
    fun skipsCategoriesWithTooFewSamples() {
        val sparse = ViolinChartSpec(categories = listOf(ViolinCategory("X", listOf(5.0), ChartColor(0xFF000000))))
        val paths = plan(sparse).plotDrawCommands.filterIsInstance<DrawCommand.Path>()
        assertTrue(paths.isEmpty())
    }
}
