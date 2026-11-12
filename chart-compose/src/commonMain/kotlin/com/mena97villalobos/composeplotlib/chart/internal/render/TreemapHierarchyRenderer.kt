package com.mena97villalobos.composeplotlib.chart.internal.render

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeCanvasAdapter
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan

/** [ComposeCanvasAdapter] bridge for [TreemapHierarchyPlan]. */
internal object TreemapHierarchyRenderer : ChartRenderer<TreemapHierarchyPlan> {
    override fun DrawScope.render(
        plan: TreemapHierarchyPlan,
        textMeasurer: TextMeasurer,
        renderStyles: ChartRenderStyles,
    ) {
        with(ComposeCanvasAdapter) {
            drawPlan(plan.layout, textMeasurer, renderStyles)
        }
    }
}
