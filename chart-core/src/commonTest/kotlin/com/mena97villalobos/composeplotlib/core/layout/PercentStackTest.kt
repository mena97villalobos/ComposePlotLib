package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStackMode
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PercentStackTest {
    // Value axis [0,1] as the percent-stacked composable viewport would supply.
    private val input =
        ChartLayoutInput(
            canvasSize = SizeD(400.0, 300.0),
            viewport = ChartViewport(xRange = ClosedRangeD(-0.5, 0.5), yRange = ClosedRangeD(0.0, 1.0)),
        )

    private fun plan(stackMode: BarStackMode): BarChartPlan =
        ChartPlanner.planBarChart(
            input,
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup("a", "A", listOf(BarEntry(0.0, 30.0))),
                        BarGroup("b", "B", listOf(BarEntry(0.0, 10.0))),
                    ),
                barLayout = BarLayoutMode.Stacked,
                stackMode = stackMode,
                xAxis = AxisSpec(showGridLines = false),
                yAxis = AxisSpec(showGridLines = false),
            ),
        )

    @Test
    fun percentStackNormalizesEachCategoryToShares() {
        val p = plan(BarStackMode.PercentOfTotal)
        val a = p.barSegments.first { it.groupId == "a" }
        val b = p.barSegments.first { it.groupId == "b" }
        // A = 30/40 = 75%, B = 10/40 = 25% -> A is 3x as tall as B.
        assertTrue(abs(a.bounds.height - 3.0 * b.bounds.height) < 1.0, "aH=${a.bounds.height} bH=${b.bounds.height}")
        // Together they fill the whole plot height.
        assertTrue(abs((a.bounds.height + b.bounds.height) - p.layout.plotBounds.height) < 1.0)
    }

    @Test
    fun percentStackPreservesOriginalEntryValuesForMarkers() {
        val segs = plan(BarStackMode.PercentOfTotal).barSegments
        // Segment geometry is normalized, but the echoed entry keeps the raw value (for markers/labels).
        assertEquals(30.0, segs.first { it.groupId == "a" }.entry.y)
        assertEquals(10.0, segs.first { it.groupId == "b" }.entry.y)
    }

    @Test
    fun absoluteModeIsUnchanged() {
        // In absolute mode with a [0,1] viewport, raw values (30, 10) are far off-scale; the point is only that
        // the two modes produce different geometry.
        val absolute =
            plan(BarStackMode.Absolute)
                .barSegments
                .first { it.groupId == "a" }
                .bounds.height
        val percent =
            plan(BarStackMode.PercentOfTotal)
                .barSegments
                .first { it.groupId == "a" }
                .bounds.height
        assertTrue(abs(absolute - percent) > 1.0)
    }
}
