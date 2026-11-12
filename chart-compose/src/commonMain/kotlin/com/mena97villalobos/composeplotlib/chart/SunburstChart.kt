package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.render.SunburstChartRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.SunburstChartPlan
import com.mena97villalobos.composeplotlib.core.layout.SunburstHierarchyPlan
import com.mena97villalobos.composeplotlib.core.selection.HierarchyNodeHit
import com.mena97villalobos.composeplotlib.core.selection.SunburstHitResolver
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec

/**
 * Sunburst (hierarchical radial) chart: draws the [SunburstChartSpec.root] hierarchy as concentric rings, one per
 * depth, where each node's angular span is proportional to its value within its parent. No pan or zoom.
 *
 * **Interaction** mirrors the hierarchical [TreemapChart]: [onNodeClick] / [onNodeLongClick] receive a
 * [HierarchyNodeHit] for the segment under the pointer (its id is [com.mena97villalobos.composeplotlib.core.spec.SunburstNode.id],
 * or the label when no id is set; [HierarchyNodeHit.depth] `0` is the center disk). Points in a ring gap or outside
 * the rings report nothing ([SunburstHitResolver]). With neither callback set the chart adds no pointer input.
 *
 * **Drill-in:** pass a node's id as [rootNodeId] to draw that node at the center with its descendants around it,
 * driven by a [com.mena97villalobos.composeplotlib.chart.state.ChartDrillState] as for the treemap. Unknown ids show
 * the spec's root. **Highlight:** segments whose id is in [highlightedNodeIds] are outlined per [highlight].
 *
 * @param spec the hierarchy and styling; see [SunburstChartSpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 * @param rootNodeId id of the node to draw at the center; `null` draws [SunburstChartSpec.root]
 * @param highlightedNodeIds ids of nodes to draw highlighted
 * @param onNodeClick called when a segment is tapped
 * @param onNodeLongClick called when a segment is long-pressed
 * @param highlight outline and dimming for [highlightedNodeIds]
 */
@Composable
fun SunburstChart(
    spec: SunburstChartSpec,
    modifier: Modifier = Modifier,
    chromeTheme: ChartChromeTheme? = null,
    rootNodeId: String? = null,
    highlightedNodeIds: Set<String> = emptySet(),
    onNodeClick: ((HierarchyNodeHit) -> Unit)? = null,
    onNodeLongClick: ((HierarchyNodeHit) -> Unit)? = null,
    highlight: HierarchyHighlightStyle = HierarchyHighlightStyle(),
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
    val plan: SunburstHierarchyPlan? =
        remember(spec, rootNodeId, highlightedNodeIds, highlight, dummyViewport, canvasSize, fontScale) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                ChartPlanner.planSunburstHierarchy(
                    input = chartLayoutBaseInput(canvasSize, dummyViewport, fontScale),
                    spec = spec,
                    rootNodeId = rootNodeId,
                    highlightedNodeIds = highlightedNodeIds,
                    highlight = highlight,
                )
            }
        }
    val latestPlan = rememberUpdatedState(plan)
    val description = spec.accessibility.contentDescription ?: "Sunburst chart of ${spec.root.label}."

    fun hitAt(offset: Offset): HierarchyNodeHit? {
        val current = latestPlan.value ?: return null
        val segment = SunburstHitResolver.resolve(OffsetD(offset.x.toDouble(), offset.y.toDouble()), current) ?: return null
        return SunburstHitResolver.hitFor(segment, current)
    }

    Box(
        modifier =
            modifier
                .semantics { contentDescription = description }
                .onSizeChanged { size ->
                    canvasSize = SizeD(size.width.toDouble(), size.height.toDouble())
                }.chartGestures(
                    onTap = onNodeClick?.let { click -> { offset -> hitAt(offset)?.let(click) } },
                    onDoubleTap = null,
                    onTransform = null,
                    onLongPress = onNodeLongClick?.let { longClick -> { offset -> hitAt(offset)?.let(longClick) } },
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentPlan = plan ?: return@Canvas
            with(SunburstChartRenderer) {
                render(SunburstChartPlan(currentPlan.layout), textMeasurer, renderStyles)
            }
        }
    }
}

/** The 1.3 signature, kept so code compiled against it still links; new code resolves to the overload above. */
@Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
@Composable
fun SunburstChart(
    spec: SunburstChartSpec,
    modifier: Modifier = Modifier,
    chromeTheme: ChartChromeTheme? = null,
) {
    SunburstChart(spec = spec, modifier = modifier, chromeTheme = chromeTheme, rootNodeId = null)
}
