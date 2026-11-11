package com.mena97villalobos.composeplotlib.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawTransform
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.TreemapTileSemantics
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.resolveChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.internal.accessibility.treemapAccessibleTileCount
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ComposeTreemapLabelMeasurer
import com.mena97villalobos.composeplotlib.chart.internal.canvas.chartLabelTextStyles
import com.mena97villalobos.composeplotlib.chart.internal.gesture.chartGestures
import com.mena97villalobos.composeplotlib.chart.internal.layout.chartLayoutBaseInput
import com.mena97villalobos.composeplotlib.chart.internal.render.TreemapChartRenderer
import com.mena97villalobos.composeplotlib.chart.internal.render.TreemapHierarchyRenderer
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.chartChromeTheme
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.HierarchyNodeHit
import com.mena97villalobos.composeplotlib.core.selection.TreemapHitResolver
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
 * Hierarchical treemap: lays out the children of the displayed root as squarified tiles and draws each tile's own
 * children inside it (below a header band with the parent's label) down to [TreemapHierarchySpec.maxDepth] levels.
 * Tile areas are proportional to node sizes within each parent; tiles narrower than
 * [TreemapHierarchySpec.minTileSidePx] are merged into one aggregate tile per parent, and labels show the node name and
 * the [TreemapHierarchySpec.valueFormatter] text when they fit. A flat list of nodes is a one-level treemap; the flat
 * [TreemapChartSpec] overload keeps working unchanged.
 *
 * **Interaction** is reported through [onNodeClick] / [onNodeLongClick] with a [HierarchyNodeHit] (node id, ancestor
 * path, value, aggregate members, tile bounds). A touch resolves to the deepest tile under it, so a parent's header
 * band, padding and the gaps between its children report the parent, while the gaps between top-level tiles report
 * nothing (see [TreemapHitResolver]). Press feedback is left to the caller. With neither callback set the chart adds no
 * pointer input at all, so it never interferes with an enclosing scroll container.
 *
 * **Drill-in** is caller state: pass the node to show as [rootNodeId] (its children then fill the chart) and keep the
 * path in a [com.mena97villalobos.composeplotlib.chart.state.ChartDrillState] — `drillDown(hit.nodeId)` from
 * [onNodeClick] when [HierarchyNodeHit.hasChildren], `drillUp()` from a back action. Use
 * [com.mena97villalobos.composeplotlib.chart.state.rememberSaveableChartDrillState] to keep the path across
 * configuration changes. An unknown id shows the top level. When [ChartAnimationPolicy.viewportChangesEnabled] is on,
 * moving to a child zooms in from that child's tile and moving back up zooms out to it, over
 * [ChartAnimationPolicy.viewportDurationMillis]; with the system's reduced-motion setting (and
 * [ChartAnimationPolicy.respectSystemReducedMotion]) the change is instantaneous.
 *
 * **Highlight:** tiles whose id is in [highlightedNodeIds] — or aggregates containing one — are outlined per
 * [TreemapHierarchySpec.highlight] (optionally dimming the rest). Ids that are not drawn are ignored. The highlight is
 * keyed by id, so it follows the node through drill-in and resize.
 *
 * **Accessibility:** every drawn tile (up to [TreemapHierarchySpec.maxAccessibleTiles]) is its own accessibility node
 * with its label and value text, nested under its parent in traversal order and in size order within a level. With
 * callbacks set, a tile can be activated (click) and offers a [TreemapHierarchySpec.longClickActionLabel] custom
 * action for the long-press. Past the cap only the top level is exposed and the chart summary says how many tiles are
 * left out; offer the same data as a list for those users.
 *
 * @param spec the hierarchy and styling; see [TreemapHierarchySpec]
 * @param modifier layout modifier for the outer [Box] (give it an explicit height)
 * @param rootNodeId id of the node whose children fill the chart; `null` shows [TreemapHierarchySpec.nodes]
 * @param highlightedNodeIds ids of nodes to draw highlighted
 * @param onNodeClick called when a tile is tapped (or activated by an accessibility service)
 * @param onNodeLongClick called when a tile is long-pressed (or through its accessibility action)
 * @param animation drill transition policy; only [ChartAnimationPolicy.viewportChangesEnabled] and
 *   [ChartAnimationPolicy.viewportDurationMillis] apply
 * @param chromeTheme plot chrome; defaults to the Material-aware [chartChromeTheme]
 */
@Composable
fun TreemapChart(
    spec: TreemapHierarchySpec,
    modifier: Modifier = Modifier,
    rootNodeId: String? = null,
    highlightedNodeIds: Set<String> = emptySet(),
    onNodeClick: ((HierarchyNodeHit) -> Unit)? = null,
    onNodeLongClick: ((HierarchyNodeHit) -> Unit)? = null,
    animation: ChartAnimationPolicy = ChartAnimationPolicy.Default,
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
        remember(spec, rootNodeId, highlightedNodeIds, canvasSize, fontScale, labelMeasurer) {
            if (canvasSize == SizeD.Zero) {
                null
            } else {
                ChartPlanner.planTreemapHierarchy(
                    input = chartLayoutBaseInput(canvasSize, viewport, fontScale),
                    spec = spec,
                    rootNodeId = rootNodeId,
                    highlightedNodeIds = highlightedNodeIds,
                    labelMeasurer = labelMeasurer,
                )
            }
        }
    val latestPlan = rememberUpdatedState(plan)

    val resolvedAnimation = resolveChartAnimationPolicy(animation)
    val animateDrill = resolvedAnimation.viewportChangesEnabled && resolvedAnimation.viewportDurationMillis > 0
    val drill = rememberTreemapDrillTransition(plan, animateDrill, resolvedAnimation.viewportDurationMillis)

    val description = spec.accessibility.contentDescription ?: treemapHierarchyContentDescription(plan, spec)

    fun hitAt(offset: Offset): HierarchyNodeHit? {
        val current = latestPlan.value ?: return null
        val tile = TreemapHitResolver.resolve(OffsetD(offset.x.toDouble(), offset.y.toDouble()), current) ?: return null
        return TreemapHitResolver.hitFor(tile)
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
            val transition = drill.transition
            if (transition == null) {
                with(TreemapHierarchyRenderer) { render(currentPlan, textMeasurer, renderStyles) }
                return@Canvas
            }
            val t = drill.progress.value.toDouble()
            val plot = currentPlan.layout.plotBounds
            if (transition.zoomIn) {
                // The new level grows out of the tapped tile, over the previous level.
                with(TreemapHierarchyRenderer) { render(transition.previous, textMeasurer, renderStyles) }
                val target = lerp(transition.tile, plot, t)
                clipRect(target.left.toFloat(), target.top.toFloat(), target.right.toFloat(), target.bottom.toFloat()) {
                    withTransform({ mapRect(this, from = plot, to = target) }) {
                        with(TreemapHierarchyRenderer) { render(currentPlan, textMeasurer, renderStyles) }
                    }
                }
            } else {
                // Going up: start zoomed into the tile of the level we came from and pull back to the full view.
                val visible = lerp(transition.tile, plot, t)
                clipRect(plot.left.toFloat(), plot.top.toFloat(), plot.right.toFloat(), plot.bottom.toFloat()) {
                    withTransform({ mapRect(this, from = visible, to = plot) }) {
                        with(TreemapHierarchyRenderer) { render(currentPlan, textMeasurer, renderStyles) }
                    }
                }
            }
        }
        if (plan != null) {
            TreemapTileSemantics(
                plan = plan,
                spec = spec,
                highlightedNodeIds = highlightedNodeIds,
                onNodeClick = onNodeClick,
                onNodeLongClick = onNodeLongClick,
            )
        }
    }
}

/** A running drill transition: the [previous] plan and the [tile] the new level zooms out of (or back into). */
private class TreemapDrillTransition(
    val previous: TreemapHierarchyPlan,
    val tile: RectD,
    val zoomIn: Boolean,
)

private class TreemapDrillAnimation(
    val progress: Animatable<Float, *>,
) {
    var transition: TreemapDrillTransition? by mutableStateOf(null)
}

/**
 * Animates between drill levels: when the displayed root changes to a node that was a tile in the previous plan, the
 * new level zooms in from that tile; when it changes to an ancestor in whose plan the previous root is a tile, the
 * view zooms out to it. Any other change (or [enabled] false) switches instantly.
 */
@Composable
private fun rememberTreemapDrillTransition(
    plan: TreemapHierarchyPlan?,
    enabled: Boolean,
    durationMillis: Int,
): TreemapDrillAnimation {
    val animation = remember { TreemapDrillAnimation(Animatable(1f)) }
    val shown = remember { mutableStateOf<TreemapHierarchyPlan?>(null) }
    LaunchedEffect(plan) {
        val previous = shown.value
        shown.value = plan
        if (!enabled || previous == null || plan == null || previous.rootNodeId == plan.rootNodeId) {
            animation.transition = null
            return@LaunchedEffect
        }
        val into = previous.tiles.firstOrNull { it.nodeId == plan.rootNodeId }
        val outOf = plan.tiles.firstOrNull { it.nodeId == previous.rootNodeId }
        val transition =
            when {
                into != null -> TreemapDrillTransition(previous, into.bounds, zoomIn = true)
                outOf != null -> TreemapDrillTransition(previous, outOf.bounds, zoomIn = false)
                else -> null
            }
        animation.transition = transition
        if (transition != null) {
            try {
                animation.progress.snapTo(0f)
                animation.progress.animateTo(1f, tween(durationMillis))
            } finally {
                animation.transition = null
            }
        }
    }
    return animation
}

private fun lerp(
    from: RectD,
    to: RectD,
    t: Double,
): RectD =
    RectD(
        left = from.left + (to.left - from.left) * t,
        top = from.top + (to.top - from.top) * t,
        right = from.right + (to.right - from.right) * t,
        bottom = from.bottom + (to.bottom - from.bottom) * t,
    )

/** Transforms the canvas so that [from] is drawn onto [to]. */
private fun mapRect(
    transform: DrawTransform,
    from: RectD,
    to: RectD,
) {
    if (from.width <= 0.0 || from.height <= 0.0) return
    val sx = to.width / from.width
    val sy = to.height / from.height
    transform.translate((to.left - from.left * sx).toFloat(), (to.top - from.top * sy).toFloat())
    transform.scale(sx.toFloat(), sy.toFloat(), pivot = Offset.Zero)
}

/** The treemap planner ignores the viewport; any fixed unit window will do. */
private fun defaultTreemapHierarchyViewport() = ChartViewport(xRange = ClosedRangeD(0.0, 1.0), yRange = ClosedRangeD(0.0, 1.0))

/**
 * "Treemap. Tiles: Photos 12 GB, Music 3 GB." over the top-level tiles that are drawn, plus how many tiles have no
 * accessibility node of their own when there are more than [TreemapHierarchySpec.maxAccessibleTiles].
 */
private fun treemapHierarchyContentDescription(
    plan: TreemapHierarchyPlan?,
    spec: TreemapHierarchySpec,
): String {
    val top = plan?.tiles?.filter { it.depth == 1 }.orEmpty()
    if (top.isEmpty()) return if (spec.nodes.isEmpty()) "Treemap with no tiles." else "Treemap."
    val summary = "Treemap. Tiles: ${top.joinToString(", ") { tile -> "${tile.label} ${tile.valueText ?: tile.value.toString()}" }}."
    val exposed = treemapAccessibleTileCount(plan!!, spec.maxAccessibleTiles)
    val hidden = plan.tiles.size - exposed
    return if (hidden > 0) "$summary $hidden smaller tiles are not listed individually." else summary
}
