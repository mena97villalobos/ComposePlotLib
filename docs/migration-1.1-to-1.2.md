# Migrating from 1.1.0 to 1.2.0

`1.2.0` is a large, **mostly additive** release: 19 chart families (up from 13), new interaction,
export, theming, and accessibility surfaces, plus a Sankey rework. Almost all existing code compiles
and runs unchanged. This guide covers the few things to check when you upgrade.

- **New features:** see [`release-notes-1.2.0.md`](release-notes-1.2.0.md).
- **Full change list:** see [`../CHANGELOG.md`](../CHANGELOG.md) → `1.2.0`.

---

## TL;DR

| If you… | Action |
|---|---|
| use the standard chart composables and specs by named arguments | **Nothing** — recompile and go. |
| ship a **precompiled** library that constructs `ChartPoint` / `BarEntry` / `SankeyLayoutSpec` / `SankeyNodeLabelsSpec` positionally or via `copy(...)` | **Recompile** against 1.2.0 (trailing constructor params were appended — source-compatible, not binary-compatible). |
| call **`ComposePlotApp()`** or depend on the `composeApp` / `iosApp` demo modules | **Removed.** Use `samples/android-showcase`; embed on iOS per [`ios-library-usage.md`](ios-library-usage.md). |
| render **Sankey** charts | The **default look changed** and a **vertical-orientation bug was fixed** (charts may look remapped). Opt back into the old look if needed — see below. |

There are **no removed or renamed** public chart composables, specs, or state types other than
`ComposePlotApp()`.

---

## 1. Recompile-only (binary, not source, changes)

These are **source-compatible** — your Kotlin still compiles unchanged — but the class/constructor
signatures changed, so a **precompiled** dependency that was built against 1.1.0 must be **recompiled**
against 1.2.0.

### `ChartPoint` and `BarEntry` gained trailing parameters

`ChartPoint` and `BarEntry` each gained trailing defaulted `yLow` / `yHigh` bounds (for error bars).
Named or short positional construction is unaffected:

```kotlin
ChartPoint(x = 1.0, y = 2.0)            // ✅ unchanged
BarEntry(x = 0.0, y = 10.0)             // ✅ unchanged
```

Only fully-positional construction that assumed the old arity, or `.copy(...)` in precompiled code,
needs a recompile.

### Sankey spec constructors gained trailing parameters

`SankeyLayoutSpec` and `SankeyNodeLabelsSpec` gained the new orientation / palette / label options as
trailing defaulted parameters, and `CandlestickChartSpec` gained a trailing `variant`. Same rule:
source-compatible, recompile precompiled callers.

`PlannedSankeyLink` gained a **required** `pathSegments` property — but plan output types are planner
results you read, not values you construct, so this only affects code that hand-built plan objects
(uncommon).

---

## 2. `ComposePlotApp()` and the demo modules were removed

The single-composable preview cycler `ComposePlotApp()` (a `@Composable @Preview` that stepped through
the families) is gone from `chart-compose`, along with the `composeApp` (Android + iOS host) and
`iosApp` (Xcode) demo modules. This is **source-breaking only for callers of `ComposePlotApp()`** — no
chart API changed.

- To preview families, use **`samples/android-showcase`** (the sole sample app).
- To embed the charts in an iOS host, see [`ios-library-usage.md`](ios-library-usage.md). The library
  still targets iOS (compile-gated in CI); there is simply no bundled iOS demo app.

---

## 3. Sankey behavior changed

If you render `SankeyChart`, review these. All are improvements, but they change pixels.

### 3a. Default visual style changed

Nodes now use the Tableau-10 palette (was uniform slate), ribbons use source→target gradients (was flat
50%-alpha slate), labels sit **beside** nodes (was rotated inside), node corners are square
(`nodeCornerRadius` `0.02` → `0.0`), and the default link-label formatter renders `9.0` as `9`.

To restore the 1.1 look:

```kotlin
SankeyChart(
    spec = spec.copy(
        layout = spec.layout.copy(
            nodePalette = emptyList(),                       // monochrome (defaultNodeColor)
            linkColorMode = SankeyLinkColorMode.Static,      // flat ribbons
            nodeCornerRadius = 0.02,
        ),
        nodeLabels = SankeyNodeLabelsSpec.DefaultInside,     // rotated-inside labels
    ),
)
```

> `DefaultInside` keeps white label text — pair it with the monochrome palette or set
> `nodeLabels.color` so labels stay legible.

### 3b. Vertical orientation was mirrored (bug fix)

Node stacks were previously laid out against the domain y-axis flip, so `Top`/`Bottom` alignment
anchored at the wrong edge, `SankeyNodeOrder.Input` placed the first node at the visual bottom, and
`TargetTopToBottom` / `SourceTopToBottom` link stacking sorted the wrong way. These now mean what they
say on screen. **Charts that were tuned against the old inverted rendering will look vertically
mirrored after upgrading** — re-check your `columnAlignment` / node-order / link-order choices.

Also fixed (no action needed, just better output): column-height overflow that could drop the last
node in a busy column, link-stacking saturation, negative/offset column indices, and Bézier-accurate
hit-testing.

---

## 4. New capabilities you may want to adopt

Nothing here is required, but these commonly replace app-side workarounds:

- **`ChartColor.rgb/argb/hex/hsl` + `withAlpha`** — drop any hand-rolled `Long` ARGB math.
- **Interactive legend** — `hiddenSeriesIds` + `onLegendItemClick` instead of filtering series yourself.
- **Data labels / annotations / crosshair / trendlines / error bars** — first-class specs on line/scatter/bar.
- **Export** — `capturePng()` (raster), `chartLayoutPlanToSvg` / `lineChartToSvg` (vector), `ChartDataTable.toCsv/toJson` (data).
- **Interaction** — `SyncChartViewports` (linked pan/zoom), `ChartRangeBrush` (box selection), `ChartRangeSelector` (minimap).
- **Accessibility** — `ChartDataTable(...)` "view data" surface and `ChartAccessibilitySpec.includeDataNarrative`.

See [`release-notes-1.2.0.md`](release-notes-1.2.0.md) for the full list with entry points.

---

## Verifying your upgrade

```bash
./gradlew ktlintCheck :chart-core:apiCheck :chart-compose:apiCheck \
  :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest \
  :samples:android-showcase:verifyRoborazziDebug :samples:android-showcase:assembleDebug
```

If you keep golden screenshots of Sankey charts, **re-record them** after upgrading (§3).
