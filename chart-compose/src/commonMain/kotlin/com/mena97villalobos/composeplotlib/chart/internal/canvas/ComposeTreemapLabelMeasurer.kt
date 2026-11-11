package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.TreemapLabelLine
import com.mena97villalobos.composeplotlib.core.layout.TreemapLabelMeasurer

/**
 * [TreemapLabelMeasurer] backed by Compose text layout, using the same styles the canvas adapter draws with (see
 * `chartLabelTextStyles(TreemapHierarchySpec)`), so the planner's fit decisions match what is drawn. Results are cached
 * per (line, text): a plan measures each label and its shortened candidates, and re-plans on resize reuse them.
 */
internal class ComposeTreemapLabelMeasurer(
    private val textMeasurer: TextMeasurer,
    private val styles: ChartLabelTextStyles,
) : TreemapLabelMeasurer {
    private val cache = HashMap<Pair<TreemapLabelLine, String>, SizeD>()

    override fun measure(
        text: String,
        line: TreemapLabelLine,
    ): SizeD =
        cache.getOrPut(line to text) {
            val style: TextStyle =
                when (line) {
                    TreemapLabelLine.Name -> styles.xAxis
                    TreemapLabelLine.Value -> styles.yAxis
                    TreemapLabelLine.Header -> styles.pieSliceLabel
                }
            val size = textMeasurer.measureChartText(text, style).size
            SizeD(size.width.toDouble(), size.height.toDouble())
        }
}
