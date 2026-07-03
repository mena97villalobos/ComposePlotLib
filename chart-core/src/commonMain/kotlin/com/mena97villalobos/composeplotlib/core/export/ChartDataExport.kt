package com.mena97villalobos.composeplotlib.core.export

import com.mena97villalobos.composeplotlib.core.table.ChartDataTable

/**
 * Serializes a [ChartDataTable] to RFC 4180 CSV: a header row (the table's column headers) followed by one row per
 * data row (`row.key` in the first column, then `row.cells`). Fields containing the [delimiter], a quote, or a line
 * break are quoted and internal quotes doubled. Pairs with `lineChartDataTable` / `scatterChartDataTable` /
 * `barChartDataTable` to export the underlying data of a chart.
 *
 * @param delimiter field separator (default `,`).
 * @param lineSeparator row separator (default `\n`).
 */
fun ChartDataTable.toCsv(
    delimiter: Char = ',',
    lineSeparator: String = "\n",
): String {
    val builder = StringBuilder()
    builder.append(columnHeaders.joinToString(delimiter.toString()) { csvField(it, delimiter) })
    for (row in rows) {
        builder.append(lineSeparator)
        val fields = listOf(row.key) + row.cells
        builder.append(fields.joinToString(delimiter.toString()) { csvField(it, delimiter) })
    }
    return builder.toString()
}

/**
 * Serializes a [ChartDataTable] to JSON: `{"caption":…,"columns":[…],"rows":[{column:value,…},…]}`. Each row object
 * maps every column header to its value (`row.key` for the first column, then `row.cells`), so the payload is
 * self-describing without positional indexing.
 */
fun ChartDataTable.toJson(): String {
    val builder = StringBuilder()
    builder.append('{')
    builder.append("\"caption\":").append(jsonString(caption)).append(',')
    builder.append("\"columns\":[").append(columnHeaders.joinToString(",") { jsonString(it) }).append("],")
    builder.append("\"rows\":[")
    rows.forEachIndexed { rowIndex, row ->
        if (rowIndex > 0) builder.append(',')
        val values = listOf(row.key) + row.cells
        builder.append('{')
        columnHeaders.forEachIndexed { columnIndex, header ->
            if (columnIndex > 0) builder.append(',')
            builder.append(jsonString(header)).append(':').append(jsonString(values.getOrElse(columnIndex) { "" }))
        }
        builder.append('}')
    }
    builder.append("]}")
    return builder.toString()
}

private fun csvField(
    value: String,
    delimiter: Char,
): String {
    val needsQuoting = value.any { it == delimiter || it == '"' || it == '\n' || it == '\r' }
    if (!needsQuoting) return value
    return "\"" + value.replace("\"", "\"\"") + "\""
}

private fun jsonString(value: String): String {
    val builder = StringBuilder()
    builder.append('"')
    for (ch in value) {
        when (ch) {
            '"' -> builder.append("\\\"")
            '\\' -> builder.append("\\\\")
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            '\t' -> builder.append("\\t")
            else ->
                if (ch < ' ') {
                    builder.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                } else {
                    builder.append(ch)
                }
        }
    }
    builder.append('"')
    return builder.toString()
}
