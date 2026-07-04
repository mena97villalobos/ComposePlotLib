# ComposePlotLib Roadmap

The **single source of truth for open, forward-looking work**. It consolidates what used to live across
several planning docs into one place: where the library is today, the constraints any new work must respect,
and a prioritized backlog of features and hardening — informed by a survey of competing charting libraries
(MPAndroidChart, Vico, YCharts, Koala Plot, ComposeCharts, Apache ECharts, Chart.js, Recharts, D3/Observable
Plot, Plotly, Highcharts, Nivo).

- **Shipped surface** (families, specs, state, gestures, theming, a11y): [`api-and-state.md`](api-and-state.md),
  [`chart-families-overview.md`](chart-families-overview.md).
- **How the pipeline fits together**: [`architecture.md`](architecture.md), [`module-guide.md`](module-guide.md).
- **Contributor rules & build commands**: [`../AGENTS.md`](../AGENTS.md).

Priorities use **P0** (do first) … **P3** (opportunistic). Effort is a rough **S / M / L / XL**. "Fit" is how
cleanly a feature sits in this library's architecture (strong / moderate / weak). A ✅ row is shipped; a
**⏸️** row is an explicit **deferred / won't-do** decision (rationale in its note) rather than open work.

---

## 1. Current state (as of 2026-07, `1.2.0` unreleased)

**Chart families (20):** `LineChart`, `BarChart`, `StackedBarChart`, `ScatterChart`,
`CombinedCartesianChart` (ordered line/bar/scatter layers, shared or secondary Y), `PieChart`/donut,
`RadarChart`, `SankeyChart` (orientation, Tableau-10 palette, gradient ribbons — reworked for 1.2.0),
`HeatmapChart`, `GaugeChart`, `CandlestickChart`, `BoxPlotChart` (Tukey five-number summaries), `WaterfallChart`
(running-total bars), `BubbleChart` (area-proportional sizing), `BulletChart` (linear KPI), `FunnelChart` (conversion funnel), `StackedAreaChart` (absolute/percent/streamgraph), `RadialBarChart` (concentric progress rings), `CalendarHeatmapChart` (year/contribution grid), plus the axis-less
`Sparkline` wrapper.

**Cartesian depth:** area / threshold-band fill (`LineStyle.areaFill` + `AreaFillMode`), reference/threshold
lines, marker shapes with per-point overrides, per-point bubble sizing on scatter, clustered/stacked/horizontal
bars, dual (secondary, data-derived) Y on combined charts, error bars (`ErrorBarsSpec` on line/scatter/vertical
bars), and fitted trendline overlays (`TrendlineSpec`: linear + moving-average, optional confidence band).

**Axes & scales:** `Linear`, `Category`, `TimeEpochMillis` (UTC / fixed-offset tick policies), `Log` / `SymLog`,
reversed axis direction (`AxisSpec.inverted`), tick-density and X-label overlap strategies. **UTC-only time.**

**Interaction:** tap-select (per-family hit resolvers), pan, per-axis pinch zoom, double-tap (in / out /
reset), fling, keyboard viewport nudge and selection roam, programmatic `ChartViewportState`, custom marker
slot, gesture presets, interactive legend toggle, crosshair / axis-pointer (`CrosshairSpec`, line/scatter),
cross-chart viewport sync (`SyncChartViewports`, `syncId`-style linked pan/zoom), range brush / box selection
(`ChartRangeBrush` on `LineChart`), and a range selector / minimap navigator (`ChartRangeSelector`). **No hover yet.**

**Theming:** `ChartColor` (named presets + `rgb`/`argb`/`hex`/`hsl`/`withAlpha` factories), a Material 3 palette
bridge, per-element style classes, `ChartChromeTheme`, Heatmap/Sankey color scales.

**Accessibility:** Image role + focus, keyboard viewport/selection roam (most families), live region,
custom actions, reduced-motion, data-table "view data" surface (`ChartDataTable`). **No chart-level narrative
summary yet.**

**Analytics:** financial technical indicators (`TechnicalIndicators`: SMA / EMA / RSI / MACD / Bollinger), fitted trendlines, and the accessible data-table / narrative surfaces.

**Performance:** viewport windowing → pixel clip, Uniform / MinMaxY decimation, plot-width pixel budget,
tick-value reuse, grid-line cache.

**Platforms:** Kotlin Multiplatform — `androidTarget`, `iosArm64`, `iosSimulatorArm64` for both `chart-core`
and `chart-compose`; JVM target on `chart-core` for tests/tooling. iOS is supported **as a library only**
(no in-repo demo app); the Android `samples/android-showcase` is the sole runnable sample. CI compiles the
iOS targets on macOS and publishes Android + JVM + iOS + KMP-metadata artifacts.

For the exact public types, see [`api-and-state.md`](api-and-state.md).

---

## 2. Design constraints (any new work must honor these)

These are durable architecture rules, not backlog items. They gate how — not whether — a feature lands.

- **`chart-core` has no Compose/UI types.** No `Modifier`, `Color`, `Dp`, `Offset`, `Rect`, `Path`, `Canvas`,
  `TextStyle`. Math, geometry, specs, planning, and `DrawCommand` output only.
- **Specs are immutable.** New configuration is new spec/model types or fields with defaults, never mutable
  setters. State (pan/zoom/selection) is explicit in `ChartViewportState` / `ChartSelectionState`.
- **Extend the planner first.** Layout/semantic changes add to `chart-core` planning + the `DrawCommand` set
  before touching the renderer. Avoid ad-hoc canvas drawing in `chart-compose`.
- **Every line is KMP-safe.** No `java.*`, no `String.format`, no Android/JVM-only APIs in `commonMain`;
  platform behavior goes through `expect`/`actual`.
- **Adding a chart family** ⇒ planner + spec in `chart-core`, composable in `chart-compose`, a showcase screen
  wired into `ShowcaseApp` with a Roborazzi golden, and doc updates (`api-and-state.md`,
  `chart-families-overview.md`, `architecture.md`, this file). See [`../AGENTS.md`](../AGENTS.md) → Obligations.

---

## 3. Near-term priorities (P0 → early P1)

The highest-leverage gaps versus nearly every competitor, each a strong architectural fit and mostly
`chart-core` math:

1. ~~**Logarithmic (+ symlog) axis scale**~~ — ✅ **shipped:** `AxisScale.Log(base)` / `AxisScale.SymLog(base, linearThreshold)`
   with a scale-aware `ChartTransform` (evenly-spaced decades) + decade/minor tick generation (line & scatter). *(M, strong)*
2. ~~**Null / missing-data gap handling** (`connectNulls` / span-gaps)~~ — ✅ **shipped:** `ChartPoint.gap(x)`
   marks a missing sample (non-finite y); line/area break at gaps, or `LineRenderSpec.connectAcrossGaps` bridges. *(S, strong)*
3. ~~**Per-point / per-bar data labels**~~ — ✅ **shipped:** `DataLabelsSpec` on line/scatter/bar specs
   (formatter, placement, greedy overlap avoidance). *(M, strong)*
4. ~~**Interactive legend**~~ — ✅ **shipped:** `hiddenSeriesIds` on line/scatter/bar specs (hidden series dropped +
   dimmed legend row) + `onLegendItemClick` tap-to-toggle on the composables. *(M, strong)*
5. ~~**Annotations layer**~~ — ✅ **shipped:** `ChartAnnotation` (Point / Text / Box-region / Segment) anchored in
   data space, on line/scatter/bar specs (`annotations`). *(L, strong)*
6. ~~**`ChartColor` ergonomics**~~ — ✅ **shipped:** `ChartColor.rgb()` / `argb()` / `hex()` / `hsl()` factories,
   `withAlpha(fraction)`, and `alpha`/`red`/`green`/`blue` channel accessors. *(S, strong)*
7. ~~**Crosshair / axis-pointer**~~ — ✅ **shipped:** `CrosshairSpec` on line/scatter draws vertical + horizontal
   guidelines through the selection plus axis-value chips at the plot edges. *(M, strong)*

---

## 4. Feature backlog by theme

Merged from the competitive gap analysis and the residual open items in the retired planning docs. Priorities
reflect fit + demand; sizes are rough. "Also in" names representative libraries that ship the capability.

### 4.1 Cartesian scales & axes

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Logarithmic + symlog scale~~ ✅ | ~~P0~~ | M | strong | **Shipped:** `AxisScale.Log` / `AxisScale.SymLog` + scale-aware `ChartTransform`. |
| ~~Inverted / reversed axis direction~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `AxisSpec.inverted` — reverses the axis transform (rendering on all cartesian families; pan/zoom on line/scatter/bar). |
| ~~Normalization / percent (100%) stacking for bars **and** areas~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `BarChartSpec.stackMode` (`BarStackMode.PercentOfTotal`) + `StackedAreaChartSpec.offset` (`AreaStackOffset.Expand`). |
| More than two Y axes / N stacked axes with per-axis layer binding | P3 | M | moderate | Generalizes the combined secondary-Y seam. |
| Independent secondary-Y pan/zoom channel | P3 | M | moderate | Needs an extra viewport channel + gesture routing. |
| Broken axis (skip empty ranges) | P3 | M | moderate | Piecewise transform + break glyphs. |
| ~~IANA / DST-aware time-zone axis~~ ⏸️ | ~~P3~~ | L | weak | **Deferred beyond 1.2.0:** needs a bundled KMP time-zone database + DST-correct tick generation — heavy for the payoff. UTC / fixed-offset time remains supported. |

### 4.2 Statistical & financial families

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Error bars / uncertainty whiskers (line/bar/scatter)~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `ChartPoint.yLow`/`yHigh` + `BarEntry.yLow`/`yHigh` (vertical bars) + `ErrorBarsSpec`; whiskers are planner output. |
| ~~Box-and-whisker (boxplot) family~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `BoxPlotChart` / `BoxPlotChartSpec` + `BoxPlotEntry.fromSamples` (Tukey quartiles/whiskers/outliers). |
| ~~Waterfall family (running-total bars + connectors)~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `WaterfallChart` / `WaterfallChartSpec` (floating bars, sign coloring, total markers, connectors). |
| ~~OHLC/candlestick variants (HLC bar, Heikin-Ashi, hollow)~~ ✅ | ~~P3~~ | S | strong | **Shipped:** `CandlestickChartSpec.variant` (`CandlestickVariant.Candles` / `HollowCandles` / `OhlcBars` / `HeikinAshi`); `heikinAshi(points)` transform is public. |
| Histogram / density / violin | P3 | M | moderate | Binning/KDE transforms; bins render as bars. |
| ~~Financial extras: range-selector strip, data grouping, indicators (SMA/EMA/RSI/MACD/Bollinger)~~ ✅ | ~~P3~~ | XL | moderate | **Shipped:** `TechnicalIndicators` (SMA / EMA / RSI / MACD / Bollinger, index-aligned, KMP-safe) + the range-selector strip (`ChartRangeSelector`, P2). Overlay any indicator as a `LineSeries`. (Period data-grouping stays caller-side.) |

### 4.3 Part-to-whole & hierarchical families

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Bubble chart as a first-class family (z→radius scale + size legend)~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `BubbleChart` / `BubbleChartSpec` / `BubbleSeries` + area-proportional `BubbleSizeScale` + size legend; reuses the scatter pipeline via `toScatterSpec`. |
| ~~Bullet graph (KPI target/threshold bands)~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `BulletChart` / `BulletChartSpec` / `BulletBand` — qualitative bands + measure bar + target marker (read-only, gauge-style). |
| ~~First-class stacked area (+ percent / streamgraph offsets)~~ ✅ | ~~P2~~ | M | strong | **Shipped:** `StackedAreaChart` / `StackedAreaChartSpec` / `StackedAreaSeries` with `AreaStackOffset.Zero` / `Expand` / `Silhouette` (streamgraph). |
| ~~Funnel family~~ ✅ | ~~P2~~ | M | strong | **Shipped:** `FunnelChart` / `FunnelChartSpec` / `FunnelStage` — stacked centered trapezoids (read-only, gauge-style). |
| ~~Radial bar / circular progress-bar~~ ✅ | ~~P3~~ | S | strong | **Shipped:** `RadialBarChart` / `RadialBarChartSpec` / `RadialBarTrack` — concentric progress rings (track + value arc per ring), read-only with a legend. |
| Treemap | P3 | L | moderate | Squarified layout + hierarchical model + label fitting. |
| Sunburst / hierarchical radial | P3 | L | moderate | Multi-ring arcs + tree model + drilldown. |
| Circle-packing / bubble-pack | P3 | L | moderate | Enclosure layout over hierarchical data. |
| Parallel coordinates | P3 | L | moderate | Multi-axis weaving; per-axis brushing is the hard part. |
| ~~Calendar / year heatmap~~ ✅ | ~~P3~~ | M | strong | **Shipped:** `CalendarHeatmapChart` / `CalendarHeatmapSpec` / `CalendarHeatmapDay` — GitHub-style week×weekday grid, colored via `HeatmapColorScale`; caller supplies 0-based day offsets (KMP-safe, no date lib). |

### 4.4 Reference, annotations & derived series

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Annotations layer (point / text / box-region / segment)~~ ✅ | ~~P0~~ | L | strong | **Shipped:** `ChartAnnotation` on line/scatter/bar (`annotations`). |
| ~~Data labels (per-point/bar value labels + collision avoidance)~~ ✅ | ~~P0~~ | M | strong | **Shipped:** `DataLabelsSpec` on line/scatter/bar. |
| ~~Trendline / regression overlays (linear, moving-average) + confidence bands~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `TrendlineSpec` (`TrendlineType.Linear` / `MovingAverage`) + confidence bands on line/scatter; public `LinearRegression` in `chart-core`. |
| ~~Line interpolation: stepped and spline/monotone-cubic~~ ✅ | ~~P2~~ | M | strong | **Shipped:** `LineStyle.interpolation` (`LineInterpolation.StepBefore` / `StepAfter` / `StepMiddle` / `MonotoneCubic`) on line, sparkline, and combined line layers; stroke + area fill follow the curve. |

### 4.5 Interaction & exploration

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Crosshair / axis-pointer~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `CrosshairSpec` (line/scatter) — vertical + horizontal guidelines + axis-value chips on the selection pipeline. |
| ~~Interactive legend (toggle)~~ ✅ | ~~P0~~ | M | strong | **Shipped:** `hiddenSeriesIds` + `onLegendItemClick` (line/scatter/bar). Hover-highlight still open. |
| ~~Range brush / box selection (+ linked filtering)~~ ✅ | ~~P2~~ | L | moderate | **Shipped:** `LineChart(rangeBrush = ChartRangeBrush(...))` drag-rectangle brush → normalized `ChartRangeSelection` (domain rect) via `onBrush`, with `ChartBrushAxis.Horizontal` / `Vertical` / `Both`. Pairs with `zoomTo` for zoom-to-selection or linked filtering. |
| ~~Range selector / minimap / brush navigator~~ ✅ | ~~P2~~ | L | moderate | **Shipped:** `ChartRangeSelector(spec, viewportState)` — a full-width overview over the pan/zoom domain with a draggable window bound to the shared `ChartViewportState` (drag body to pan, edges to zoom, outside to jump). |
| ~~Cross-chart sync (linked selection & tooltip, `syncId`-style)~~ ✅ | ~~P2~~ | M | moderate | **Shipped:** `SyncChartViewports(...)` links a group of `ChartViewportState`s so pan/zoom on one mirrors the shared axis onto the others (`ChartSyncAxis.X` / `Y` / `Both`); linked selection/tooltips by sharing one `ChartSelectionState`. |
| Distinct hover interaction (pointer-move highlight vs tap) | P3 | M | moderate | Adds a hover channel to gesture policies; matters for desktop/iPad. |
| Drill-down (click a mark to expand detail) | P3 | M | weak | Mostly app-level; library exposes callbacks + transitions. |

### 4.6 Realtime & data lifecycle

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Streaming / real-time append helpers (incremental push, ring-buffer, append transitions)~~ ✅ | ~~P1~~ | L | moderate | **Shipped:** `StreamingSeriesBuffer` (bounded ring buffer, `chart-core`) + `rememberStreamingSeries` / `StreamingSeriesState` (observable snapshots, pairs with data-transition easing). |
| ~~Null-gap handling~~ ✅ | ~~P0~~ | S | strong | **Shipped:** `ChartPoint.gap(x)` + `LineRenderSpec.connectAcrossGaps`. |
| ~~LTTB (largest-triangle-three-buckets) decimation~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `LineDecimationStrategy.Lttb` — shape-preserving alternative to Uniform/MinMaxY. |
| ~~Percent / normalized stacking (see 4.1)~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `BarStackMode.PercentOfTotal` + `AreaStackOffset.Expand`. |

### 4.7 Theming & color system

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~`ChartColor` constructors (`rgb`/hex/hsl + `withAlpha`)~~ ✅ | ~~P1~~ | S | strong | **Shipped:** `ChartColor.rgb`/`argb`/`hex`/`hsl` + `withAlpha` + channel accessors. |
| ~~Named sequential/diverging/categorical palettes (viridis, turbo, tableau, colorbrewer)~~ ✅ | ~~P2~~ | M | strong | **Shipped:** `ChartPalettes` (Tableau10 / Category10 / ColorblindSafe) + `ColorGradient` (Viridis / Plasma / Turbo / RdBu / BrBG) with `colorAt`/`sample`. |
| ~~Named light/dark theme presets + auto dark-mode adaptation~~ ✅ | ~~P3~~ | S | strong | **Shipped:** `ChartChromeTheme.Light` / `.Dark` presets + `chartChromeThemeAuto(darkTheme = isSystemInDarkTheme())`. |
| ~~Dark-mode-aware **data** palettes (planner does not auto-shift series colors today)~~ ✅ | ~~P3~~ | M | moderate | **Shipped:** `ChartColor.lighten` / `.darken` + `ChartPalettes.forDarkBackground(palette, amount)` (tint categorical colors for dark surfaces). |
| Expanded gradient fills (radial, multi-stop, per-series brushes) across families | P3 | M | moderate | Style + canvas-adapter extension. |
| Pattern / texture (decal) fills (colorblind-safe, print) | P3 | M | moderate | Pattern DrawCommand + shader tiling; doubles as a11y. |

### 4.8 Accessibility beyond markers

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Data-table alternative / "view data" surface (screen-reader-navigable value list)~~ ✅ | ~~P1~~ | M | strong | **Shipped:** `ChartDataTable` model + `lineChartDataTable` / `scatterChartDataTable` / `barChartDataTable` builders (`chart-core`) + `ChartDataTable(...)` composable with row-level semantics. |
| ~~Keyboard selection-roam for Heatmap & Gauge (parity with the rest)~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `KeyboardSelectionRoam.heatmapRoam` (Shift+arrows across the grid). Gauge is a single non-interactive value with no selectable elements, so roam is N/A. |
| ~~Whole-chart auto-generated screen-reader summary (trend, min/max, series count)~~ ✅ | ~~P2~~ | M | strong | **Shipped:** `lineChartNarrative` / `scatterChartNarrative` / `barChartNarrative` in `chart-core`, opt-in via `ChartAccessibilitySpec.includeDataNarrative`. |
| ~~Sonification (data-to-audio)~~ ⏸️ | ~~P3~~ | XL | weak | **Deferred beyond 1.2.0:** XL platform-audio `expect`/`actual` work for a niche audience; the data-table + narrative surfaces already cover non-visual access. |

### 4.9 Export & interop

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Raster (PNG/bitmap) snapshot/export~~ ✅ | ~~P2~~ | M | moderate | **Shipped:** `rememberChartCaptureController()` + `Modifier.chartCaptureTarget(...)` record the chart into a `GraphicsLayer`; `capturePng()` / `captureImageBitmap()` snapshot it, with an `expect`/`actual` `ImageBitmap.encodeToChartPng()` (Android `Bitmap.compress` / iOS Skia). |
| ~~Underlying-data export (CSV / JSON)~~ ✅ | ~~P3~~ | S | strong | **Shipped:** `ChartDataTable.toCsv()` / `.toJson()` over the `lineChartDataTable` / `scatterChartDataTable` / `barChartDataTable` builders (RFC 4180 CSV, self-describing JSON). |
| ~~`DrawCommand` → SVG / vector export~~ ✅ | ~~P3~~ | M | moderate | **Shipped:** `chartLayoutPlanToSvg(plan)` serializes any planned `ChartLayoutPlan` (shapes, paths, gradients, arcs, clips) to a standalone SVG — no DOM; `lineChartToSvg(spec, w, h)` is a one-call convenience. |
| ~~PDF export / direct print~~ ⏸️ | ~~P3~~ | L | weak | **Deferred beyond 1.2.0:** low ROI now that raster (PNG) and vector (SVG) export ship — SVG prints/embeds losslessly. Revisit if demand appears. |

### 4.10 Non-cartesian coordinate systems

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| Polar / wind-rose coordinate system (polar bars, scatterpolar) | P3 | L | moderate | Radar proves the polar grid; a general angle+radius mode is a new plotting system. |
| Faceting / small multiples (shared or per-facet scales) | P3 | L | moderate | Grid-of-plots composition layer in `chart-compose`. |

### 4.11 Performance (large data)

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~LTTB decimation option (see 4.6)~~ ✅ | ~~P2~~ | S | strong | **Shipped:** `LineDecimationStrategy.Lttb`. |
| Progressive / frame-chunked rendering for very large sets | P3 | L | moderate | Helps million-point cases; interacts with the animation/gesture loop. |
| ~~GPU/batch boost renderer~~ ⏸️ | ~~P3~~ | XL | weak | **Won't do:** Compose Canvas has no cross-platform GPU batch path; decimation (`Uniform` / `MinMaxY` / `Lttb`) + the plot-width pixel budget are the pragmatic substitute. |

### 4.12 Platform-specific

| Feature | Pri | Size | Fit | Notes |
|---|---|---|---|---|
| ~~Android Glance / home-screen widget rendering~~ ⏸️ | ~~P3~~ | M | weak | **Deferred beyond 1.2.0:** Android-only static-image path breaks the KMP symmetry the library is built around; the PNG/SVG export path covers static embedding cross-platform. |

---

## 5. Production-hardening workstreams

Quality/robustness work carried over from the retired production-readiness backlog. Not new features —
correctness, parity, and polish on what already ships.

- **Layout & text measurement.** Unify all label paths (x/y ticks, legend, future chrome) to one
  baseline-centered / cap-height policy via `TextLayoutMetrics`, removing the remaining magic optical offsets
  in `ChartPlanner`; close the last tick-label overlap edge cases.
- **iOS ↔ Android measurement parity.** Exercise layouts on real iOS hosts under font scale, locale scripts,
  and large strings (same `TextMeasurer` pipeline); add manual matrices and, where stable, iOS
  screenshot/golden comparison tooling (Android has Roborazzi; iOS has none yet).
- **iOS device QA.** Run the physical-device gesture matrix (pan, pinch, fling, double-tap zoom/reset,
  tap-vs-pan slop, keyboard nudge) and log it in [`ios-gesture-validation.md`](ios-gesture-validation.md) —
  only a compile-only gate exists today.
- **Selection model.** Generalize beyond point / bar-segment / pie-slice selection; expose richer optional
  selection metadata for custom markers and accessibility.
- **Markers & tooltips.** Deeper multi-marker / global collision solving beyond the current rect
  nudge/stack helpers; optional first-class bezier/stroked callout rendering in the library
  (see [`marker-tooltip-placement.md`](marker-tooltip-placement.md)).
- **Animation depth.** Cross-family and topology-changing data morphs (bar/scatter/combined and
  topology-changing line); richer physics than the current gesture chase.
- **Planner evolution.** Continue toward an explicit multi-pass structure
  (bounds → ticks → measure → final layout → draw commands) via incremental refactors; consider memoizing the
  whole `ChartLayoutPlan` in Compose beyond the current pass-2 tick and grid-line reuse.
- **Theming depth.** A full design-system token bridge beyond the `MaterialChartPalette` snapshot, and
  theme/token integration for data strokes (not just chrome).
- **Performance CI.** Optional non-blocking JMH / Android Macrobenchmark baselines on pinned hardware
  (see [`performance-benchmarks.md`](performance-benchmarks.md)); currently local-only. Further oversample /
  scatter-decimation default tuning as product feedback warrants.
- **Time-axis semantics.** Trading-holiday and session-hour calendars in core; zone-aligned weekday policy
  (today's weekend filtering is UTC calendar days / fixed offset only).
- **Testing.** Extend Roborazzi coverage (scatter/pie elevated-font-scale rows, more per-family goldens);
  keep growing `chart-core` planner/hit-test/degenerate-input tests as families gain features.

---

## 6. Showcase app roadmap

`samples/android-showcase` is the sole runnable demo (iOS is library-only). It ships a flat home screen with
per-chart demo screens and Roborazzi goldens. Open work (see [`showcase-guide.md`](showcase-guide.md) for the
current layout):

- **Grouped-by-use-case home nav** (Basics / Styling / Interaction / Realtime & performance / Advanced
  compositions / Accessibility & theming) instead of the current flat button list.
- **Styling screens:** multi-series line, filled/gradient line, grouped & stacked bars, legend & axis
  customization, tooltip & selection customization.
- **Interaction screens:** pan & zoom, visible-range controls, programmatic viewport changes, a dedicated
  selection & highlighting demo.
- **Realtime & performance screens:** realtime append-points, large-dataset line & bar, optional
  sampled-vs-unsampled comparison.
- **Advanced compositions:** chart in scroll content, chart inside tabs/pager, a multi-chart dashboard.
- **Accessibility & theming screens:** light/dark comparison, dynamic color, reduced-motion, a11y labels &
  contrast, and empty/loading/error states.
- **Datasets & docs integration:** committed deterministic datasets (time series, categorical sales,
  financial candles, sensor streams, multi-series comparisons); per-screen purpose + API-concept notes;
  a docs-side screen index with screenshots and code snippets sourced from the app.
- **Testing & long-term:** navigation smoke tests, large-dataset performance checks, theming verification;
  later — in-app code-snippet viewer, tablet/form-factor layouts, benchmark mode, debug overlays for
  bounds/ticks.

---

## 7. Documentation gaps

Reference docs are maintained per feature. Guides still to write:

- **MPAndroidChart migration guide** (immutable specs vs setters, state objects vs `invalidate()`,
  `MarkerView` → composable slots).
- **Accessibility guide** consolidating the a11y surface (roles, keyboard roam, live region, custom actions,
  reduced motion) beyond [`accessibility-custom-actions.md`](accessibility-custom-actions.md).
- **Performance guide** (windowing, decimation strategies, pixel budget, when to cap) beyond
  [`performance-benchmarks.md`](performance-benchmarks.md).

---

## 8. Verifying changes

Mirror CI locally before opening a PR:

```bash
# ktlint + binary-API + KMP purity
./gradlew ktlintCheck :chart-core:apiCheck :chart-compose:apiCheck :chart-core:verifyChartCorePurity

# unit tests (JVM + Android)
./gradlew :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest

# Roborazzi screenshot verification (record after intentional UI changes)
./gradlew :samples:android-showcase:verifyRoborazziDebug
./gradlew :samples:android-showcase:recordRoborazziDebug

# assemble the Android sample
./gradlew :samples:android-showcase:assembleDebug

# iOS compile gate (macOS) — the library targets iOS; there is no iOS demo app
./gradlew :chart-compose:compileKotlinIosSimulatorArm64

# API dump after intentional public-API changes; docs (Dokka)
./gradlew :chart-compose:apiDump :chart-core:apiDump
./gradlew dokkaGenerateHtml
```

Before tagging a release, walk [`api-checklist.md`](api-checklist.md).
