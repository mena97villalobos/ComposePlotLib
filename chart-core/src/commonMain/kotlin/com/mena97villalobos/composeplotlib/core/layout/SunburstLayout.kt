package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.lighten
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode
import com.mena97villalobos.composeplotlib.core.spec.totalValue

/** A laid-out sunburst node: its ring [depth], angular span (degrees), resolved fill [color], and [label]. */
data class SunburstSlice(
    val depth: Int,
    val startAngleDegrees: Double,
    val sweepAngleDegrees: Double,
    val color: ChartColor,
    val label: String,
)

/**
 * Flattens a [SunburstChartSpec] into angle-and-depth slices. The [SunburstChartSpec.root] spans
 * `[startAngleDegrees, startAngleDegrees + sweepDegrees]`; each child occupies a proportional sub-span within its
 * parent. Colors resolve to each node's explicit color, else a Tableau palette color per top-level branch, lightened
 * with depth. When [SunburstChartSpec.showRoot] is false the root slice is omitted (rings start at depth 1).
 */
fun sunburstLayout(spec: SunburstChartSpec): List<SunburstSlice> {
    val out = ArrayList<SunburstSlice>()
    layoutNode(
        node = spec.root,
        depth = 0,
        startAngle = spec.startAngleDegrees,
        sweep = spec.sweepDegrees,
        branchColor = spec.root.color ?: ChartColor(0xFFBAB0AC),
        includeSelf = spec.showRoot,
        out = out,
    )
    return out
}

private fun layoutNode(
    node: SunburstNode,
    depth: Int,
    startAngle: Double,
    sweep: Double,
    branchColor: ChartColor,
    includeSelf: Boolean,
    out: MutableList<SunburstSlice>,
) {
    val resolvedColor = node.color ?: branchColor
    if (includeSelf) {
        out += SunburstSlice(depth, startAngle, sweep, resolvedColor, node.label)
    }
    val total = node.children.sumOf { it.totalValue() }
    if (total <= 0.0) return
    var angle = startAngle
    node.children.forEachIndexed { index, child ->
        val childSweep = sweep * child.totalValue() / total
        val childColor =
            when {
                child.color != null -> child.color
                depth == 0 -> ChartPalettes.cyclic(ChartPalettes.Tableau10, index)
                else -> branchColor.lighten(0.14)
            }
        layoutNode(
            node = child,
            depth = depth + 1,
            startAngle = angle,
            sweep = childSweep,
            branchColor = childColor,
            includeSelf = true,
            out = out,
        )
        angle += childSweep
    }
}
