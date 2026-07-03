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
import com.mena97villalobos.composeplotlib.chart.internal.render.RadialBarChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.spec.RadialBarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.resolvedMaxValue

/**
 * Radial bar (circular progress-bar) chart: one concentric ring per [RadialBarChartSpec.tracks] entry (outermost
 * first), each a background track plus a foreground value arc proportional to `value / maxValue`. Read-only (no
 * pan/zoom/selection), like [GaugeChart] / [FunnelChart]; an optional legend labels the rings.
 *
 * @param spec rings, scale, angles, and styling; see [RadialBarChartSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun RadialBarChart(
    spec: RadialBarChartSpec,
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
                ChartPlanner.planRadialBarChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val description = spec.accessibility.contentDescription ?: radialBarContentDescription(spec)

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
            with(RadialBarChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

private fun radialBarContentDescription(spec: RadialBarChartSpec): String {
    if (spec.tracks.isEmpty()) return "Radial bar chart with no rings."
    val max = spec.resolvedMaxValue()
    val parts = spec.tracks.joinToString(", ") { "${it.label} ${it.value} of $max" }
    return "Radial bar chart. Rings: $parts."
}
