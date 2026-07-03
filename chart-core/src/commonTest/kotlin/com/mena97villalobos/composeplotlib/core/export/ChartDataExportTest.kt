package com.mena97villalobos.composeplotlib.core.export

import com.mena97villalobos.composeplotlib.core.table.ChartDataTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartDataExportTest {
    private val table =
        ChartDataTable(
            caption = "Sales",
            columnHeaders = listOf("Month", "North", "South"),
            rows =
                listOf(
                    ChartDataTable.Row(key = "Jan", cells = listOf("10", "20")),
                    ChartDataTable.Row(key = "Feb, 2026", cells = listOf("15", "")),
                ),
        )

    @Test
    fun csvHasHeaderAndQuotesFieldsWithDelimiters() {
        val csv = table.toCsv()
        val lines = csv.split("\n")
        assertEquals("Month,North,South", lines[0])
        assertEquals("Jan,10,20", lines[1])
        // "Feb, 2026" contains the delimiter, so it must be quoted; missing cell is empty.
        assertEquals("\"Feb, 2026\",15,", lines[2])
    }

    @Test
    fun csvDoublesInternalQuotes() {
        val quoted =
            ChartDataTable(
                caption = "c",
                columnHeaders = listOf("A"),
                rows = listOf(ChartDataTable.Row(key = "say \"hi\"", cells = emptyList())),
            )
        assertEquals("A\n\"say \"\"hi\"\"\"", quoted.toCsv())
    }

    @Test
    fun jsonIsSelfDescribing() {
        val json = table.toJson()
        assertTrue(json.startsWith("{\"caption\":\"Sales\""))
        assertTrue(json.contains("\"columns\":[\"Month\",\"North\",\"South\"]"))
        assertTrue(json.contains("{\"Month\":\"Jan\",\"North\":\"10\",\"South\":\"20\"}"))
        // The delimiter-containing key is a plain JSON string (no CSV quoting).
        assertTrue(json.contains("\"Month\":\"Feb, 2026\""))
    }
}
