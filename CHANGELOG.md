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
