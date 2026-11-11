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
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeTreemapLabelMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.render.TreemapChartRenderer
import com.mena97villalobos.composeplotlib.chart.internal.render.TreemapHierarchyRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.TreemapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Treemap: tiles [TreemapChartSpec.nodes] into area-proportional rectangles via a squarified layout (aspect ratios
 * kept near 1), each filled with the node color and labeled when it fits. Read-only (no pan/zoom/selection), like
 * [GaugeChart] / [CalendarHeatmapChart].
 *
 * @param spec nodes and styling; see [TreemapChartSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun TreemapChart(
    spec: TreemapChartSpec,
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
                ChartPlanner.planTreemapChart(chartLayoutBaseInput(canvasSize, dummyViewport, fontScale), spec)
            }
        }
    val description = spec.accessibility.contentDescription ?: treemapContentDescription(spec)

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
            with(TreemapChartRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

private fun treemapContentDescription(spec: TreemapChartSpec): String {
    if (spec.nodes.isEmpty()) return "Treemap with no tiles."
    return "Treemap. Tiles: ${spec.nodes.joinToString(", ") { "${it.label} ${it.value}" }}."
}

/**
 * Hierarchical treemap: lays out [TreemapHierarchySpec.nodes] as squarified tiles and draws each tile's children
 * inside it (below a header band with the parent's label) down to [TreemapHierarchySpec.maxDepth] levels. Tile areas
 * are proportional to node sizes within each parent; tiles narrower than [TreemapHierarchySpec.minTileSidePx] are
 * merged into one aggregate tile per parent, and labels show the node name and the
 * [TreemapHierarchySpec.valueFormatter] text when they fit.
 *
 * A flat list of nodes is a one-level treemap; the flat [TreemapChartSpec] overload keeps working unchanged.
 *
 * @param spec the hierarchy and styling; see [TreemapHierarchySpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun TreemapChart(
    spec: TreemapHierarchySpec,
    modifier: Modifier = Modifier,
    chromeTheme: ChartChromeTheme? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyles = chartLabelTextStyles(spec)
    val chromeResolved = chromeTheme ?: chartChromeTheme()
    val renderStyles =
        remember(labelStyles, chromeResolved) {
            ChartRenderStyles(labelStyles, chromeResolved)
        }
    val labelMeasurer = remember(textMeasurer, labelStyles) { ComposeTreemapLabelMeasurer(textMeasurer, labelStyles) }
    val viewport = remember { defaultTreemapHierarchyViewport() }
    var canvasSize by remember { mutableStateOf(SizeD.Zero) }
    val fontScale = LocalDensity.current.fontScale
    val plan: TreemapHierarchyPlan? =
        remember(spec, canvasSize, fontScale, labelMeasurer) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                ChartPlanner.planTreemapHierarchy(
                    input = chartLayoutBaseInput(canvasSize, viewport, fontScale),
                    spec = spec,
                    rootNodeId = null,
                    labelMeasurer = labelMeasurer,
                )
            }
        }
    val description = spec.accessibility.contentDescription ?: treemapHierarchyContentDescription(plan, spec)

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
            with(TreemapHierarchyRenderer) {
                render(currentPlan, textMeasurer, renderStyles)
            }
        }
    }
}

/** The treemap planner ignores the viewport; any fixed unit window will do. */
private fun defaultTreemapHierarchyViewport() = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0))

/** "Treemap. Tiles: Photos 12 GB, Music 3 GB." over the top-level tiles that are drawn. */
private fun treemapHierarchyContentDescription(
    plan: TreemapHierarchyPlan?,
    spec: TreemapHierarchySpec,
): String {
    val top = plan?.tiles?.filter { it.depth == 1 }.orEmpty()
    if (top.isEmpty()) return if (spec.nodes.isEmpty()) "Treemap with no tiles." else "Treemap."
    return "Treemap. Tiles: ${top.joinToString(", ") { tile -> "${tile.label} ${tile.valueText ?: tile.value.toString()}" }}."
}
