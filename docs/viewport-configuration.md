# Viewport configuration

This guide covers zoom span limits (**constraints**), **axis lock** (horizontal/vertical exploration), and how those interact with **gestures** in `chart-compose`.

Related backlog notes: [`roadmap.md`](roadmap.md) (Viewport model, Gesture system).

## Types (chart-core)

- **[ViewportConstraints](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/viewport/ViewportConstraints.kt)** — `minXSpan` / `maxXSpan` / `minYSpan` / `maxYSpan` on the visible window. Applied inside [ViewportState.zoom](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/viewport/ViewportState.kt) and after programmatic viewport updates that go through `ViewportState`.
- **Numeric hygiene:** `ViewportState` re-canonicalizes the **domain** (finite, ordered axes; non-finite endpoints fall back to a unit interval) and sanitizes the current and incoming viewport against that domain before clamping. Fully degenerate domains (point axes) stay stable; extreme pinch scales are capped by constraints + domain as before, with safer pivot handling when midpoints are non-finite.
- **[ViewportAxisPolicy](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/viewport/ViewportAxisPolicy.kt)** — After each pan/zoom, one or both axes can be reset to the chart **domain** on that axis:
  - `Free` — both axes follow gestures (default).
  - `HorizontalExploration` — `lockYToDomain = true`: Y always shows the full domain; only X pans/zooms (typical time-series strip).
  - `VerticalExploration` — `lockXToDomain = true`: X always shows the full domain; only Y pans/zooms.

## Compose wiring

- Create state with [rememberChartViewportState](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/state/RememberChartStates.kt):
  - `initialViewport` — starting visible window (often equals `domain` or a zoomed subset).
  - `domain` — maximum range pan/zoom is clamped to (e.g. full data extent). May be wider than `initialViewport` so the chart can start zoomed in.
  - `constraints` — optional `ViewportConstraints`.
  - `axisPolicy` — optional `ViewportAxisPolicy` preset or custom instance.
- At runtime:
  - [ChartViewportState.updateConstraints](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/state/ChartViewportState.kt) — change span limits and re-clamp.
  - [ChartViewportState.updateAxisPolicy](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/state/ChartViewportState.kt) — change axis lock and re-clamp.
- **[ChartViewportState.lastViewportChangeOrigin](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/state/ChartViewportState.kt)** ([ViewportChangeOrigin](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/state/ViewportChangeOrigin.kt)) — `Gesture` after `pan` / `zoom` (and fling); `Programmatic` after `panTo`, `zoomTo`, `fitToData`, `reset`, `updateConstraints`, `updateAxisPolicy`. Chart composables combine this with [ChartAnimationPolicy](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartAnimationPolicy.kt) to ease the **rendered** viewport for programmatic jumps when **`viewportChangesEnabled`** is true, and optionally **chase** gesture targets when **`gestureViewportEasingEnabled`** is true (otherwise gesture updates snap each frame).

## Gestures vs axis lock

[ChartGestureConfig](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartGestureConfig.kt) controls whether pan/zoom are enabled per axis. Use **`ChartGestureConfig.CartesianDefault`**, **`cartesianInteractive()`**, **`cartesianInteractiveFullExploration()`**, **`exploreOnly()`**, **`readOnly()`**, or **`PieDefault`** when you want documented bundles instead of spelling every nested policy (see **`docs/ios-gesture-validation.md`**). [effectiveTransformFlags](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/internal/gesture/EffectiveTransformFlags.kt) combines gesture config with `ViewportAxisPolicy`:

- If **`lockXToDomain`** is true, **pan and zoom on X are ignored** (X is fixed to the domain after each update).
- If **`lockYToDomain`** is true, **pan and zoom on Y are ignored**.

So axis lock is both a **semantic** choice (what the user sees) and a **gesture** gate (locked axes do not receive drag/pinch deltas).

## Example

```kotlin
val viewportState = rememberChartViewportState(
    initialViewport = defaultViewport(spec),
    domain = defaultViewport(spec),
    constraints = ViewportConstraints(minXSpan = 0.5, maxXSpan = 50.0),
    axisPolicy = ViewportAxisPolicy.HorizontalExploration,
)
LineChart(
    spec = spec,
    viewportState = viewportState,
    gestureConfig = ChartGestureConfig(), // still respects axis lock via effectiveTransformFlags
)
```

The same `rememberChartViewportState` + `defaultViewport(spec)` pattern applies to every cartesian chart (**`BarChart`**, **`ScatterChart`**, **`CandlestickChart`**, **`CombinedCartesianChart`**); only the spec type and composable change. `defaultViewport(spec)` is also defined for the remaining spec types (`PieChartSpec`, `RadarChartSpec`, `GaugeChartSpec`, `HeatmapChartSpec`).

Adjust `constraints` or `axisPolicy` when the spec or data bounds change via `updateConstraints` / `updateAxisPolicy` on the same `ChartViewportState` instance.

## Keyboard (cartesian viewport vs selection roam)

When `ChartAccessibilitySpec.keyboardViewportNudgeEnabled` is true on a **cartesian** chart, a **focused** chart handles **unmodified** **arrow** keys for pan, **+ / −** for zoom at the visible center, and **Escape** to clear tap selection (when tap selection is enabled). When **`keyboardSelectionRoamEnabled`** is also true and tap selection is enabled, **Shift+arrow** keys move the logical selection: **Shift+Left/Right** steps along points (line/scatter), bars in left-to-right order (bar), a combined flat walk (combined), candles within a series in domain order (**candlestick**, via `KeyboardSelectionRoam.candlestickChartRoam`), or slices (pie); **Shift+Up/Down** switches series on **line**, **scatter**, and **candlestick**. The viewport handler **does not** consume Shift+direction keys so they are reserved for roam. See [api-and-state.md](api-and-state.md) §Accessibility.
