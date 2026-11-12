# Accessibility custom actions

Charts expose optional TalkBack / VoiceOver **custom actions** on the chart container via `accessibilityCustomActions: List<ChartCustomAccessibilityAction>` (label + callback).

## Viewport shortcuts helper

[`chartViewportAccessibilityActions`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartCustomAccessibilityAction.kt) builds **Reset chart viewport**, and when zoom is allowed, **Zoom in** and **Zoom out** at the current window center, respecting axis locks via `ChartViewportState.axisPolicy`.

## Merging with app actions

Use [`chartAccessibilityActionsMergeViewport`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartCustomAccessibilityAction.kt) to place viewport actions **first**, then append product-specific actions (export, clear selection only, drill-down, etc.):

```kotlin
LineChart(
    spec = spec,
    viewportState = viewportState,
    accessibilityCustomActions = chartAccessibilityActionsMergeViewport(
        viewportState = viewportState,
        gestureConfig = ChartGestureConfig.cartesianInteractive(),
        appActions = listOf(
            ChartCustomAccessibilityAction("Clear selection only") {
                selectionState.clear()
            },
        ),
    ),
)
```

**Ordering:** viewport reset/zoom appear before app actions so users hear stable shortcuts first. Duplicate labels are allowed but avoid confusing duplicates (e.g. two “Reset” actions).

**Idempotency:** callbacks may run multiple times if the user repeats the action; keep them safe to invoke repeatedly.

## Hierarchical treemap tiles

The hierarchical `TreemapChart` (`TreemapHierarchySpec`) exposes **one accessibility node per drawn tile**, laid over
the canvas at the tile's bounds:

- **Content:** `"<label>, <value text>"`, where the value text comes from `TreemapHierarchySpec.valueFormatter` (the raw
  value without one). Aggregate tiles read as their aggregate label (for example "42 smaller items") and value.
- **Structure:** a parent's children are nested under the parent's node (a traversal group), so TalkBack and VoiceOver
  read a folder and then its contents. Within a level, tiles are visited largest first and report their position
  (collection item info).
- **Actions:** with `onNodeClick` set, each tile is a button whose click action calls it; with `onNodeLongClick` set,
  each tile offers a custom action labeled `TreemapHierarchySpec.longClickActionLabel` (default "More options") that
  performs the long-press. Highlighted tiles are marked selected.
- **Chart summary:** the chart node keeps a generated summary of the top-level tiles, or
  `ChartAccessibilitySpec.contentDescription` when set.

**Limitation — large charts.** Every tile node is a real layout node, so they are capped by
`TreemapHierarchySpec.maxAccessibleTiles` (default 100). Past the cap only the top-level tiles are exposed (at most
that many, largest first) and the chart summary ends with "N smaller tiles are not listed individually." For those
users, offer the same data another way — the list the app already shows next to the treemap, or the library's
`ChartDataTable` — and keep it in sync with the drill level. Merging small tiles with `minTileSidePx` also keeps the
tile count, and therefore the node count, down.

## Related

- [api-and-state.md](api-and-state.md) — `ChartAccessibilitySpec`, keyboard roam, semantics
- [viewport-configuration.md](viewport-configuration.md) — keyboard pan/zoom vs Shift+arrow selection
