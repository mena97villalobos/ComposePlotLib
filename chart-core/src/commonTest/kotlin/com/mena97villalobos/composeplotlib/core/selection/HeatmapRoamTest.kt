package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.model.HeatmapCell
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HeatmapRoamTest {
    // 3 columns x 2 rows, dense.
    private val spec =
        HeatmapChartSpec(
            cells =
                (0 until 2).flatMap { r ->
                    (0 until 3).map { c -> HeatmapCell(col = c, row = r, value = (r * 3 + c).toDouble()) }
                },
            columnLabels = listOf("A", "B", "C"),
            rowLabels = listOf("R0", "R1"),
        )

    private val plan =
        ChartPlanner.planHeatmapChart(
            ChartLayoutInput(
                canvasSize = SizeD(400.0, 300.0),
                viewport = ChartViewport(xRange = ClosedRangeD(-0.5, 2.5), yRange = ClosedRangeD(-0.5, 1.5)),
            ),
            spec,
        )

    private fun coord(sel: ChartSelection): Pair<Int, Int> {
        val point = sel.target.point
        return point.x.toInt() to point.y.toInt()
    }

    @Test
    fun fromNothingSelectsBottomLeftCell() {
        val first = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, null, deltaCol = 1, deltaRow = 0))
        assertEquals(0 to 0, coord(first))
        assertEquals(spec.id, first.target.seriesId)
    }

    @Test
    fun arrowsStepColumnsAndRows() {
        val start = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, null, 1, 0))
        val right = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, start, deltaCol = 1, deltaRow = 0))
        assertEquals(1 to 0, coord(right))
        val up = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, right, deltaCol = 0, deltaRow = 1))
        assertEquals(1 to 1, coord(up))
    }

    @Test
    fun clampsAtGridEdges() {
        val start = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, null, 1, 0)) // (0,0)
        // Left at col 0 -> no move.
        val left = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, start, deltaCol = -1, deltaRow = 0))
        assertEquals(0 to 0, coord(left))
        // Down at row 0 -> no move.
        val down = assertNotNull(KeyboardSelectionRoam.heatmapRoam(plan, spec.id, start, deltaCol = 0, deltaRow = -1))
        assertEquals(0 to 0, coord(down))
    }

    @Test
    fun zeroDeltaReturnsCurrent() {
        val start = KeyboardSelectionRoam.heatmapRoam(plan, spec.id, null, 1, 0)
        assertEquals(start, KeyboardSelectionRoam.heatmapRoam(plan, spec.id, start, 0, 0))
    }
}
