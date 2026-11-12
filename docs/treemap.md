# Treemap

`TreemapChart` draws values as area-proportional rectangles. It comes in two forms:

| | Flat | Hierarchical (1.4.0) |
|---|---|---|
| Spec | `TreemapChartSpec` (`TreemapNode` leaves) | `TreemapHierarchySpec` (`TreemapHierarchyNode` tree) |
| Levels | one | nested, down to `maxDepth` |
| Interaction | none | tap, long-press, drill-in, highlight |
| Accessibility | one chart description | one node per tile |

The flat form is unchanged since 1.2.0. Everything below is about the hierarchical form.

```kotlin
val spec = TreemapHierarchySpec(
    nodes = listOf(
        TreemapHierarchyNode("photos", "Photos", children = listOf(
            TreemapHierarchyNode("photos/2025", "2025", value = 8.1e9),
            TreemapHierarchyNode("photos/2026", "2026", value = 3.4e9),
        )),
        TreemapHierarchyNode("music", "Music", value = 2.2e9),
    ),
    valueFormatter = { node -> formatBytes(node.layoutSize()) }, // your formatter
    minTileSidePx = with(LocalDensity.current) { 48.dp.toPx() }.toDouble(),
)

val drill = rememberSaveableChartDrillState<String?>(null)
var highlighted by remember { mutableStateOf(emptySet<String>()) }

TreemapChart(
    spec = spec,
    modifier = Modifier.fillMaxWidth().height(320.dp),
    rootNodeId = drill.current,
    highlightedNodeIds = highlighted,
    onNodeClick = { hit -> if (hit.hasChildren) drill.drillDown(hit.nodeId) else highlighted = setOf(hit.nodeId) },
    onNodeLongClick = { hit -> showActions(hit) },
    animation = ChartAnimationPolicy(viewportChangesEnabled = true),
)
```

The showcase's **Interactive treemap** screen is a complete example: a storage-style folder tree with a breadcrumb,
drill-in, long-press details and a list kept in sync with the highlighted tile.

## Hierarchy and sizes

`TreemapHierarchyNode(id, label, value = 0.0, color = null, children = emptyList())`.

- **Ids** identify nodes everywhere: hit results, drill-in, highlighting, accessibility. Keep them unique across the
  tree and stable across data updates (a file path works well).
- **Size** (`layoutSize()`): a leaf's `value`. A parent's size is the larger of its own `value` and the sum of its
  children's sizes:
  - leave `value` at `0` and the parent is exactly the sum of its children;
  - give it a **larger** total and the difference is a **remainder**: area inside the parent that no child covers. Use
    it for, say, the bytes of files directly in a folder when you only list sub-folders. The remainder is not drawn as
    a tile, and a tap on it reports the parent.
  - Non-finite, zero and negative sizes are left out.
- **Levels:** `maxDepth` (default 2) is how many levels are drawn below the displayed root. Deeper nodes still count
  toward their ancestors' sizes, and `PlannedTreemapTile.hasChildren` tells you a tile can be drilled into.
- **Start from one root:** pass `listOf(root)` (the root is one tile containing its children) or `root.children`.

## Layout

Each level is laid out with the squarified algorithm (aspect ratios close to 1), the same one the flat treemap uses.
A parent's children fill its tile inside `nodePaddingPx`, below a header band of at least `headerHeightPx` that holds
the parent's label; the band grows to fit the measured header text. Within each parent, tile areas are proportional to
sizes. Tiles are separated by `tileGapPx` and rounded by `tileCornerRadiusPx`.

Colors: a node's own `color`, else top-level nodes take `palette` colors by index and descendants a progressively
lighter shade of their top-level ancestor. Colors stay the same when you drill in.

All sizes are **pixels**, like every spec in the library. Convert `dp` with `LocalDensity`.

`ChartPlanner.planTreemapHierarchy(input, spec, rootNodeId, highlightedNodeIds, labelMeasurer)` is the pure planner
behind the composable. Its `TreemapHierarchyPlan.tiles` lists every drawn tile (`PlannedTreemapTile`: id, path,
depth, bounds, value, label, value text, color, aggregate members) in pre-order, parents before their children.

## Minimum tile size and the aggregate tile

Real hierarchies have long tails; a folder with thousands of small files would otherwise produce tiles nobody can see
or touch. With `minTileSidePx` set, no drawn tile is narrower or shorter than that:

1. Within each parent, the smallest children that cannot be drawn at that size are merged into one **aggregate** tile.
   Its value is their sum, its label is `aggregateLabel(count)` (default "N smaller items"; localize it), its fill is
   `aggregateColor` (a neutral gray by default), and its id is the parent's id (or the spec's `id` at the top level)
   plus `#aggregate`. A single small child is drawn as itself instead.
2. If that tile is still too small, it is given more area, up to half the parent, at the expense of its siblings.
   Only if that fails is the next-smallest sibling merged into it. Values never change, so the drawn tiles' values
   always add up to their parent; the aggregate's area can be larger than its share.
3. A parent whose children would all end up in one aggregate draws no children. It shows as a plain tile you can
   drill into.

The aggregate is identifiable in the plan (`isAggregate`, `memberIds`) and in callbacks (`HierarchyNodeHit.isAggregate`,
`memberIds`). It is not a drill target, so show its members in your own UI. The layout is deterministic for a given
input and canvas size.

If your data layer also shapes the tree, let the library own the minimum size and have your code only cap the total
tile count.

## Labels

Each tile gets up to two lines: the node `label` and the `valueFormatter` text (the library has no byte or unit
formatter of its own). They are drawn only when they fit, as both lines, the name only, or nothing; long names are
shortened with `…`. Parents label their header band, with the value right-aligned when there is room. Labels are
clipped to their tile.

Text is black or white per tile, whichever contrasts more with the fill. That always reaches at least 4.5:1 against an
opaque fill, so it holds for every built-in palette color and shade. Set `labelColor` to override it. The planner
measures text through `TreemapLabelMeasurer`; the composable backs it with Compose text layout using
`MaterialTheme.typography.labelMedium` for names and headers and `labelSmall` for values.

## Tap and long-press

`onNodeClick` and `onNodeLongClick` receive a `HierarchyNodeHit` (shared with `SunburstChart`): `nodeId`, `path` (ids
from the top level down to the node), `label`, `value`, `valueText`, `depth`, `hasChildren`, `isAggregate`,
`memberIds`, and `bounds` for anchoring a menu.

- A touch reports the **deepest** tile under it. A parent's header band, its padding and the gaps between its children
  report the parent. The gaps between top-level tiles, and anything outside the tiling, report nothing.
  `TreemapHitResolver.resolve` / `resolveAtDepth` expose the same logic.
- Press feedback (ripple, haptics) is up to you.
- With neither callback set, the chart adds **no pointer input**, so an enclosing scroll container behaves exactly as
  without the chart.

The treemap uses callbacks rather than `ChartSelectionState`. Selection elsewhere in the library is an index plus a
data point and has no long-press, while treemap nodes are identified by id. The selection-like state here is
`highlightedNodeIds`, which you hoist.

## Drill-in

Pass `rootNodeId` to make any node's children fill the chart (an unknown id shows the top level). Keep the path in a
`ChartDrillState`: `drillDown(hit.nodeId)` on a tap with `hasChildren`, `drillUp()` from your back action, and
`path` for a breadcrumb. `TreemapHierarchySpec.findPath(id)` turns ids into labels.
`rememberSaveableChartDrillState(root)` keeps the path across configuration changes; `rootNodeId` is a plain string,
so you can also keep it in a view model.

With `ChartAnimationPolicy.viewportChangesEnabled`, drilling down zooms in from the tapped tile and drilling up zooms
out to it, over `viewportDurationMillis`. When the system asks for reduced motion (and `respectSystemReducedMotion` is
on, the default) the change is instantaneous.

## Highlight

`highlightedNodeIds` outlines those tiles, and an aggregate tile when it contains one of them, as set by
`TreemapHierarchySpec.highlight` (`HierarchyHighlightStyle`): `outlineColor` (black or white per tile by default),
`outlineWidthPx`, and optional `dimOthersAlpha` to fade every tile outside the highlighted subtrees. Ids that are not
drawn (another branch, below `maxDepth`, unknown) are ignored. The highlight is keyed by id, so it follows the node
through drill-in and resize. Pair it with a list: select a row to highlight the tile, and select the row of a tapped
tile.

## Accessibility

Every drawn tile is its own accessibility node. It reads as `"<label>, <value text>"`, is nested under its parent
(so a screen reader visits a folder and then its contents), reports its position among its siblings (largest first),
is marked selected when highlighted, offers a click action when `onNodeClick` is set, and offers a
`longClickActionLabel` custom action when `onNodeLongClick` is set. The chart node keeps a summary of the top level, or
`ChartAccessibilitySpec.contentDescription` when set.

The number of nodes is capped by `maxAccessibleTiles` (default 100). Past it only the top level is exposed and the
summary says how many tiles are left out. Offer those users the same data another way, such as the list you show next
to the treemap; see [`accessibility-custom-actions.md`](accessibility-custom-actions.md).

## Performance

Planning 500 nodes on two levels takes about 60 µs on a desktop JVM and about 1 ms on an Android emulator. On the
emulator, the first frame with tiles arrives 45–64 ms after composition when warm, and up to about 500 ms for the
first chart after app start. See [`performance-benchmarks.md`](performance-benchmarks.md) for the benchmarks, the
on-device measurement screen, and what is still unmeasured.

## Sunburst

`SunburstChart` takes the same `onNodeClick` / `onNodeLongClick` / `rootNodeId` / `highlightedNodeIds` parameters
(plus `highlight`). Its nodes are identified by `SunburstNode.id`, or the label when unset, and depth `0` is the center
disk. See [`api-and-state.md`](api-and-state.md).
