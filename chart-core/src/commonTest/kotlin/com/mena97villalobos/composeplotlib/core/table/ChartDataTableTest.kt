package com.mena97villalobos.composeplotlib.core.table

import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartDataTableTest {
    @Test
    fun lineTableUnionsXAndMarksMissingCells() {
        val spec =
            LineChartSpec(
                series =
                    listOf(
                        LineSeries("a", "A", listOf(ChartPoint(0.0, 1.0), ChartPoint(1.0, 2.0))),
                        LineSeries("b", "B", listOf(ChartPoint(1.0, 20.0), ChartPoint(2.0, 30.0))),
                    ),
            )
        val table = lineChartDataTable(spec)
        assertEquals(listOf("X", "A", "B"), table.columnHeaders)
        assertEquals(3, table.rowCount)
        assertEquals(ChartDataTable.Row("0", listOf("1", "")), table.rows[0])
        assertEquals(ChartDataTable.Row("1", listOf("2", "20")), table.rows[1])
        assertEquals(ChartDataTable.Row("2", listOf("", "30")), table.rows[2])
    }

    @Test
    fun lineTableSkipsGapPoints() {
        val spec =
            LineChartSpec(
                series = listOf(LineSeries("a", "A", listOf(ChartPoint(0.0, 5.0), ChartPoint.gap(1.0)))),
            )
        val table = lineChartDataTable(spec)
        // The gap x is still a finite x (1.0), but its y is missing.
        assertEquals(2, table.rowCount)
        assertEquals("5", table.rows[0].cells[0])
        assertEquals("", table.rows[1].cells[0])
    }

    @Test
    fun scatterTableMatchesLineLayout() {
        val spec =
            ScatterChartSpec(
                series = listOf(ScatterSeries("a", "A", listOf(ChartPoint(2.0, 4.0), ChartPoint(1.0, 3.0)))),
            )
        val table = scatterChartDataTable(spec)
        assertEquals(listOf("X", "A"), table.columnHeaders)
        // Rows are sorted ascending by x.
        assertEquals(listOf("1", "2"), table.rows.map { it.key })
        assertEquals("3", table.rows[0].cells[0])
    }

    @Test
    fun barTableIsCategoryByGroup() {
        val spec =
            BarChartSpec(
                groups =
                    listOf(
                        BarGroup("g1", "G1", listOf(BarEntry(0.0, 10.0), BarEntry(1.0, 12.0))),
                        BarGroup("g2", "G2", listOf(BarEntry(0.0, 7.0))),
                    ),
            )
        val table = barChartDataTable(spec)
        assertEquals(listOf("Category", "G1", "G2"), table.columnHeaders)
        assertEquals(2, table.rowCount)
        assertEquals(ChartDataTable.Row("0", listOf("10", "7")), table.rows[0])
        assertEquals(ChartDataTable.Row("1", listOf("12", "")), table.rows[1])
    }
}
