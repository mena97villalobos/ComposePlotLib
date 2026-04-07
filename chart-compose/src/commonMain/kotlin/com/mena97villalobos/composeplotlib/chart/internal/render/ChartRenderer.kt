package com.mena97villalobos.composeplotlib.chart.internal.render

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan

/** Draws a typed plan and shared legend pass. */
internal interface ChartRenderer<T> {
    fun DrawScope.render(
        plan: T,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    )
}

internal fun DrawScope.renderLegend(
    plan: ChartLayoutPlan,
    textMeasurer: TextMeasurer,
    renderStyles: ChartRenderStyles,
) {
    com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter.run {
        drawLegend(plan, textMeasurer, renderStyles)
    }
}
