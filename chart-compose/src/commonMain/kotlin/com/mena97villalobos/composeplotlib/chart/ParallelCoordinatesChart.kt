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
import com.mena97villalobos.composeplotlib.chart.internal.render.ParallelCoordinatesChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.spec.ParallelCoordinatesChartSpec

/**
 * Parallel coordinates chart: one vertical axis per dimension and one polyline per record, connecting each record's
 * value on every axis (each axis normalized to its own range). Useful for multi-dimensional correlation/cluster
 * exploration. Read-only (no pan/zoom/selection or brushing in v1).
 *
 * @param spec the axes, series, and styling; see [ParallelCoordinatesChartSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun ParallelCoordinatesChart(
    spec: ParallelCoordinatesChartSpec,
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
                ChartPlanner.planParallelCoordinatesChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val description = spec.accessibility.contentDescription ?: parallelCoordinatesContentDescription(spec)

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
            with(ParallelCoordinatesChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

private fun parallelCoordinatesContentDescription(spec: ParallelCoordinatesChartSpec): String {
    if (spec.axes.isEmpty()) return "Parallel coordinates chart with no axes."
    return "Parallel coordinates chart across ${spec.axes.size} axes (${spec.axes.joinToString(", ") { it.label }}) with ${spec.series.size} records."
}
