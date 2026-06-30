package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.render.GaugeChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.clampedProgress
import kotlin.math.roundToInt

/**
 * Gauge / progress-ring chart: a track arc plus a progress arc covering `sweepDegrees * progress`, with an optional
 * [centerContent] slot (e.g. a percentage label) centered in the ring. Radial and read-only — there is no pan/zoom
 * or selection.
 *
 * @param centerContent optional composable centered inside the ring (measured, then centered at the ring center).
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme].
 */
@Composable
fun GaugeChart(
    spec: GaugeChartSpec,
    modifier: Modifier = Modifier,
    centerContent: (@Composable BoxScope.() -> Unit)? = null,
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
                ChartPlanner.planGaugeChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val radialLayout =
        remember(plan) {
            plan?.layout?.plotBounds?.let { PieChartLayoutMath.layout(it, holeRadiusRatio = null) }
        }
    var centerContentSize by remember { mutableStateOf(IntSize.Zero) }
    val progressPercent = (spec.clampedProgress() * 100).roundToInt()

    Box(
        modifier =
            modifier
                .semantics { contentDescription = "Gauge, $progressPercent percent" }
                .onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentPlan = plan ?: return@Canvas
            with(GaugeChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }

        if (centerContent != null && radialLayout != null) {
            val cx = radialLayout.center.x
            val cy = radialLayout.center.y
            Box(
                modifier =
                    Modifier
                        .onSizeChanged { centerContentSize = it }
                        .offset {
                            IntOffset(
                                (cx - centerContentSize.width / 2.0).roundToInt(),
                                (cy - centerContentSize.height / 2.0).roundToInt(),
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                centerContent()
            }
        }
    }
}
