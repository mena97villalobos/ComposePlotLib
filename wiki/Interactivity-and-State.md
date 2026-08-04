# Interactivity and State

ComposePlotLib has no mutable chart object. Everything a user can change — the visible window, the
highlighted value, the drill path — lives in **hoisted Compose state** that you own, and specs stay
immutable. Two objects cover almost everything: `ChartViewportState` (visible window, domain,
constraints, axis policy) and `ChartSelectionState` (the currently highlighted value). Both are
optional parameters on every interactive chart — omit them and the composable remembers its own
instance; hoist them when you need to read, drive, or share the state.

## Viewport state

`defaultViewport(spec)` computes the data extent for a spec, so the usual starting point is:

```kotlin
val viewportState = rememberChartViewportState(
    initialViewport = defaultViewport(spec),   // starting window
    domain = defaultViewport(spec),            // hard clamp for pan/zoom
)

LineChart(spec = spec, viewportState = viewportState)
```

`domain` may be wider than `initialViewport` so a chart can start zoomed in. At runtime the state
exposes `viewport`, `domain`, `constraints`, `axisPolicy`, and `lastViewportChangeOrigin`
(`ViewportChangeOrigin.Gesture` or `.Programmatic`), plus `pan`, `zoom`, `panTo`, `centerOn`,
`zoomTo`, `fitToData`, and `reset()`. `CombinedCartesianChart` adds an independent right-axis channel
via `secondaryYViewport` / `updateSecondaryYViewport` / `panSecondaryY` / `zoomSecondaryY` /
`resetSecondaryY`.

## Constraints and axis lock

`ViewportConstraints` caps the visible **span** per axis (`minXSpan`, `maxXSpan`, `minYSpan`,
`maxYSpan`); `ViewportAxisPolicy` pins an axis back to the domain after every gesture:

```kotlin
val viewportState = rememberChartViewportState(
    initialViewport = defaultViewport(spec),
    constraints = ViewportConstraints(minXSpan = 0.5, maxXSpan = 50.0),
    axisPolicy = ViewportAxisPolicy.HorizontalExploration, // Y always shows the full domain
)
```

Presets are `ViewportAxisPolicy.Free` (default), `.HorizontalExploration`, and
`.VerticalExploration`. A locked axis is also a **gesture gate** — drag and pinch deltas on that axis
are discarded, so you do not need to also switch them off in the gesture config. Change either at
runtime with `viewportState.updateConstraints(...)` / `updateAxisPolicy(...)`.

Full reference: [viewport-configuration.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/viewport-configuration.md).

## Gestures

`ChartGestureConfig` bundles three policies — `tap` (`ChartTapGesturePolicy`), `transform`
(`ChartTransformGesturePolicy`: pan/pinch per axis, double-tap zoom in/out, double-tap reset), and
`fling` (`ChartFlingGesturePolicy`). Prefer the companion presets over spelling out every nested flag:

| Preset | Behavior |
|---|---|
| `ChartGestureConfig.CartesianDefault` | tap select + pan/pinch; no double-tap, no fling |
| `ChartGestureConfig.PieDefault` | tap select only; transform and fling off |
| `ChartGestureConfig.readOnly()` | nothing interactive |
| `ChartGestureConfig.exploreOnly(transform, fling)` | pan/zoom without tap selection |
| `ChartGestureConfig.cartesianInteractive(...)` | tap + pan/pinch + double-tap zoom-in (flags for zoom-out, reset, fling) |
| `ChartGestureConfig.cartesianInteractiveFullExploration()` | everything on — for device QA |

When several double-tap modes are enabled the resolution order is reset, then zoom out, then zoom in.

## Selection

Tap selection is on by default on every family whose composable takes a `selectionState` parameter
(see the list in [Chart Gallery](Chart-Gallery)). Hoist the state to read it:

```kotlin
val selectionState = rememberChartSelectionState()

LineChart(spec = spec, selectionState = selectionState)

val target = selectionState.selection?.target
Text(target?.let { "${it.seriesId} @ ${it.point.x}, ${it.point.y}" } ?: "Nothing selected")
```

`ChartSelection` carries the logical `target` (`HighlightTarget`: `seriesId`, `pointIndex`, `point`,
and `layerId` on combined charts) plus pixel geometry (`pixelOffset`, `markerAnchor`, `hitBounds`).
You can also drive it yourself with `select(...)` / `clear()`. Line and scatter resolve the nearest
point in pixel space; bars hit-test the drawn rectangles; pie/radar/sankey/heatmap have their own
resolvers. `LineChart` additionally accepts `hoverEnabled = true` for mouse/stylus hover-select
(a no-op on touch).

## Markers and tooltips

Pass a `marker` composable and it is positioned near the selection anchor, flipping and sliding to
stay inside the containment region:

```kotlin
LineChart(
    spec = spec,
    selectionState = selectionState,
    marker = { data -> DefaultChartMarker(data, showOutline = true) },
    markerPlacement = ChartMarkerPlacement(
        verticalGap = 12.dp,
        containment = ChartMarkerContainment.PlotBounds,
        preferAbove = true,
        callout = ChartMarkerCalloutLeader.DefaultElbow,
    ),
)
```

`ChartMarkerData` hands you the selection plus preformatted `seriesLabel` / `xLabel` / `yLabel` /
`valueLabel` strings. Containment is `PlotBounds` (default), `CanvasBounds`, or `None`. For a
non-elbow leader, `ChartMarkerCalloutLeader.custom { anchorX, anchorY, left, top, w, h -> … }`
returns your own polyline in chart pixels (see
[marker-tooltip-placement.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/marker-tooltip-placement.md)).

## Linked charts

`SyncChartViewports(vararg states, axis)` mirrors one axis window across a group of viewport states —
the "shared time axis" dashboard pattern. Each follower still clamps to its own domain.

```kotlin
val price = rememberChartViewportState(initialViewport = defaultViewport(priceSpec))
val volume = rememberChartViewportState(initialViewport = defaultViewport(volumeSpec))
val shared = rememberChartSelectionState()

SyncChartViewports(price, volume, axis = ChartSyncAxis.X)
LineChart(priceSpec, viewportState = price, selectionState = shared)
BarChart(volumeSpec, viewportState = volume, selectionState = shared)
```

Pass the **same** `ChartSelectionState` to link highlighting/tooltips as well. `ChartSyncAxis` is
`X` (default), `Y`, or `Both`.

## Drill-down

`rememberChartDrillState(root)` is a typed back-stack: `drillDown(level)`, `drillUp()`, `reset()`,
with `current`, `path`, `depth`, and `canDrillUp`. The library owns the navigation; you decide what a
level is and build each level's spec from `current`.

```kotlin
val drill = rememberChartDrillState(root = "all-regions")

BarChart(spec = specFor(drill.current), selectionState = selectionState)
LaunchedEffect(selectionState.selection) {
    selectionState.selection?.target?.seriesId?.let(drill::drillDown)
}
if (drill.canDrillUp) TextButton(onClick = drill::drillUp) { Text("Back") }
```

## Range brushing

`LineChart` accepts an opt-in `rangeBrush: ChartRangeBrush`. While attached it takes over the drag
gesture (pan/zoom/tap are suspended) and reports the dragged region as a `ChartRangeSelection`
(`xRange` / `yRange`), or `null` when the drag was shorter than `minDragPx`. Pair it with `zoomTo`
for zoom-to-selection:

```kotlin
LineChart(
    spec = spec,
    viewportState = viewportState,
    rangeBrush = ChartRangeBrush(axis = ChartBrushAxis.Horizontal) { range ->
        val x = range?.xRange ?: return@ChartRangeBrush
        val y = viewportState.viewport.yRange
        viewportState.zoomTo(x.span, y.span, x.midpoint, y.midpoint)
    },
)
```

`ChartBrushAxis` is `Horizontal` (default), `Vertical`, or `Both`; `fill`, `stroke`,
`strokeWidthPx`, and `minDragPx` style and gate the drag rectangle.

## Accessibility

Keyboard and screen-reader behavior is opt-in through `ChartAccessibilitySpec` on the spec:
`contentDescription`, `exposeSelectionState`, `requestFocusForAccessibility`,
`keyboardViewportNudgeEnabled` (arrow-key pan, `+`/`-` zoom, Escape clears selection),
`keyboardSelectionRoamEnabled` (Shift+arrows walk points/series), `requestFocusOnMarker`,
`selectionLiveRegionMode`, and `includeDataNarrative`.

Charts also take `accessibilityCustomActions`. Two helpers build the viewport shortcuts for you:

```kotlin
LineChart(
    spec = spec,
    viewportState = viewportState,
    accessibilityCustomActions = chartAccessibilityActionsMergeViewport(
        viewportState = viewportState,
        gestureConfig = ChartGestureConfig.cartesianInteractive(),
        appActions = listOf(
            ChartCustomAccessibilityAction("Clear selection") { selectionState.clear() },
        ),
    ),
)
```

`chartViewportAccessibilityActions(viewportState, gestureConfig)` alone yields "Reset chart viewport"
plus "Zoom in" / "Zoom out" when zooming is allowed (it respects axis locks and returns an empty list
when transform gestures are off); `chartAccessibilityActionsMergeViewport` puts those first, then
your app actions. Callbacks may fire repeatedly — keep them idempotent. See
[accessibility-custom-actions.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/accessibility-custom-actions.md).

## Motion and reduced motion

`ChartAnimationPolicy` starts with motion off — `viewportChangesEnabled`,
`gestureViewportEasingEnabled`, `selectionHighlightEnabled`, and `dataTransitionEnabled` are all
`false` in `ChartAnimationPolicy.Default`. Opt in per chart:

```kotlin
LineChart(
    spec = spec,
    animation = ChartAnimationPolicy(
        viewportChangesEnabled = true,     // ease programmatic panTo/zoomTo/reset
        selectionHighlightEnabled = true,  // fade the marker in and out
    ),
)
```

`respectSystemReducedMotion` defaults to **true**: when the platform reports a reduced-motion
preference, viewport easing, gesture chase, line data morph, and marker fades are disabled and
transition durations forced to zero. Leave it on.

## Next

- [Core Concepts](Core-Concepts) — the spec → planner → draw-command pipeline
- [Chart Gallery](Chart-Gallery) — which of the 26 families support selection and pan/zoom
- [api-and-state.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md) — the exhaustive API reference
