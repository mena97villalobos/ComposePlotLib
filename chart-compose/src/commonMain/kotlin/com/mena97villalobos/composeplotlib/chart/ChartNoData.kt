package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Declarative copy and optional semantics for an empty chart; use with [LineChart]/[BarChart] `noDataUi`
 * when you do not need a fully custom `noData` composable (line, bar, scatter, and pie charts).
 */
data class ChartNoDataUi(
    val message: String,
    val title: String? = null,
    /**
     * TalkBack label for the empty state; defaults to [message] and [title] combined when null.
     */
    val contentDescription: String? = null,
)

/** Suggested messaging for showcase and app defaults; override per product. */
object ChartNoDataDefaults {
    const val LineChartTitle: String = "No series data"
    const val LineChartMessage: String = "Add points to a line series or load a dataset to see this chart."

    const val BarChartTitle: String = "No bar data"
    const val BarChartMessage: String = "Add bar groups or entries to see this chart."

    const val ScatterChartTitle: String = "No scatter data"
    const val ScatterChartMessage: String = "Add points to a scatter series or load a dataset to see this chart."

    const val PieChartTitle: String = "No pie data"
    const val PieChartMessage: String = "Add slices with positive values or load a dataset to see this chart."

    fun lineChart(
        title: String = LineChartTitle,
        message: String = LineChartMessage,
    ): ChartNoDataUi = ChartNoDataUi(title = title, message = message)

    fun barChart(
        title: String = BarChartTitle,
        message: String = BarChartMessage,
    ): ChartNoDataUi = ChartNoDataUi(title = title, message = message)

    fun scatterChart(
        title: String = ScatterChartTitle,
        message: String = ScatterChartMessage,
    ): ChartNoDataUi = ChartNoDataUi(title = title, message = message)

    fun pieChart(
        title: String = PieChartTitle,
        message: String = PieChartMessage,
    ): ChartNoDataUi = ChartNoDataUi(title = title, message = message)
}

/**
 * Centered Material-styled placeholder for empty charts.
 *
 * Prefer `noData` for full custom layouts; use [ChartNoDataUi] + this composable for consistent title/body.
 */
@Composable
fun BoxScope.DefaultNoDataOverlay(
    message: String = "No data available",
    modifier: Modifier = Modifier,
    title: String? = null,
    contentDescription: String? = null,
) {
    val semanticsText = contentDescription ?: listOfNotNull(title, message).joinToString(". ")
    Box(
        modifier =
            modifier
                .align(Alignment.Center)
                .semantics { this.contentDescription = semanticsText }
                .background(Color(0x14000000), MaterialTheme.shapes.medium)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            } else {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Renders [ui] with [DefaultNoDataOverlay]. */
@Composable
fun BoxScope.ChartNoDataContent(ui: ChartNoDataUi) {
    DefaultNoDataOverlay(
        message = ui.message,
        title = ui.title,
        contentDescription = ui.contentDescription,
    )
}
