package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.table.barChartDataTable
import com.mena97villalobos.composeplotlib.core.table.lineChartDataTable
import com.mena97villalobos.composeplotlib.core.table.scatterChartDataTable
import com.mena97villalobos.composeplotlib.core.table.ChartDataTable as ChartDataTableModel

/**
 * Renders a chart's underlying data as an accessible, scrollable table — the "view data" alternative for
 * screen-reader users (and a plain visual fallback). Each data row is a single merged semantics node so TalkBack /
 * VoiceOver announces the whole row (`"X 1, Series A 2, Series B no value"`); the caption is a heading.
 *
 * Pass a [ChartDataTableModel] built in `chart-core` (e.g. `lineChartDataTable(spec)`), or use the spec overloads
 * below to build it inline.
 *
 * @param table the tabular data to render
 * @param modifier layout modifier for the outer [Column]
 * @param cellMinWidth minimum width per column cell (columns scroll horizontally when they overflow)
 */
@Composable
fun ChartDataTable(
    table: ChartDataTableModel,
    modifier: Modifier = Modifier,
    cellMinWidth: Dp = 72.dp,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = table.caption,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp).semantics { heading() },
        )
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row {
                for (header in table.columnHeaders) {
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = cellMinWidth).padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            for (row in table.rows) {
                val cells = listOf(row.key) + row.cells
                val rowDescription =
                    table.columnHeaders
                        .zip(cells)
                        .joinToString(", ") { (h, v) -> "$h ${v.ifEmpty { "no value" }}" }
                Row(modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = rowDescription }) {
                    for (value in cells) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.widthIn(min = cellMinWidth).padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Accessible data table for a [LineChartSpec] (one column per series, one row per distinct x). */
@Composable
fun ChartDataTable(
    spec: LineChartSpec,
    modifier: Modifier = Modifier,
    cellMinWidth: Dp = 72.dp,
) = ChartDataTable(lineChartDataTable(spec), modifier, cellMinWidth)

/** Accessible data table for a [ScatterChartSpec] (one column per series, one row per distinct x). */
@Composable
fun ChartDataTable(
    spec: ScatterChartSpec,
    modifier: Modifier = Modifier,
    cellMinWidth: Dp = 72.dp,
) = ChartDataTable(scatterChartDataTable(spec), modifier, cellMinWidth)

/** Accessible data table for a [BarChartSpec] (one column per group, one row per category). */
@Composable
fun ChartDataTable(
    spec: BarChartSpec,
    modifier: Modifier = Modifier,
    cellMinWidth: Dp = 72.dp,
) = ChartDataTable(barChartDataTable(spec), modifier, cellMinWidth)
