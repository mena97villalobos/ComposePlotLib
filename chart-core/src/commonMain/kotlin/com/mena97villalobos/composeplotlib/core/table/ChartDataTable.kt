package com.mena97villalobos.composeplotlib.core.table

import com.mena97villalobos.composeplotlib.core.spec.AxisLabelFormatter
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec

/**
 * A screen-reader-navigable **tabular view** of a chart's underlying data — the "view data" accessibility
 * alternative. `chart-core` knows every datum, so these builders turn a spec into headers + rows of already-formatted
 * strings; `chart-compose` renders it (`ChartDataTable(spec)`), or a host can serialize it (CSV/JSON) itself.
 *
 * @property caption Short human description of the table (e.g. `"Line chart data"`).
 * @property columnHeaders Column titles, including the leading row-key column (e.g. `["X", "Series A", "Series B"]`).
 * @property rows Data rows in display order; each row's [Row.cells] aligns with [columnHeaders] after the row key.
 */
data class ChartDataTable(
    val caption: String,
    val columnHeaders: List<String>,
    val rows: List<Row>,
) {
    /**
     * One table row.
     *
     * @property key The leading cell (row header) — the value under [columnHeaders]`[0]` (e.g. the x value/category).
     * @property cells The remaining cells, aligned with `columnHeaders.drop(1)`. Empty string marks a missing value.
     */
    data class Row(
        val key: String,
        val cells: List<String>,
    )

    /** Total column count including the row-key column. */
    val columnCount: Int get() = columnHeaders.size

    /** Row count excluding the header row. */
    val rowCount: Int get() = rows.size
}

/** Builds a "missing" placeholder for absent cells; distinct from a formatted `0`. */
private const val MISSING_CELL = ""

/**
 * Tabular view of a [LineChartSpec]: one column per series, one row per distinct finite x (ascending). Values use
 * each axis' [AxisLabelFormatter] so the table matches the on-chart tick formatting. All series are included
 * (hidden series too — the table is the full data). Gap points (non-finite y) are treated as missing.
 */
fun lineChartDataTable(
    spec: LineChartSpec,
    xHeader: String = "X",
    xFormatter: AxisLabelFormatter = spec.xAxis.labelFormatter,
    yFormatter: AxisLabelFormatter = spec.yAxis.labelFormatter,
    caption: String = "Line chart data",
): ChartDataTable {
    val headers = listOf(xHeader) + spec.series.map { it.label }
    val xs = sortedUniqueFiniteX(spec.series.flatMap { s -> s.points.mapNotNull { if (it.x.isFinite()) it.x else null } })
    val perSeries =
        spec.series.map { s ->
            val map = HashMap<Double, Double>()
            for (p in s.points) {
                if (p.x.isFinite() && p.y.isFinite() && p.x !in map) map[p.x] = p.y
            }
            map
        }
    val rows =
        xs.map { x ->
            ChartDataTable.Row(
                key = xFormatter(x),
                cells = perSeries.map { m -> m[x]?.let(yFormatter) ?: MISSING_CELL },
            )
        }
    return ChartDataTable(caption = caption, columnHeaders = headers, rows = rows)
}

/**
 * Tabular view of a [ScatterChartSpec]: one column per series, one row per distinct finite x (ascending), matching
 * [lineChartDataTable] layout. Points with the same x within a series keep the first.
 */
fun scatterChartDataTable(
    spec: ScatterChartSpec,
    xHeader: String = "X",
    xFormatter: AxisLabelFormatter = spec.xAxis.labelFormatter,
    yFormatter: AxisLabelFormatter = spec.yAxis.labelFormatter,
    caption: String = "Scatter chart data",
): ChartDataTable {
    val headers = listOf(xHeader) + spec.series.map { it.label }
    val xs = sortedUniqueFiniteX(spec.series.flatMap { s -> s.points.mapNotNull { if (it.x.isFinite()) it.x else null } })
    val perSeries =
        spec.series.map { s ->
            val map = HashMap<Double, Double>()
            for (p in s.points) {
                if (p.x.isFinite() && p.y.isFinite() && p.x !in map) map[p.x] = p.y
            }
            map
        }
    val rows =
        xs.map { x ->
            ChartDataTable.Row(
                key = xFormatter(x),
                cells = perSeries.map { m -> m[x]?.let(yFormatter) ?: MISSING_CELL },
            )
        }
    return ChartDataTable(caption = caption, columnHeaders = headers, rows = rows)
}

/**
 * Tabular view of a [BarChartSpec]: one column per group, one row per category index (aligned by index across
 * groups). The row key is the category's x value formatted with the x-axis formatter. Values use the y-axis formatter.
 */
fun barChartDataTable(
    spec: BarChartSpec,
    categoryHeader: String = "Category",
    categoryFormatter: AxisLabelFormatter = spec.xAxis.labelFormatter,
    valueFormatter: AxisLabelFormatter = spec.yAxis.labelFormatter,
    caption: String = "Bar chart data",
): ChartDataTable {
    val headers = listOf(categoryHeader) + spec.groups.map { it.label }
    val categoryCount = spec.groups.maxOfOrNull { it.entries.size } ?: 0
    val rows =
        (0 until categoryCount).map { index ->
            val keyX = spec.groups.firstNotNullOfOrNull { it.entries.getOrNull(index)?.x } ?: index.toDouble()
            ChartDataTable.Row(
                key = categoryFormatter(keyX),
                cells =
                    spec.groups.map { g ->
                        val entry = g.entries.getOrNull(index)
                        if (entry != null && entry.y.isFinite()) valueFormatter(entry.y) else MISSING_CELL
                    },
            )
        }
    return ChartDataTable(caption = caption, columnHeaders = headers, rows = rows)
}

private fun sortedUniqueFiniteX(xs: List<Double>): List<Double> = xs.distinct().sorted()
