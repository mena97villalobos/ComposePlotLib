package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * **Faceting / small multiples**: lays out one chart per [items] entry in a responsive grid of [columns], each cell
 * an optional [facetTitle] above the [facet] chart. This is a pure Compose composition layer — it does not impose a
 * chart type.
 *
 * **Shared vs. per-facet scales** is up to you:
 * - *Shared scales* — build each facet's spec/viewport from a common domain (e.g. one `defaultViewport` or a shared
 *   `ChartViewportState`) so facets are directly comparable (and, with a shared `ChartViewportState`, pan/zoom
 *   together — see [SyncChartViewports]).
 * - *Per-facet scales* — derive each facet's domain from its own data.
 *
 * @param items the data driving each facet, in reading order (row-major).
 * @param modifier layout modifier for the outer [Column].
 * @param columns number of columns (coerced to at least 1).
 * @param spacing gap between facets, both directions.
 * @param facetHeight height of each facet's chart area.
 * @param facetTitle optional per-facet title shown above the chart.
 * @param facet renders one facet's chart for the given item; it is placed in a `fillMaxWidth().height(facetHeight)` box.
 */
@Composable
fun <T> ChartFacetGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp = 8.dp,
    facetHeight: Dp = 160.dp,
    facetTitle: ((T) -> String)? = null,
    facet: @Composable (T) -> Unit,
) {
    val columnCount = columns.coerceAtLeast(1)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columnCount).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                rowItems.forEach { item ->
                    Column(modifier = Modifier.weight(1f)) {
                        if (facetTitle != null) {
                            Text(
                                text = facetTitle(item),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(facetHeight)) {
                            facet(item)
                        }
                    }
                }
                // Pad the final row so cells keep a consistent width across rows.
                repeat(columnCount - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
