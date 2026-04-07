package com.mena97villalobos.composeplotlib.chart.internal.render

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.core.layout.BarChartPlan

/** [com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter] bridge for [BarChartPlan]. */
internal object BarChartRenderer : ChartRenderer<BarChartPlan> {
    override fun DrawScope.render(
        plan: BarChartPlan,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    ) {
        with(com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter) {
            drawPlan(plan.layout, textMeasurer, renderStyles)
        }
        renderLegend(plan.layout, textMeasurer, renderStyles)
    }
}
