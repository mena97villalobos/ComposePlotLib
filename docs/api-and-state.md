# API And State Guide

See also **[`README.md`](README.md)** (documentation index) and **[`roadmap.md`](roadmap.md)** (roadmap / open work).

## Current Public API
The current public surface is intentionally small.

### Charts
- `LineChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)`
- `BarChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)`
- `StackedBarChart(spec, …)` — same parameters as `BarChart`; `spec` is `StackedBarChartSpec` (always stacked when group entry counts match; see `StackedBarChartSpec.toBarChartSpec()`).
- `ScatterChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — point-only cartesian chart; same gesture/marker/no-data/a11y shape as line/bar (`spec` is `ScatterChartSpec`).
- `PieChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — radial pie/donut; **pan/zoom/fling default off** (`ChartTransformGesturePolicy(enabled = false)`). Tap uses `PieHitResolver` in the planner’s `plotBounds`.
- `RadarChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — radial radar/spider chart (multi-series polygons); **pan/zoom/fling default off** (`PieDefault`). Tap selects the nearest vertex via `RadarHitResolver`.
- `SankeyChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — explicit-column flow diagram (`SankeyChartSpec`) with horizontal or vertical flow (`SankeyLayoutSpec.orientation`); tap supports selecting nodes and links; double-tap zoom, keyboard viewport nudge, and pan fling follow the same `gestureConfig`/accessibility flags as the cartesian charts; pan/zoom reuse `ChartViewportState` (normalized `0..1` domain by default).
- `CombinedCartesianChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — multi-layer cartesian overlay (`CombinedCartesianChartSpec`); tap hit-testing is **top-layer-first** (`CombinedCartesianHitResolver`). **Secondary *Y*** is data-derived in `chart-core` on each plan (min/max of secondary-bound layers; not an independent zoom target in `ChartViewportState` v1). See **`docs/design-combined-cartesian-layers.md`** §3.1.
- `CandlestickChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — cartesian OHLC chart (`CandlestickChartSpec`); draws bodies + wicks with bull/bear coloring, supports pan/zoom, **decimation** (`CandlestickRenderSpec`), and a **tap-to-OHLC** marker. Same gesture/marker/no-data/a11y shape as line/bar/scatter, including chart-level **`accessibilityCustomActions`** and **Shift+arrow keyboard selection-roam** (see **Accessibility**).
- `HeatmapChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — 2-D grid of colored cells (`HeatmapChartSpec`); each cell's color encodes a numeric value via `HeatmapColorScale`. Column labels map to the X axis, row labels to the Y axis (row 0 at the bottom). Tap selects a cell via `HeatmapHitResolver`; pan/zoom reuse `ChartViewportState`. Same marker/no-data/a11y shape as the other cartesian charts (Image role + optional `accessibilityCustomActions`; no keyboard selection-roam).
- `GaugeChart(spec, modifier, centerContent)` — radial progress / gauge ring (`GaugeChartSpec`); track + progress arc with optional rounded caps and an optional **`centerContent`** slot. **Non-interactive**: no viewport, selection, or gesture parameters.
- `Sparkline(...)` — thin convenience wrapper over `LineChart` with **axes, grid, and legend off**, zero padding, and **non-interactive** defaults; optional area fill. Intended for inline/compact trend rendering.

**Markers:** `ChartMarkerPlacement` / `ChartMarkerContainment` (`PlotBounds` default) control flip/slide tooltip placement inside the layout plan; optional **`ChartMarkerCalloutLeader`** draws an elbow (default) or **`ChartMarkerCustomCallout`** polyline from the anchor to the marker box; **`nudgeAxisAlignedRectToAvoidOverlap`** / **`stackAxisAlignedRectBelow`** help lay out multiple overlay boxes in chart pixels. See **`docs/marker-tooltip-placement.md`**. `DefaultChartMarker` supports optional outline and hiding label rows.

**Theming:** Data colors and strokes use **`ChartColor`** and style types on specs (`LineStyle`, `BarStyle`, …); plot frame and legend chrome use optional **`ChartChromeTheme`** / `chartChromeTheme()`. Map **`MaterialTheme`** tokens into specs with **`materialChartPalette()`** (includes `surface` / `surfaceVariant` / `primaryFixed` / `secondaryFixed`) / **`Color.toChartColor()`**, or use **`withMaterialSeriesColors`** / **`withMaterialGroupColors`** / **`withMaterialScatterSeriesColors`** (`chart-compose` `theme/`). See **`docs/style-and-theme.md`**.

### State
- `ChartViewportState`
- `ChartSelectionState`
- `rememberChartViewportState(...)` — optional `ViewportConstraints` and `ViewportAxisPolicy`; see `docs/viewport-configuration.md`
- `rememberChartSelectionState()`
- Empty overlays: `ChartNoDataUi`, `ChartNoDataDefaults`, `ChartNoDataContent`, `DefaultNoDataOverlay`
- `ChartAnimationPolicy` on charts — opt-in programmatic **viewport** easing; optional **gesture viewport chase** (`gestureViewportEasingEnabled`, `gestureViewportChaseStiffness`); marker **fade-in / fade-out** (clearing selection) when `selectionHighlightEnabled` and `selectionDurationMillis` > 0; optional **`LineChart`** same-topology **data** coordinate morph (`dataTransitionEnabled`, `dataTransitionDurationMillis`); `respectSystemReducedMotion` disables motion and durations when the user prefers reduced animation (see **Viewport State** / **Accessibility**)
- `ChartCustomAccessibilityAction` for TalkBack / VoiceOver custom actions on the chart node
- `chartViewportAccessibilityActions(viewportState, gestureConfig)` — optional reset + zoom in/out custom actions when transform gestures are enabled
- `chartAccessibilityActionsMergeViewport(viewportState, gestureConfig, appActions)` — concatenates viewport actions with app-defined custom actions (ordering: viewport first, then app)

### Helpers
- `ChartPlanner.computeCartesianTickValuePass(viewport, xAxis, yAxis)` → `TickValuePass` (pass-2 domain tick values before pixel thinning). Cartesian composables reuse it across the measurement pre-pass and final pass when the viewport and primary axes are unchanged. Optional `tickValuePass` on `ChartPlanner.planLineChart`, `planBarChart`, `planScatterChart`, and `planCombinedCartesianChart` (default: compute internally).
- `defaultViewport(LineChartSpec)`
- `defaultViewport(BarChartSpec)` — vertical: *X* spans categories (`entry.x`), *Y* spans values with zero included; horizontal: *X* spans values, *Y* spans categories.
- `defaultViewport(ScatterChartSpec)`
- `defaultViewport(CombinedCartesianChartSpec)` — primary *X* / **shared *Y*** from layers bound to `CombinedCartesianYAxisBinding.SharedY`; secondary *Y* range is still computed in the planner.
- `defaultViewport(PieChartSpec)` — placeholder `0..1` ranges (layout ignores axes; keeps measurement passes consistent).
- `defaultViewport(RadarChartSpec)` — placeholder `0..1` ranges (layout ignores cartesian axes; keeps measurement passes consistent).
- `defaultViewport(HeatmapChartSpec)` — *X* spans the column count, *Y* spans the row count (category indices; row 0 at the bottom).
- `SankeyLayoutSpec` — sankey layout controls in flow/cross-axis terms: `orientation` (`SankeyOrientation.Horizontal` / `Vertical`), per-column node ordering and cross-axis alignment (`columnAlignment: SankeyColumnAlignment?`, falling back to the legacy `columnVerticalAlignment`); per-node outgoing/incoming link ordering for ribbon stacking; `nodePalette` (Tableau-10 default) and `linkColorMode` (`SankeyLinkColorMode`, default source→target gradient); optional node and link labels (`nodeLabels.enabled` / `linkLabels.enabled`). Node labels default to adjacent (d3-style, `SankeyNodeLabelPlacement.Adjacent`); inside placement supports `SankeyNodeLabelOrientation` and ellipsizes labels that do not fit. All labels are measured via the standard two-pass `TextMeasurer` path.
- `ChartGestureConfig(...)` — optional **presets** on the companion object: `CartesianDefault` (same as `ChartGestureConfig()` for cartesian charts), `PieDefault` (`PieChart` default), `readOnly()`, `exploreOnly(...)`, `cartesianInteractive(...)` (double-tap zoom-in + optional fling; used by the Android line showcase), `cartesianInteractiveFullExploration()` (zoom in/out + reset + fling for full device QA). See **`docs/ios-gesture-validation.md`** for iOS QA matrices.

### Demo surface (`samples/android-showcase`)
- The Android **showcase** (`ShowcaseApp`) is the demo and manual-verification surface: per-family screens (**line, bar, stacked bar, horizontal bar, scatter, pie, radar, combined, sankey, heatmap, gauge, candlestick, domain axes**), richer demos—including **Combined (multi-layer)**—and Roborazzi screenshot tests. Adding a new chart family **must** include a showcase screen (**`AGENTS.md` → Obligations**).
- iOS is supported as a **library** target; consume `chart-compose` from your own Compose app. See `docs/ios-library-usage.md`.

## Current Spec Model
Chart composables consume immutable specs rather than mutable chart objects.

### Line chart
Main types:
- `LineChartSpec`
- `LineSeries`
- `ChartPoint`
- `AxisSpec`
- `GridSpec`
- `LegendSpec`

`AxisSpec` supports:
- `scale: AxisScale` — **`Linear`**, **`Category(categories)`** (integer indices → label strings; ticks respect `tickMin`/`tickMax` ∩ viewport), **`TimeEpochMillis`** (duration-nice ticks in ms; default formatter path uses compact UTC labels; see **`docs/axis-category-time-cookbook.md`**), **`Log(base = 10.0)`** (logarithmic — decades evenly spaced, power-of-base ticks with 2×/5× minors over ≤2 decades; for strictly positive data — non-positive values are treated as gaps; intended for line/scatter), or **`SymLog(base, linearThreshold)`** (log away from zero, linear within `±linearThreshold`, for data spanning zero/negatives)
- `labelFormatter: (Double) -> String`
- `categoryLabelMaxChars` — optional cap on category tick-label length (ellipsis); ignored for linear/time scales
- `epochMillisTickPolicy` — optional post-pass on time-scale tick **values** (`All` default; `WeekdaysUtc`; `WeekdaysFixedOffset` for fixed-offset weekends; `ExcludeUtcDays` for app-supplied UTC day indices; `SessionUtcMinutes` for a UTC minute-of-day window; `Combined` to chain policies—see **`docs/axis-category-time-cookbook.md`**)
- `xTickLabelOverlapStrategy` — `HideOverlapping` (default) or `StaggerAdjacent` for a second row of X tick labels when labels would overlap horizontally
- `minVisibleXTickLabels` / `minVisibleYTickLabels` — optional soft minimum visible tick labels after overlap resolution and canvas clipping (best-effort; X minimum is honored only for `HideOverlapping`)
- `raiseTickDensityCapFromMeasuredLabelWidth` — when true, raises the effective tick-density cap using measured average label width plus the axis gap (narrow labels can allow more ticks; does not force extra thinning when labels are wide)
- `tickCount`, `tickMin` / `tickMax`, `tickDensity`, grid and label-gap tuning as documented in cookbook

`LineChartSpec` now supports:
- `accessibility: ChartAccessibilitySpec` (`contentDescription`, `exposeSelectionState`, `requestFocusForAccessibility`, `keyboardViewportNudgeEnabled`, `keyboardSelectionRoamEnabled`, `requestFocusOnMarker`, `selectionLiveRegionMode`)
- `valueFormatter: (ChartPoint) -> String`
- `selectionStyle: SelectionStyle`
- `referenceLines: List<ReferenceLine>` — optional horizontal/vertical threshold lines drawn under the series (see **Reference lines** below)

`LineSeries` now groups visual styling with:
- `lineStyle: LineStyle` — stroke plus optional **area / threshold-band fill** (`areaFill`, optional `areaFillEnd` vertical gradient end, and `areaFillMode` = `ToPlotBottom` / `ToBaseline(value)` / `BelowThreshold(value)`; see **Area fill and reference lines** below)
- `pointStyle: PointStyle` — point visibility, radius, optional color, and `shape: MarkerShape` (see **Marker shapes and per-point styling** below)

`LineChartSpec.lineRender` (`LineRenderSpec`) controls viewport windowing (domain **x and y** ranges, continuity padding, optional decimation caps) before pixel-space polyline clipping to `plotBounds`. Set an explicit positive `maxPointsPerSeries` to cap vertices, or enable `usePlotWidthPixelBudget` (with `maxPointsPerSeries <= 0`) so `LineChart` supplies a cap from plot width via `LineRenderSpec.autoMaxPointsForPlotWidthPx` (default oversample `DEFAULT_PLOT_WIDTH_OVERSAMPLE`, ~2.25 samples per horizontal pixel before clamping to 256–16384). `LineDecimationStrategy` chooses uniform index spacing vs per-bucket min/max **y** retention (better for spiky data).

**Missing-data gaps:** mark a missing sample with `ChartPoint.gap(x)` (a non-finite y). By default the line and area fill **break** into separate segments at each gap; set `LineRenderSpec.connectAcrossGaps = true` to skip the holes and **connect** the surrounding points (`connectNulls`). Gap points are excluded from data bounds, decimation, and selection.

**Data labels** (`dataLabels: DataLabelsSpec` on `LineChartSpec` / `ScatterChartSpec` / `BarChartSpec`): opt-in per-point / per-bar value labels. `DataLabelsSpec(enabled, valueFormatter, placement, color, offsetPx, avoidOverlap)` — `placement` is `Above` (default) / `Below` / `Left` / `Right` / `Center`; when `avoidOverlap` is on, labels whose boxes would collide or leave the plot are dropped so the rest stay legible.

**Interactive legend / series visibility** (`hiddenSeriesIds: Set<String>` on `LineChartSpec` / `ScatterChartSpec` / `BarChartSpec`): ids listed here are excluded from drawing but still occupy a **dimmed** legend row. `LineChart` / `ScatterChart` / `BarChart` take an optional `onLegendItemClick: (seriesId) -> Unit`; the composable hit-tests taps on legend rows and invokes it, so an app hoisting a `Set<String>` state gets tap-to-toggle series visibility (immutable-spec friendly — rebuild the spec with the updated set).

**Annotations layer** (`annotations: List<ChartAnnotation>` on `LineChartSpec` / `ScatterChartSpec` / `BarChartSpec`): freeform marks anchored in **domain space**, drawn over the series (and above reference lines), clipped to the plot. Variants: `ChartAnnotation.Point(x, y, label?…)` (marker + label), `ChartAnnotation.Text(x, y, text…)`, `ChartAnnotation.Box(xRange?, yRange?, fillColor?, borderColor?, label?)` (a shaded region — a `null` range spans the full plot on that axis, so a `yRange`-only box is a horizontal band), and `ChartAnnotation.Segment(x1, y1, x2, y2…)` (arbitrary line). `ReferenceLine` remains the dedicated helper for full-width/height threshold lines.

### Area fill, reference lines, and marker shapes
These additive options (1.1.0) extend existing cartesian charts; defaults preserve prior stroke/point behavior.

**Area / threshold-band fill** (`LineStyle`):
- `areaFill: ChartColor?` — fill color under the line (null = stroke only, prior behavior)
- `areaFillEnd: ChartColor?` — optional vertical-gradient end color (paired with `areaFill`)
- `areaFillMode: AreaFillMode` — `ToPlotBottom` (default; fill to plot bottom), `ToBaseline(value)` (fill between the line and a baseline value, e.g. `0.0`), or `BelowThreshold(value)` (fill only where the line is below `value` — a shortfall band)

**Reference / threshold lines** (`ReferenceLine`): `orientation: ReferenceOrientation` (`Horizontal` / `Vertical`), `value`, `color`, `strokeWidth`, optional `dashIntervals` (null = solid), and optional `label`. Supplied via `referenceLines: List<ReferenceLine>` on `LineChartSpec`, `BarChartSpec`, `ScatterChartSpec`, and `CombinedCartesianChartSpec`; the planner maps each `value` to pixels and draws lines under the series so they track pan/zoom.

**Marker shapes + per-point styling**: `MarkerShape` = `Circle` / `Square` / `Triangle` / `Diamond`. Set a series default via `PointStyle.shape`; override per point with `ChartPoint.markerShape`, `ChartPoint.markerColor`, and `ChartPoint.markerRadius` (each null falls back to the series `PointStyle`). On `ScatterChart`, a per-point `markerRadius` override drives **bubble** charts (point size encodes a third dimension).

### Scatter chart
Main types:
- `ScatterChartSpec`
- `ScatterSeries`
- `ChartPoint`
- `AxisSpec`
- `GridSpec`
- `LegendSpec`

`ScatterChartSpec` supports:
- `scatterRender: ScatterRenderSpec` — optional cap after **domain-box** pre-filter (`ChartViewport` x/y ranges), then `LineDecimationStrategy` downsampling; `usePlotWidthPixelBudget` + `maxPointsPerSeries <= 0` lets `ScatterChart` derive a cap from plot width (same helper as line). **Default** (`maxPointsPerSeries == 0` and no budget): every series point is considered; a point is omitted only when its marker disc does **not** intersect the plot `RectD` (partial edge discs still draw). With a positive cap, near-edge domain-outside discs may be omitted if the point is not in the domain-visible subset.
- `accessibility: ChartAccessibilitySpec` (same semantics as line/bar, including keyboard shortcuts, live-region policy, and optional `requestFocusOnMarker`)
- `valueFormatter: (ChartPoint) -> String` (same `LineValueFormatter` / defaults as `LineChartSpec`)
- `selectionStyle: SelectionStyle`
- `referenceLines: List<ReferenceLine>` — optional horizontal/vertical threshold lines (see **Area fill, reference lines, and marker shapes**)
- The planner emits **circles only** (no connecting segments). The same disc test is used for line-series vertex markers.

`ScatterSeries` carries:
- `pointStyle: PointStyle` (visibility, radius, optional color, `shape: MarkerShape`; legend uses `pointStyle.color` or a default when null)
- Per-point overrides via `ChartPoint.markerShape` / `markerColor` / `markerRadius`; a per-point `markerRadius` enables **bubble** charts (point size encodes magnitude)

Core planning:
- `ChartPlanner.planScatterChart(...)` → `ScatterChartPlan` (`layout: ChartLayoutPlan`, `selection`)
- Hit-testing uses `ScatterPlotPoints` + `NearestPointResolver` on the **same** candidate set as rendering (decimated when a cap is active).

### Combined cartesian (multi-layer)
Main types:
- `CombinedCartesianChartSpec` — ordered `layers: List<CombinedCartesianLayer>` (`Line` / `Bar` / `Scatter` variants), shared `xAxis` / `yAxis`, optional `yAxisSecondary` when any layer uses `CombinedCartesianYAxisBinding.SecondaryY`, `lineRender` (line layers), `scatterRender` (scatter layers), `valueFormatter` / `barValueFormatter`, `grid`, `legend`, `selectionStyle`, `contentPadding`, `referenceLines: List<ReferenceLine>`, `accessibility: ChartAccessibilitySpec`.
- `CombinedCartesianChartPlan` — `layout: ChartLayoutPlan`, `yTicksSecondary`, `secondaryViewport` (domain window for secondary *Y*), `allBarSegments`, `hitTestLayers`, `selection` echo. Right-axis tick **`DrawCommand.Text`** uses `isSecondaryYAxisTick` so renderers apply **`yAxisSecondary.labelStyle`** (not the primary *Y* tick style slot).
- `ChartPlanner.planCombinedCartesianChart(input, spec, selection, reserveRightYAxisSpace)` — merges grid + layers in list order (earlier layers draw beneath later layers); appends line-style selection overlay when the selection is not a bar match.

Selection: `HighlightTarget.layerId` identifies the layer for combined charts (standalone charts leave it `null`). Hit-testing: `CombinedCartesianHitResolver` (top layer first).

### Pie / donut
Main types:
- `PieChartSlice` — `value`, `label`, `color` (positive `value`s only get arc span).
- `PieChartSpec` — `slices`, optional `holeRadiusRatio` (`null` / `0` = pie; `(0..1)` = donut hole as a fraction of the outer radius), optional **`startAngleDegrees`** (default 12 o’clock) and **`sliceGapDegrees`** (empty space between adjacent positive slices; coerced to fit the circle), `LegendSpec`, `SelectionStyle`, `sliceValueFormatter`, optional `sliceLabels` (`PieSliceLabelsSpec`: canvas leaders + labels; off by default), `id` for selection matching (`HighlightTarget.seriesId` / `pointIndex` = slice index).
- `PieChartPlan` — `layout: ChartLayoutPlan` (empty axis ticks and no axis overlay), `selection` echo.
- `ChartPlanner.planPieChart(input, spec, selection)` emits one filled `DrawCommand.Arc` per positive slice; donut slices set `Arc.innerRadius`. Matching selection adds a translucent overlay `DrawCommand.Group`. When `sliceLabels.enabled`, also emits `DrawCommand.Polyline` + `DrawCommand.Text` per eligible slice (measurement keys: slice index in `ChartTextMeasurementInput.pieSliceLabelMeasurements`; `PieChart` passes the spec into `measuredLayoutInput` so Compose measures strings).
- `PieChartLayoutMath` / `PieRadialLayout` — shared center and radii for planning and hit-testing.
- `PieHitResolver.resolve(touch, spec, plotBounds)` → `ChartSelection?` (inside hole or outside ring → `null`).

Compose:
- `PieChart` wires two-pass measured planning (including pie slice label measurement when enabled), canvas render (`PieChartRenderer`), tap selection, marker (`buildPieMarkerData`), a11y (`pieChartAccessibility`; when `sliceLabels.enabled`, default chart summary includes slice percentages), and no-data (`ChartNoDataDefaults.pieChart()`).

### Radar / spider
Main types:
- `RadarChartSpec` — `axes: List<String>` (ordered spokes), `series: List<RadarSeries>`, `id`, `startAngleDegrees` (axis 0 rotation; 12 o'clock default), `grid: RadarGridSpec`, `axisLabelStyle`, `accessibility: ChartAccessibilitySpec`, `legend`, `selectionStyle`, `contentPadding`, `axisValueFormatter: (axisLabel, value) -> String`.
- `RadarSeries` — `id`, `label`, `values: List<Double>` (aligned to `axes` order), `lineStyle`, `pointStyle`, optional polygon `fillColor`.
- `RadarGridSpec` — `enabled`, `ringCount` (polygon rings between center and outer radius), and grid styling.

Radial multi-series polygon chart; **pan/zoom/fling default off** (`PieDefault`). Selection uses `RadarSeries.id` as `seriesId` and the axis index as `pointIndex`; tap selects the nearest vertex via `RadarHitResolver`. Compose entry: `RadarChart`; a11y via `radarChartAccessibility` (Image role; Shift+Left/Right selection roam when enabled).

### Sankey
Main types:
- `SankeyChartSpec` — `id`, `nodes: List<SankeyNode>`, `links: List<SankeyLink>`, `layout: SankeyLayoutSpec`, `contentPadding`, `legend`, `selectionStyle`, `accessibility: ChartAccessibilitySpec` (validates unique node ids, that every link references a known node, and that link values are finite).
- `SankeyNode` — `id`, `column` (explicit column index; any integer range, normalized by the planner), `label`, optional `color`.
- `SankeyLink` — `sourceNodeId`, `targetNodeId`, `value`, optional `color` (an explicit color wins over `linkColorMode`).
- `SankeyLayoutSpec` — `orientation` (`SankeyOrientation`), per-column node ordering and cross-axis alignment (`columnAlignment`, legacy `columnVerticalAlignment` fallback), per-node outgoing/incoming link ordering for ribbon stacking, `nodePalette` / `linkColorMode` coloring, and optional node / link labels (`SankeyNodeLabelsSpec` / `SankeyLinkLabelsSpec`).
- Layout enums: `SankeyOrientation`, `SankeyNodeOrder`, `SankeyColumnAlignment` (+ legacy `SankeyColumnVerticalAlignment`), `SankeyLinkOrder`, `SankeyNodeLabelPlacement`, `SankeyNodeLabelOrientation`, `SankeyLinkColorMode`.

Explicit-column flow diagram with horizontal or vertical flow; tap selects nodes and links (hit polygons are sampled from the drawn Bézier ribbons); pan/zoom reuse `ChartViewportState` (normalized `0..1` domain by default). Compose entry: `SankeyChart`; a11y via `sankeyChartAccessibility` (Image role, `stateDescription` for the selected node/ribbon, empty-state description).

### Bar chart
Main types:
- `BarChartSpec`
- `StackedBarChartSpec` — same fields as `BarChartSpec` except layout is fixed to stacked (`BarLayoutMode.Stacked` via `toBarChartSpec()`); falls back to independent layout when group entry counts differ (same rule as `BarChartSpec.resolvedBarLayout()`).
- `BarChartOrientation` — `Vertical` (default) or `Horizontal` (category on chart Y, value on chart X; domain `BarEntry` is still `x` = category, `y` = value).
- `BarGroup`
- `BarEntry`
- `AxisSpec`
- `GridSpec`
- `LegendSpec`

`BarChartSpec` now supports:
- `orientation: BarChartOrientation` — bar columns vs rows (`Horizontal` swaps value/category axes in the planner; assign category vs value `AxisSpec` accordingly).
- `barLayout: BarLayoutMode` — `Independent`, `Clustered`, or `Stacked`
- `clusterBarGapPx` — pixel gap between clustered bars in a category band (horizontal gap when `Vertical`, vertical gap when `Horizontal`)
- `accessibility: ChartAccessibilitySpec` (`contentDescription`, `exposeSelectionState`, `requestFocusForAccessibility`, `keyboardViewportNudgeEnabled`, `keyboardSelectionRoamEnabled`, `requestFocusOnMarker`, `selectionLiveRegionMode`)
- `valueFormatter: (BarEntry) -> String`
- `selectionStyle: SelectionStyle`
- `referenceLines: List<ReferenceLine>` — optional horizontal/vertical threshold lines (see **Area fill, reference lines, and marker shapes**)

`BarGroup` now groups visual styling with:
- `style: BarStyle`

Shared grouped style types now include:
- `LineStyle`
- `PointStyle`
- `BarStyle`
- `AxisLabelStyle`
- `AxisLineStyle`
- `GridLineStyle` (`strokeWidth`, optional `strokeStyle` with dash intervals / cap / join for major grid lines)
- `LegendStyle`
- `SelectionStyle`

Additional shared model / style types (1.1.0):
- `MarkerShape` — `Circle` / `Square` / `Triangle` / `Diamond`; series default via `PointStyle.shape`, per-point override on `ChartPoint.markerShape`
- `AreaFillMode` — `ToPlotBottom` / `ToBaseline(value)` / `BelowThreshold(value)`; selected by `LineStyle.areaFillMode` (with `LineStyle.areaFill` / `areaFillEnd`)
- `ReferenceLine` / `ReferenceOrientation` (`Horizontal` / `Vertical`) — added via `referenceLines` on `LineChartSpec` / `BarChartSpec` / `ScatterChartSpec` / `CombinedCartesianChartSpec`

### Candlestick chart
Main types:
- `CandlestickChartSpec` — `series: List<CandlestickSeries>`, `candleRender: CandlestickRenderSpec`, `accessibility: ChartAccessibilitySpec`, `xAxis` / `yAxis` (`AxisSpec`), `valueFormatter` (`CandlestickValueFormatter`), `grid` (`GridSpec`), `legend` (`LegendSpec`), `selectionStyle` (`SelectionStyle`), `contentPadding`.
- `OHLCPoint` — `x`, `open`, `high`, `low`, `close`.
- `CandlestickSeries` — `id`, `label`, `points: List<OHLCPoint>`, `style: CandlestickStyle`.
- `CandlestickStyle` — `bullColor`, `bearColor`, `bodyWidth`, `wickWidth`, `wickColorFollowsBody`, `wickColor`.
- `CandlestickRenderSpec` — `maxCandles` (decimation cap for long series), `viewportContinuityPadding`.

Cartesian family on the shared line/bar/scatter axis + viewport stack: bodies + wicks with bull/bear coloring, **pan/zoom**, **decimation**, and a **tap-to-OHLC** marker. Compose entry: `CandlestickChart`.

**Accessibility / keyboard:** `CandlestickChart` exposes chart-level **Image-role** semantics (default summary lists the series; `stateDescription` reflects the selected candle when `exposeSelectionState` is on), an optional **`accessibilityCustomActions`** parameter, and **keyboard selection-roam** when `ChartAccessibilitySpec.keyboardSelectionRoamEnabled` is on and tap selection is enabled: **Shift+Left/Right** steps between candles within a series in domain (x/time) order, **Shift+Up/Down** switches series, and plain arrows pan (when `keyboardViewportNudgeEnabled`). Roam is backed by the public `KeyboardSelectionRoam.candlestickChartRoam(plan, current, deltaPoint, deltaSeries)`, which walks `plan.candles` by stable global point index (decimation-safe; both axes clamp, no wrap).

### Heatmap
Main types:
- `HeatmapChartSpec` — `cells: List<HeatmapCell>`, `columnLabels` / `rowLabels` (lengths set the grid size), `id`, `colorScale: HeatmapColorScale`, `xAxis` / `yAxis` (`AxisSpec`; always rendered as `AxisScale.Category` — scale field is ignored), `cellGapPx`, `legend` (`LegendSpec`; disabled by default), `selectionStyle`, `contentPadding`, `valueFormatter: (HeatmapCell) -> String`, `accessibility: ChartAccessibilitySpec`.
- `HeatmapCell` — `col` (X), `row` (Y; row 0 at the bottom), `value`; out-of-range indices are silently skipped.
- `HeatmapColorScale` — `minValue`, `maxValue`, `minColor`, `maxColor` (linear interpolation; clamps outside the range). `HeatmapColorScale.auto(cells, minColor, maxColor)` derives the value range from the data.

Core planning:
- `ChartPlanner.planHeatmapChart(input, spec, selection, tickValuePass)` → `HeatmapChartPlan` (`layout: ChartLayoutPlan`, `selection`, `heatmapCells: List<PlannedHeatmapCell>`); `cellsForHitTest()` returns topmost-first order.
- `HeatmapHitResolver` resolves a tap to the covering cell.
- `defaultViewport(HeatmapChartSpec)` spans the column / row counts as category indices.

Compose: `HeatmapChart` wires two-pass measured planning, canvas render (`HeatmapChartRenderer`), tap selection, marker (`buildHeatmapMarkerData`), a11y (`heatmapChartAccessibility`; Image role, optional `accessibilityCustomActions`), and no-data handling. Pan/zoom reuse `ChartViewportState`; there is no keyboard selection-roam for heatmaps.

### Gauge chart
Main types:
- `GaugeChartSpec` — `progress` (0.0..1.0), `id`, `trackColor`, `progressColor`, `strokeWidthPx`, `roundedCaps`, `startAngleDegrees`, `sweepDegrees` (`360` = full ring; `<360` = arc gauge), `accessibility: ChartAccessibilitySpec`, `contentPadding`.

Radial progress ring: track + progress arc with optional rounded caps and an optional **`centerContent`** composable slot. **Non-interactive** — no viewport/selection/gesture state. Compose entry: `GaugeChart(spec, modifier, centerContent)`.

### Sparkline
`Sparkline` is a thin convenience wrapper over `LineChart` with **axes, grid, and legend off**, zero padding, and **non-interactive** defaults, plus optional area fill (`LineStyle.areaFill`). It reuses `LineChartSpec` / `LineSeries`; there is no separate spec type. Intended for compact inline trend rendering.

## Layout input and font scale
`chart-core` plans charts from `ChartLayoutInput` (`canvasSize`, `viewport`, optional `ChartTextMeasurementInput`, **`labelLayoutScale`** defaulting to `1.0`). Tick label **boxes** come from measured text (Compose two-pass `TextMeasurer`); **`labelLayoutScale`** scales the planner’s fixed gaps and padding (horizontal/vertical tick label spacing, axis offsets, legend spacing) so accessibility font scaling does not squeeze chrome. The bundled chart composables set `labelLayoutScale` from `LocalDensity.current.fontScale`. The planner also hides tick labels whose boxes would extend past the canvas and reserves bottom axis space using measured descender depth when it exceeds a nominal margin. Optional **`AxisSpec.xTickLabelOverlapStrategy.StaggerAdjacent`** alternates a second row of X labels; **`LegendStyle.labelMaxChars`** ellipsizes legend text; optional **`minVisibleXTickLabels` / `minVisibleYTickLabels`** and **`raiseTickDensityCapFromMeasuredLabelWidth`** tune label visibility and tick-density caps (see **`AxisSpec`** KDoc). A full baseline policy for all label paths and tighter iOS/Android measurement parity under stress remain open; see **`docs/roadmap.md`** (layout / text measurement).

## Why Specs Are Immutable
The library is being built to avoid the mutable, setter-heavy API style common in older chart libraries.

The current design goal is:
- app state changes create new specs
- viewport and selection stay explicit as UI state
- render planning stays deterministic

## Viewport State
`ChartViewportState` is the public wrapper around `ViewportState` from `chart-core`.

`ViewportState` re-canonicalizes the **domain** (finite, ordered axes) and sanitizes the stored and incoming viewport on every `pan` / `zoom` / `withViewport` pass so non-finite axis data and aggressive pinch scales clamp predictably against **`ViewportConstraints`**; degenerate point domains stay stable. See **`docs/viewport-configuration.md`**.

It currently provides:
- `viewport`, `constraints`, `axisPolicy`, `lastViewportChangeOrigin` (`Gesture` vs `Programmatic`) for animation snapping
- `updateConstraints(ViewportConstraints)` / `updateAxisPolicy(ViewportAxisPolicy)`
- `reset()`
- `pan(deltaX, deltaY, origin = Gesture|Programmatic)` — optional **origin** tags programmatic vs gesture-driven updates (keyboard/a11y use `Programmatic` for viewport easing)
- `zoom(scaleX, scaleY, pivotX, pivotY, origin = Gesture|Programmatic)`
- `panTo(centerX, centerY)` / `centerOn(x, y)`
- `zoomTo(xSpan, ySpan, centerX, centerY)`
- `fitToData()`

`rememberChartViewportState(...)` accepts optional **constraints** and **axis policy**. See **`docs/viewport-configuration.md`**.

Conceptually:
- `chart-core` owns the math
- `chart-compose` owns the reactive state wrapper

## Selection State
`ChartSelectionState` holds the currently highlighted value.

It currently provides:
- `selection`
- `select(...)`
- `clear()`

Selection objects are currently built from:
- `HighlightTarget` (optional `layerId` for `CombinedCartesianChart`)
- `ChartSelection`

**`HighlightTarget`:** for **bars**, `seriesId` is `BarGroup.id`; `pointIndex` is the category index (entry index within that group); `point` is the bar entry’s domain `(x, y)`. For **combined** charts, `layerId` identifies the `CombinedCartesianLayer` when applicable.

The model covers point-like series, pie slices, and **bar segments** (independent, stacked, and clustered). Range selection and richer overlays remain future work.

**Line** and **scatter** charts use **nearest-point** resolution in pixel space (`NearestPointResolver`); scatter (and combined scatter layers) restrict candidates to the same set of indices the planner renders: marker discs that intersect plot bounds in pixel space (`ScatterPlotPoints.indexedInPlotBounds`, radius-aware). Combined cartesian line/scatter layers use the same `LineRenderPipeline` / `ScatterRenderPipeline` preparation as drawing so taps stay aligned when decimation caps apply. Samples with non-finite domain *x*/*y* are skipped; when two candidates tie on distance, the **lower point index** wins (stable for accessibility and tests).

**Bar** charts use **direct rect** hit testing (`BarHitResolver.resolvePlanned`) on `PlannedBarSegment` bounds — the same pixel rectangles the planner draws (`DrawCommand.Rect`). Hit-test order is **top segment first** (`List<PlannedBarSegment>.forHitTestFromDrawOrder()`, same policy as `BarChartPlan.barSegmentsForHitTest()`). Segments carry `orientation` for selection stroke and marker anchoring. Segments with non-finite rectangle edges or inverted axis alignment (`left` > `right` or `top` > `bottom`) are ignored. **`ChartSelection`:** for vertical bars, `pixelOffset` is top-center of the selection stroke; for horizontal bars, outer value-edge center; `markerAnchor` is typically the bar rect center; `hitBounds` is the segment bounds. Keyboard roam: **Shift+Left/Right** steps categories when `Vertical`; **Shift+Up/Down** when `Horizontal`.

## Marker API
`chart-compose` now supports an optional marker slot for selected values.

Current public marker types:
- `ChartMarkerData`
- `DefaultChartMarker(...)`
- `ChartMarkerCustomCallout` (callout path), `ChartMarkerCalloutLeader.custom(...)` (factory)

Layout helpers (multi-overlay / dense dashboards): `nudgeAxisAlignedRectToAvoidOverlap`, `stackAxisAlignedRectBelow` (`RectD` in chart pixels).

Charts accept:
- `marker: @Composable BoxScope.(ChartMarkerData) -> Unit`

## No-data API
Charts support an optional empty state when all series (line/scatter) or all groups (bar) have no plottable data.

Charts accept:
- `noData: @Composable BoxScope.() -> Unit` — custom slot; **takes precedence** over `noDataUi` when non-null
- `noDataUi: ChartNoDataUi?` — title/message (+ optional `contentDescription`) rendered with `DefaultNoDataOverlay` / `ChartNoDataContent`

Helpers:
- `ChartNoDataDefaults.lineChart()` / `barChart()` / `scatterChart()` / `pieChart()` — preset copy for apps and showcase
- `DefaultNoDataOverlay(...)`, `ChartNoDataContent(...)`

When both `noData` and `noDataUi` are null, a default overlay is used.

## Current Rendering Model
The render model is planner-driven.

Planner output is represented by:
- `AxisTick`
- `LegendEntryPlan`
- `ChartLayoutPlan`
- `DrawCommand`

Current draw command types:
- `Line`
- `Polyline`
- `Rect`
- `Circle`
- `Text` — planner may set **`firstBaselineFromTop`** (from `TextLayoutMetrics`) so `ComposeCanvasAdapter` aligns the rendered baseline to the measurement pass; legend uses the same baseline rule from `LegendEntryPlan.labelMetrics`
- `Path` (stroke or fill; **1.1.0** adds `PathDrawStyle.VerticalGradientFill` for line area / threshold-band gradient fill), `ClipRect`, `Group`, `RoundRect`, `Arc` (see **`roadmap.md`**)

**Chrome:** `ChartChromeTheme.layerCompositingForTranslucentGroups` opts into save-layer rendering for translucent `Group` (see **`style-and-theme.md`**).

This is intentionally small and should stay minimal until the core rendering semantics are stable.

## Gesture Flow
The gesture pipeline currently works like this:

1. Compose pointer input captures taps and transform gestures
2. Tap:
   - uses `ChartTransform`
   - resolves nearest point through `NearestPointResolver` (line/scatter) or bar hit testing (bar)
   - updates `ChartSelectionState`
3. Transform:
   - converts gesture centroid back into domain space
   - computes pan/zoom deltas
   - updates `ChartViewportState`

This means interaction is already state-driven rather than view-object-driven.

Public `ChartGestureConfig` (`chart-compose`) includes tap selection, transform (pan/pinch), per-axis pan/zoom toggles, optional **double-tap zoom in**, optional **double-tap zoom out** (`doubleTapZoomOutScale` < 1), optional **double-tap reset** (`doubleTapResetViewportEnabled`, same as `ChartViewportState.reset()`), and optional **pan fling**. If several double-tap modes are enabled, order is **reset**, then **zoom out**, then **zoom in**. Axis lock from `ViewportAxisPolicy` can disable pan/zoom on a locked axis (see `viewport-configuration.md`).

## Internal Renderer Roles
### `ComposeCanvasAdapter`
Responsible for converting `DrawCommand` values into actual Compose canvas instructions (including baseline-aligned label drawing and optional save-layer `Group` compositing).

### `LineChartRenderer`
Responsible for drawing the current line-chart plan and its legend.

### `ScatterChartRenderer`
Responsible for drawing the current scatter-chart plan and its legend (same `ChartLayoutPlan` / canvas path as line).

### `BarChartRenderer`
Responsible for drawing the current bar-chart plan and its legend.

## Accessibility
`ChartAccessibilitySpec` on the spec supplies `contentDescription`, `exposeSelectionState`, **`requestFocusForAccessibility`**, **`keyboardViewportNudgeEnabled`**, **`keyboardSelectionRoamEnabled`**, **`requestFocusOnMarker`**, and **`selectionLiveRegionMode`** (`Off` / `Polite` / `Assertive` in `chart-core`, mapped to Compose live regions in `chart-compose`). **Cartesian** charts (**line**, **bar**, **scatter**, **combined**, **candlestick**), **pie**, **radar**, **sankey**, and **heatmap** apply **Image** role semantics, optional **`stateDescription`** when a value is selected, optional **`accessibilityCustomActions`**, and **`focusable`** when focus is requested, **`requestFocusOnMarker`** is on, keyboard viewport shortcuts are enabled, or **`keyboardSelectionRoamEnabled`** is on. Descendants are **merged** by default; when **`requestFocusOnMarker`** is true (and `marker` is non-null), the chart node stops merging so the **marker** can be a separate accessibility/focus target; selection changes **request focus** on the marker container (`accessibilitySummary`-style description). The marker container itself uses **unmerged** semantics so **focusable controls inside** custom `marker` content (buttons, fields) participate in Tab and TalkBack traversal. When **`keyboardViewportNudgeEnabled`** is true and transform gestures are on, a focused chart accepts **unmodified arrow-key** pan (10% of visible span per press), **+ / −** zoom at the window center, and **Escape** clears tap selection if tap selection is enabled. When **`keyboardSelectionRoamEnabled`** is true and tap selection is enabled, **Shift+Left/Right** moves the selection along domain-ordered points (line/scatter), along bars in increasing **X** (bar), along a flat **layer order** walk (combined), between candles within a series in domain (x/time) order (**candlestick**, via `KeyboardSelectionRoam.candlestickChartRoam`), or among pie slices; **Shift+Up/Down** switches series on **line**, **scatter**, and **candlestick** multi-series charts. Unmodified arrows still pan when viewport nudge is on. (Heatmaps expose Image-role semantics but no keyboard selection-roam.) Default content descriptions append keyboard, selection-roam, and optional marker-focus hints when those flags are on. See **`docs/accessibility-custom-actions.md`** for composing **`chartViewportAccessibilityActions`** with app actions.

Reduced motion: **`ChartAnimationPolicy.respectSystemReducedMotion`** (default true) combines with platform settings to disable programmatic viewport easing, gesture chase, line data morph, and **marker highlight** transitions (fade-in and fade-out), and to force transition durations to zero when the user prefers reduced animation.

**Gesture vs programmatic:** pinch/drag pan/zoom updates the viewport **target** each frame. By default the **rendered** viewport snaps to that target. **Programmatic** `ChartViewportState` changes can ease when `viewportChangesEnabled` is true. When **`gestureViewportEasingEnabled`** is true, the rendered viewport **chases** the gesture target each frame instead of snapping (via the internal `rememberAnimatedChartViewport`).

Parent layouts can add screen-level semantics (e.g. `ShowcaseApp` and per-chart showcase screens).

## Current limitations
- **Layout:** text and legend still imperfect under stress (long labels, font scale, iOS parity)
- **Gestures:** cartesian **pan/zoom** keyboard shortcuts do not apply to **pie**; **pie** supports **Shift+Left/Right** selection roam when `keyboardSelectionRoamEnabled` is on; iOS gesture parity remains a manual checklist item
- **Marker:** placement and viewport-edge clipping are basic
- **Theming:** style system exists; deeper Material integration and docs are ongoing
- **Animation:** defaults keep motion off (`viewportChangesEnabled` / `gestureViewportEasingEnabled` / `selectionHighlightEnabled` / `dataTransitionEnabled` false); opt in per chart. **`LineChart`** can morph coordinates between specs when structure matches (`canMorphLineSpecs` in **chart-core**).
- **Axes:** linear, category, epoch-ms time, and **log / symlog** scales are supported; locale/time-zone labeling is custom-formatter territory (`CompactUtcAxisLabelFormatter` / `formatEpochMillisUtcYmdHm` for UTC)
- **Multi-axis:** secondary (right) *Y* is supported for `CombinedCartesianChart` as a data-derived axis; it is not yet an independent zoom target in `ChartViewportState` v1
- **Performance:** large-dataset and decimation story not proven

## How To Add New Public API Safely
When adding new public API:

1. Define new immutable data/spec types in `chart-core`
2. Decide whether the feature is chart semantics or Compose behavior
3. Add planner support in `chart-core`
4. Add rendering or state wrappers in `chart-compose`
5. Add a showcase screen
6. Update documentation
