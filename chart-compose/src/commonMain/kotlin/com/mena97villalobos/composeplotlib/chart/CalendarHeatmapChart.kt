package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.render.CalendarHeatmapChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapSpec
import com.mena97villalobos.composeplotlib.core.spec.resolvedWeekCount

/**
 * Calendar / year heatmap: a GitHub-contribution-style grid where each [com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapDay]
 * sits in a week-column × weekday-row cell colored by its value. Read-only (no pan/zoom/selection), like
 * [GaugeChart] / [RadialBarChart]. Callers supply 0-based day offsets, so the library stays date-library-free.
 *
 * @param spec days, calendar layout, and color scale; see [CalendarHeatmapSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun CalendarHeatmapChart(
    spec: CalendarHeatmapSpec,
    modifier: Modifier = Modifier,
    chromeTheme: ChartChromeTheme? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyles = chartLabelTextStyles(spec)
    val chromeResolved = chromeTheme ?: chartChromeTheme()
    val renderStyles =
        remember(spec, labelStyles, chromeResolved) {
            ChartRenderStyles(labelStyles, chromeResolved)
        }
    val dummyViewport = remember(spec) { defaultViewport(spec) }
    var canvasSize by remember { mutableStateOf(SizeD.Zero) }
    val fontScale = LocalDensity.current.fontScale
    val plan =
        remember(spec, dummyViewport, canvasSize, fontScale) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                ChartPlanner.planCalendarHeatmapChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val description = spec.accessibility.contentDescription ?: calendarHeatmapContentDescription(spec)

    Box(
        modifier =
            modifier
                .semantics { contentDescription = description }
                .onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentPlan = plan ?: return@Canvas
            with(CalendarHeatmapChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

private fun calendarHeatmapContentDescription(spec: CalendarHeatmapSpec): String {
    if (spec.days.isEmpty()) return "Calendar heatmap with no days."
    return "Calendar heatmap over ${spec.resolvedWeekCount()} weeks with ${spec.days.size} days of data."
}
