package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.BulletBand
import com.mena97villalobos.composeplotlib.core.spec.BulletChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals

class BulletChartTest {
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 120.0),
            viewport = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0)),
        )

    private fun plan(
        value: Double = 70.0,
        target: Double? = 80.0,
        showBounds: Boolean = true,
    ): BulletChartPlan =
        ChartPlanner.planBulletChart(
            input,
            BulletChartSpec(
                value = value,
                target = target,
                domainMax = 100.0,
                showBoundLabels = showBounds,
                bands =
                    listOf(
                        BulletBand(50.0, ChartColor(0xFFEEEEEE)),
                        BulletBand(75.0, ChartColor(0xFFCCCCCC)),
                        BulletBand(100.0, ChartColor(0xFFAAAAAA)),
                    ),
            ),
        )

    private inline fun <reified T : DrawCommand> plotCount(p: BulletChartPlan) = p.layout.plotDrawCommands.count { it is T }

    @Test
    fun emitsBandsMeasureAndTarget() {
        val p = plan()
        assertEquals(4, plotCount<DrawCommand.Rect>(p)) // 3 bands + measure bar
        assertEquals(1, plotCount<DrawCommand.Line>(p)) // target marker
    }

    @Test
    fun targetCanBeOmitted() {
        val p = plan(target = null)
        assertEquals(0, plotCount<DrawCommand.Line>(p))
    }

    @Test
    fun boundLabelsAreOverlayText() {
        val withLabels = plan(showBounds = true)
        assertEquals(2, withLabels.layout.overlayDrawCommands.count { it is DrawCommand.Text })
        val noLabels = plan(showBounds = false)
        assertEquals(0, noLabels.layout.overlayDrawCommands.count { it is DrawCommand.Text })
    }

    @Test
    fun zeroValueEmitsNoMeasureBar() {
        val p = plan(value = 0.0, target = null)
        // Only the 3 band rects; no measure bar (value maps to the left edge).
        assertEquals(3, plotCount<DrawCommand.Rect>(p))
    }
}
