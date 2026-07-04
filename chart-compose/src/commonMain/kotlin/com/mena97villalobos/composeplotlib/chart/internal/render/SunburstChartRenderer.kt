package com.mena97villalobos.composeplotlib.chart.internal.render

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.core.layout.SunburstChartPlan

/** [com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter] bridge for [SunburstChartPlan]. */
internal object SunburstChartRenderer : ChartRenderer<SunburstChartPlan> {
    override fun DrawScope.render(
        plan: SunburstChartPlan,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    ) {
        with(com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter) {
            drawPlan(plan.layout, textMeasurer, renderStyles)
        }
    }
}
