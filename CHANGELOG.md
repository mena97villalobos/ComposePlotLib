# Changelog

All notable changes to `ComposePlotLib` are documented here.

Versions are published to GitHub Packages from the Release workflow: pushing a semver tag like
`v1.1.0` publishes version `1.1.0` (see `README.md` → Install).

## 1.2.0 — unreleased

Sankey chart rework: orientation support, professional visual defaults, and layout correctness
fixes. **Source-compatible; the default Sankey look intentionally changes** (see *Changed* below);
precompiled callers of the `SankeyLayoutSpec` / `SankeyNodeLabelsSpec` data-class constructors or
`copy` must recompile (trailing parameters were appended). `PlannedSankeyLink` gained a required
`pathSegments` property (plan output types are planner results, not user-constructed specs).

### Added

- **Sankey orientation** — `SankeyLayoutSpec.orientation` (`SankeyOrientation.Horizontal` /
  `Vertical`): columns can flow left-to-right (default, unchanged) or top-to-bottom. Layout knobs
  are documented in flow/cross-axis terms; hit-testing, markers, and accessibility work in both
  orientations.
- **Orientation-neutral column alignment** — `SankeyLayoutSpec.columnAlignment`
  (`SankeyColumnAlignment.Start` / `Center` / `End` / `Justify`); the legacy
  `columnVerticalAlignment` still applies when `columnAlignment` is `null`.
- **Node palette + ribbon color modes** — `SankeyLayoutSpec.nodePalette` (Tableau-10 default,
  assigned by spec index; empty list restores the monochrome `defaultNodeColor`) and
  `SankeyLayoutSpec.linkColorMode` (`SankeyLinkColorMode.Static` / `SourceNode` / `TargetNode` /
  `SourceToTargetGradient`); an explicit `SankeyLink.color` always wins.
- **Node label placement** — `SankeyNodeLabelsSpec.placement`
  (`SankeyNodeLabelPlacement.Adjacent` d3-style beside the node, or `Inside`) with
  `adjacentColor`; inside labels now ellipsize (`…`) instead of silently dropping.
- **`SankeyChart` parity with the cartesian charts** — `noData` / `noDataUi` empty states,
  double-tap zoom, keyboard viewport nudge (`keyboardViewportNudgeEnabled`), pan fling, TalkBack /
  VoiceOver `stateDescription` for the selected node/ribbon, and selection auto-clear when the
  selected element pans out of view.
- One new draw primitive: `PathDrawStyle.LinearGradientFill` (point-to-point gradient; used for
  sankey ribbons).
- **`ChartColor` ergonomic factories** — `ChartColor.rgb(r, g, b)` / `argb(a, r, g, b)` (0..255
  channels), `hex("#RRGGBB" / "#AARRGGBB" / "#RGB")`, and `hsl(hue, saturation, lightness, alpha)`,
  plus `withAlpha(fraction)` and read-only `alpha` / `red` / `green` / `blue` channel accessors and a
  `ChartColor.Transparent` constant. All KMP-safe (no `java.*` / Compose types).
- **Error bars / uncertainty whiskers** — `ChartPoint.yLow` / `yHigh` (line & scatter) and
  `BarEntry.yLow` / `yHigh` (vertical bar charts) carry per-datum bounds; `ErrorBarsSpec` (on
  `LineChartSpec` / `ScatterChartSpec` / `BarChartSpec`) styles the whisker + end caps. Bounds may be
  asymmetric or one-sided; a datum with neither bound draws nothing.
- **Trendline / regression overlays** — `TrendlineSpec` on `LineChartSpec` / `ScatterChartSpec`
  fits `TrendlineType.Linear` (ordinary least squares, with an optional mean-response confidence band)
  or `TrendlineType.MovingAverage(window)` per series. Backed by a new public, KMP-safe
  `com.mena97villalobos.composeplotlib.core.math.LinearRegression` (`fit` → slope / intercept /
  r² / residual std-dev / mean-response standard error).
  - `ChartPoint` and `BarEntry` gained trailing `yLow` / `yHigh` parameters; precompiled callers of
    their constructors / `copy` must recompile.
- **Crosshair / axis-pointer** — `CrosshairSpec` on `LineChartSpec` / `ScatterChartSpec` draws a
  vertical and/or horizontal guideline through the active selection, plus optional axis-value chips at
  the plot's inner edges (formatted with each axis' `labelFormatter`). Disabled by default; when
  enabled it replaces the plain vertical selection guideline.
- **Accessible data-table ("view data")** — `ChartDataTable` in `chart-core` plus
  `lineChartDataTable` / `scatterChartDataTable` / `barChartDataTable` builders turn any spec into
  headers + formatted rows; the `ChartDataTable(...)` composable (`chart-compose`) renders it as a
  scrollable, screen-reader-navigable table (each row is one merged semantics node).
- **Streaming / real-time append helpers** — `StreamingSeriesBuffer` (bounded ring buffer, `chart-core`)
  and the `rememberStreamingSeries` / `StreamingSeriesState` Compose helpers publish an immutable
  snapshot on each `append`, so a line/scatter spec rebuilt from `state.points` replans without
  unbounded growth. Pairs with `ChartAnimationPolicy` data-transition easing.
- **Box-and-whisker (boxplot) chart family** — `BoxPlotChart` composable + `BoxPlotChartSpec` /
  `BoxPlotSeries` / `BoxPlotEntry` / `BoxPlotStyle`. Draws the Q1–Q3 box, median line, Tukey whiskers
  with caps, and outlier points; `BoxPlotEntry.fromSamples(x, values)` computes the five-number summary
  (linear-interpolation quartiles + 1.5·IQR whiskers/outliers). Pan/zoom/fling/double-tap, tap selection
  (`BoxPlotHitResolver`), **Shift+arrow keyboard selection-roam** (`KeyboardSelectionRoam.boxPlotChartRoam`
  — Left/Right steps entries, Up/Down switches series), an optional animated **marker** slot (five-number
  summary), no-data, and TalkBack/VoiceOver semantics.
- **Waterfall chart family** — `WaterfallChart` composable + `WaterfallChartSpec` / `WaterfallEntry`.
  Each step's bar floats between the running total before and after it (increase / decrease / total
  markers colored distinctly) with optional connector lines. Pan/zoom/fling/double-tap, tap selection
  (`WaterfallHitResolver`), **Shift+Left/Right keyboard selection-roam**
  (`KeyboardSelectionRoam.waterfallChartRoam`), an optional animated **marker** slot (contribution +
  running total), no-data, and TalkBack/VoiceOver semantics.
- **LTTB decimation** — `LineDecimationStrategy.Lttb` (largest-triangle-three-buckets): a
  shape-preserving downsampling option that keeps the visually significant vertices (peaks, troughs,
  inflections) far better than `Uniform`, at ~half the vertices of `MinMaxY`.
- **Named color palettes** — `ChartPalettes` (categorical `Tableau10` / `Category10` / `ColorblindSafe`
  lists + `cyclic(...)` wrap-around indexing) and `ColorGradient` (sequential `Viridis` / `Plasma` /
  `Turbo` and diverging `RdBu` / `BrBG`, with `colorAt(t)` interpolation and `sample(count)`). All
  KMP-safe in `chart-core`.
- **Auto-generated data narrative (accessibility)** — `lineChartNarrative` / `scatterChartNarrative` /
  `barChartNarrative` in `chart-core` produce a screen-reader summary (per-series point count, min/max
  range, first→last trend). Opt in via `ChartAccessibilitySpec.includeDataNarrative` to prefix the
  chart's `contentDescription`, or call them directly.
- **Heatmap keyboard selection-roam** — `HeatmapChart` now supports Shift+arrow grid navigation
  (`KeyboardSelectionRoam.heatmapRoam`) when `ChartAccessibilitySpec.keyboardSelectionRoamEnabled` is on,
  clamping at edges and skipping gaps in sparse grids.
- **Line interpolation** — `LineStyle.interpolation` (`LineInterpolation`): stepped
  (`StepBefore` / `StepAfter` / `StepMiddle`) and shape-preserving `MonotoneCubic` (Fritsch–Carlson) curves.
  Both the stroke and the area fill follow the interpolation; applies to line charts, `Sparkline`, and
  combined-chart line layers. Default `Linear` (unchanged).
- **Reversed axis direction** — `AxisSpec.inverted` reverses an axis (X right→left, Y top→bottom) via the plot
  transform, so data, grid, ticks, markers, and selection all follow on every cartesian family. Pan/zoom
  direction is additionally reversed on `LineChart` / `ScatterChart` / `BarChart`.
- **Bubble chart family** — `BubbleChart` composable + `BubbleChartSpec` / `BubbleSeries` / `BubblePoint`,
  where each bubble's size (`z`) maps to a radius via an **area-proportional** `BubbleSizeScale` (`radiusFor`,
  `auto`, `legendTicks`). Rendering, gestures, selection, and markers reuse the scatter pipeline
  (`BubbleChartSpec.toScatterSpec`); the composable overlays an optional size legend.
- **Bullet graph family** — `BulletChart` composable + `BulletChartSpec` / `BulletBand`: a compact linear KPI
  display with qualitative range bands behind a measure bar and an optional target marker. Read-only
  (gauge-style, no pan/zoom/selection), with optional min/max bound labels.
- **Funnel chart family** — `FunnelChart` composable + `FunnelChartSpec` / `FunnelStage`: ordered stages as
  stacked, centered trapezoids that taper from each stage's width (∝ value) to the next, colored from the
  spec palette with optional centered labels. Read-only.
- **Stacked-area chart family** — `StackedAreaChart` composable + `StackedAreaChartSpec` / `StackedAreaSeries`:
  index-aligned bands stacked over shared x values with `AreaStackOffset.Zero` (absolute), `Expand` (100%),
  or `Silhouette` (streamgraph). Cartesian with grid/legend and pan/zoom (no per-point selection).
- **100% / percent bar stacking** — `BarChartSpec.stackMode` / `StackedBarChartSpec.stackMode`
  (`BarStackMode.PercentOfTotal`) normalizes each stacked category to fill the `[0, 1]` value axis; the
  `BarChart` / `StackedBarChart` default viewport switches to `[0, 1]` automatically. Segments keep their raw
  values for markers/labels. Default `Absolute` (unchanged).
- **Cross-chart viewport sync** — `SyncChartViewports(vararg states, axis)` links a group of
  `ChartViewportState`s so panning or zooming any one mirrors the shared axis window onto the others
  (`ChartSyncAxis.X` / `Y` / `Both`) — the `syncId` / "connect" pattern for aligned dashboards. Each follower
  clamps to its own domain and feedback loops are avoided. Link selection/tooltips by sharing one
  `ChartSelectionState`.
- **Raster (PNG) export** — `rememberChartCaptureController()` + `Modifier.chartCaptureTarget(controller)`
  record a chart into a `GraphicsLayer`; `controller.capturePng()` / `captureImageBitmap()` snapshot exactly
  what was drawn (markers and overlays included). Encoding is `expect`/`actual`
  `ImageBitmap.encodeToChartPng()` — Android `Bitmap.compress`, iOS Skia `Image.encodeToData`.
- **Range brush / box selection** — `LineChart(rangeBrush = ChartRangeBrush(...))` adds a drag-rectangle brush
  that reports the covered domain region as a normalized `ChartRangeSelection` (new `chart-core` type) via
  `onBrush`, with `ChartBrushAxis.Horizontal` / `Vertical` / `Both`. While a brush is attached it takes over the
  drag channel (pan/zoom/tap suspended). Pairs with `ChartViewportState.zoomTo(...)` for zoom-to-selection or
  drives linked filtering.
- **Range selector / minimap** — `ChartRangeSelector(spec, viewportState, style)` renders a compact full-width
  overview over the whole pan/zoom domain with a draggable window bound to the shared `ChartViewportState`: drag
  the window to pan, its edges to zoom, or outside it to jump. Place it beneath a chart sharing the same
  viewport to get a brush navigator that stays in lock-step.
- **Underlying-data export (CSV / JSON)** — `ChartDataTable.toCsv(delimiter, lineSeparator)` (RFC 4180) and
  `ChartDataTable.toJson()` (self-describing) serialize the tables produced by `lineChartDataTable` /
  `scatterChartDataTable` / `barChartDataTable`, so a chart's underlying data can be exported.
- **SVG / vector export** — `chartLayoutPlanToSvg(plan, background)` serializes any planned `ChartLayoutPlan`
  (lines, polylines, rects, circles, text, paths, gradients, arcs/donut sectors, clips, groups) to a standalone,
  resolution-independent SVG document with no DOM or rasterization; `lineChartToSvg(spec, widthPx, heightPx)`
  plans a line chart headlessly and returns the SVG in one call.
- **Radial bar chart family** — `RadialBarChart` composable + `RadialBarChartSpec` / `RadialBarTrack`: concentric
  progress rings (outermost first), each a background track plus a foreground value arc spanning
  `sweepDegrees * value / maxValue` (auto-scaled to the largest track when `maxValue` is null). Read-only
  (gauge-style) with an optional legend.
- **Calendar / year heatmap family** — `CalendarHeatmapChart` composable + `CalendarHeatmapSpec` /
  `CalendarHeatmapDay` / `CalendarMonthLabel`: a GitHub-contribution-style week-column × weekday-row grid colored
  via the shared `HeatmapColorScale`. Callers pass 0-based `dayIndex` offsets (KMP-safe, no date library) with
  optional weekday and month labels. Read-only.
- **Light/dark theming presets + dark-aware palettes** — `ChartChromeTheme.Light` / `.Dark` presets and
  `chartChromeThemeAuto(darkTheme = isSystemInDarkTheme())` give mode-fixed chrome independent of `MaterialTheme`;
  `ChartColor.lighten(fraction)` / `.darken(fraction)` and `ChartPalettes.forDarkBackground(palette, amount)` tint
  categorical data colors for legibility on dark surfaces (the planner still does not auto-shift series colors).
- **Financial technical indicators** — `TechnicalIndicators` in `chart-core`: `sma` / `ema` / `rsi` / `macd`
  (`MacdSeries`) / `bollingerBands` (`BollingerBands`) over a value series, index-aligned (`null` until defined)
  and KMP-safe. Map any result to a `LineSeries` to overlay it on a line or candlestick chart.
- **Candlestick variants** — `CandlestickChartSpec.variant` (`CandlestickVariant.Candles` (default) /
  `HollowCandles` (bullish bodies outlined) / `OhlcBars` (open-tick-left, close-tick-right) / `HeikinAshi`
  (smoothed)); the `heikinAshi(points)` OHLC transform is public. `CandlestickChartSpec` gained a trailing
  `variant` parameter (recompile precompiled positional callers).
- **Treemap chart family** — `TreemapChart` composable + `TreemapChartSpec` / `TreemapNode`: area-proportional
  tiles laid out by a squarified algorithm (`squarifiedTreemap` in `chart-core`, aspect ratios kept near 1) with
  per-tile fill color and label fitting. Single-level (flat), read-only.
- **Sunburst chart family** — `SunburstChart` composable + `SunburstChartSpec` / `SunburstNode`: a hierarchy drawn
  as concentric rings (one per depth), each node's angular span proportional to its value within its parent
  (`sunburstLayout` in `chart-core`). Optional center-disk root; top-level branches take palette colors and
  descendants inherit a lighter shade. Read-only.

### Changed

- **Default Sankey look** — nodes use the Tableau-10 palette (was uniform slate), ribbons use
  source→target gradients (was flat 50%-alpha slate), node labels sit beside nodes (was rotated
  inside), node corners default to square (`nodeCornerRadius` `0.02` → `0.0`), and the default link
  label formatter renders `9.0` as `9`. Pass `nodePalette = emptyList()`,
  `linkColorMode = SankeyLinkColorMode.Static`, `nodeLabels = SankeyNodeLabelsSpec.DefaultInside`,
  and `nodeCornerRadius = 0.02` to restore the 1.1 look (note: `DefaultInside` keeps white label
  text — pair it with the monochrome palette or set `nodeLabels.color`).
- **Selection emphasis** — selecting a node/ribbon now dims the rest of the diagram and re-draws
  the selected element at full alpha with an outline along its actual curved geometry (was a
  straight centerline stroke across the curved ribbon).

### Fixed

- **Sankey vertical mirror** — node stacks were laid out against the domain y-axis flip, so
  `Top`/`Bottom` alignment anchored at the wrong edge, `SankeyNodeOrder.Input` placed the first
  node at the visual bottom, and `TargetTopToBottom`/`SourceTopToBottom` link stacking sorted the
  wrong way. All now mean what they say on screen. **Charts that relied on the inverted rendering
  will look vertically mirrored after upgrading.**
- **Column overflow** — the node-height scale paired the largest column total with a different
  column's usable height, so busy columns could overflow and their last nodes collapsed to zero
  height (invisible, unselectable, silently dropping flow). The scale now follows the d3-sankey
  rule (min over columns of usable-extent / total).
- **Link stacking saturation** — when `minLinkThickness` (or a squeezed node) made stacked bands
  exceed the node extent, later links collapsed to zero-thickness ribbons at the node edge; bands
  now rescale proportionally.
- **Column indices** — negative or offset column indices no longer collapse/shift the layout
  (normalized to the smallest used index; interior gaps keep an empty slot), and node width is
  capped so many-column charts can no longer overlap columns and fold ribbons backwards.
- **Hit-testing** — link tap targets are now sampled from the drawn Bézier ribbons (the old
  control-point polygon self-intersected at the default curvature and mismatched the visuals);
  node/link tap priority follows the drawn z-order (`linksBehindNodes`); marker anchors sit at the
  ribbon's true curve midpoint. Sub-pixel ribbons are widened to ~1px so tiny flows stay visible.

### Removed

- **`ComposePlotApp()` demo host** — the single-composable preview cycler in `chart-compose` (a
  `@Composable @Preview` that stepped through every chart family with one button) has been removed
  from the public API, along with the `composeApp` (Android + iOS host) and `iosApp` (Xcode) demo
  modules. **Source-breaking only for callers of `ComposePlotApp()`**; no other public chart API
  changed. The library still targets iOS (compile-gated in CI); there is no longer a bundled iOS
  demo app. Use `samples/android-showcase` — the sole sample app — to preview chart families, and
  see `docs/ios-library-usage.md` for embedding the charts in your own iOS host.

## 1.1.0 — unreleased

Additive, backward-compatible release. Existing specs keep their prior behavior with defaults.

### Added

- **Area / threshold-band fill on `LineChart`** — `LineStyle.areaFill`, `LineStyle.areaFillEnd`
  (vertical gradient), and `LineStyle.areaFillMode` (`AreaFillMode.ToPlotBottom`,
  `ToBaseline(value)`, `BelowThreshold(value)`).
- **Reference / threshold lines** — `ReferenceLine` (`ReferenceOrientation.Horizontal` / `Vertical`,
  `value`, `color`, `strokeWidth`, `dashIntervals`, `label`) via a `referenceLines` field on
  `LineChartSpec`, `BarChartSpec`, `ScatterChartSpec`, and `CombinedCartesianChartSpec`.
- **Marker shapes + per-point styling** — `MarkerShape` (`Circle` / `Square` / `Triangle` /
  `Diamond`) and `PointStyle.shape`, plus per-point overrides `ChartPoint.markerShape`,
  `ChartPoint.markerColor`, and `ChartPoint.markerRadius`.
- **Bubble sizing on `ScatterChart`** — a per-point `ChartPoint.markerRadius` override encodes a
  third dimension as point size.
- **Gauge chart (new family)** — `GaugeChart` / `GaugeChartSpec`: a radial progress ring with an
  optional `centerContent` slot; non-interactive.
- **Candlestick chart (new family)** — `CandlestickChart` / `CandlestickChartSpec` with `OHLCPoint`,
  `CandlestickSeries`, `CandlestickStyle`, `CandlestickRenderSpec`, and `CandlestickValueFormatter`;
  cartesian bodies + wicks, bull/bear coloring, pan/zoom, decimation, and a tap-to-OHLC marker.
  Chart-level **accessibility** (Image role + selection state) with an **`accessibilityCustomActions`**
  parameter, and **keyboard selection-roam** (Shift+Left/Right step candles within a series,
  Shift+Up/Down switch series, plain arrows pan) via the public
  `KeyboardSelectionRoam.candlestickChartRoam`. (Library shipped; downstream app adoption is gated on
  a live OHLC data feed.)
- **Sparkline** — a thin, axis-less / grid-less / non-interactive wrapper over `LineChart` with an
  optional area fill.

### Internal

- One new draw primitive: `PathDrawStyle.VerticalGradientFill` (for line area / threshold-band
  gradient fill). The planning → draw-command → canvas pipeline is otherwise unchanged.

### Publishing

- Publishing happens on a `v1.1.0` git tag (GitHub Packages, via the Release workflow).
