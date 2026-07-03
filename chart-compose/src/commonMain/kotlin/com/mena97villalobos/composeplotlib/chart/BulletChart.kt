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
import com.mena97villalobos.composeplotlib.chart.internal.render.BulletChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.spec.BulletChartSpec

/**
 * Bullet graph: a compact linear KPI display — qualitative range bands behind a featured measure bar and an
 * optional target marker. Read-only (no pan/zoom/selection), like [GaugeChart]. Pair it with your own title /
 * value caption in the surrounding layout.
 *
 * @param spec measure value, qualitative bands, target, domain, and styling; see [BulletChartSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height; bullets are short and wide)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun BulletChart(
    spec: BulletChartSpec,
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
                ChartPlanner.planBulletChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val description = spec.accessibility.contentDescription ?: bulletContentDescription(spec)

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
            with(BulletChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

private fun bulletContentDescription(spec: BulletChartSpec): String {
    val fmt = spec.valueFormatter
    val targetPart = spec.target?.let { " Target ${fmt(it)}." } ?: ""
    return "Bullet graph. Value ${fmt(spec.value)} on a scale from ${fmt(spec.domainMin)} to " +
        "${fmt(spec.domainMax)}.$targetPart"
}
