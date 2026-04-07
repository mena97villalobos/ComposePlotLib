# API And State Guide

See also **[`README.md`](README.md)** (documentation index) and **[`production-readiness-next-steps.md`](production-readiness-next-steps.md)** (production gaps).

## Current Public API
The current public surface is intentionally small.

### Charts
- `LineChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)`
- `BarChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)`
- `ScatterChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — point-only cartesian chart; same gesture/marker/no-data/a11y shape as line/bar (`spec` is `ScatterChartSpec`).
- `PieChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — radial pie/donut; **pan/zoom/fling default off** (`ChartTransformGesturePolicy(enabled = false)`). Tap uses `PieHitResolver` in the planner’s `plotBounds`.
- `CombinedCartesianChart(spec, modifier, viewportState, selectionState, gestureConfig, marker, markerPlacement, noData, noDataUi, accessibilityCustomActions, animation, chromeTheme)` — multi-layer cartesian overlay (`CombinedCartesianChartSpec`); tap hit-testing is **top-layer-first** (`CombinedCartesianHitResolver`). **Secondary *Y*** is data-derived in `chart-core` on each plan (min/max of secondary-bound layers; not an independent zoom target in `ChartViewportState` v1). See **`docs/design-combined-cartesian-layers.md`** §3.1.

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
- `defaultViewport(BarChartSpec)`
- `defaultViewport(ScatterChartSpec)`
- `defaultViewport(CombinedCartesianChartSpec)` — primary *X* / **shared *Y*** from layers bound to `CombinedCartesianYAxisBinding.SharedY`; secondary *Y* range is still computed in the planner.
- `defaultViewport(PieChartSpec)` — placeholder `0..1` ranges (layout ignores axes; keeps measurement passes consistent).
- `ChartGestureConfig(...)` — optional **presets** on the companion object: `CartesianDefault` (same as `ChartGestureConfig()` for cartesian charts), `PieDefault` (`PieChart` default), `readOnly()`, `exploreOnly(...)`, `cartesianInteractive(...)` (double-tap zoom-in + optional fling; used by **`ComposePlotApp`** line tab and the Android line showcase), `cartesianInteractiveFullExploration()` (zoom in/out + reset + fling for full device QA). See **`docs/ios-gesture-validation.md`** for iOS QA matrices.

### Shared host (`composeApp`)
- `ComposePlotApp()` in `chart-compose` is the cross-platform milestone demo: **line → bar → scatter → pie → combined → domain axes** (category + epoch-ms time line previews; gestures, selection, marker) used by Android `MainActivity` and the iOS `MainViewController`. Adding a new chart family **must** include a preview here (**`AGENTS.md` → Obligations**). The **Android showcase** adds richer demos—including **Combined (multi-layer)**—and Roborazzi (`ShowcaseApp`).
- Xcode integration and manual checks: `docs/ios-host-runbook.md`.

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
- `scale: AxisScale` — **`Linear`**, **`Category(categories)`** (integer indices → label strings; ticks respect `tickMin`/`tickMax` ∩ viewport), or **`TimeEpochMillis`** (duration-nice ticks in ms; default formatter path uses compact UTC labels; see **`docs/axis-category-time-cookbook.md`**)
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

`LineSeries` now groups visual styling with:
- `lineStyle: LineStyle`
- `pointStyle: PointStyle`

`LineChartSpec.lineRender` (`LineRenderSpec`) controls viewport windowing (domain **x and y** ranges, continuity padding, optional decimation caps) before pixel-space polyline clipping to `plotBounds`. Set an explicit positive `maxPointsPerSeries` to cap vertices, or enable `usePlotWidthPixelBudget` (with `maxPointsPerSeries <= 0`) so `LineChart` supplies a cap from plot width via `LineRenderSpec.autoMaxPointsForPlotWidthPx` (default oversample `DEFAULT_PLOT_WIDTH_OVERSAMPLE`, ~2.25 samples per horizontal pixel before clamping to 256–16384). `LineDecimationStrategy` chooses uniform index spacing vs per-bucket min/max **y** retention (better for spiky data).

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
- The planner emits **circles only** (no connecting segments). The same disc test is used for line-series vertex markers.

`ScatterSeries` carries:
- `pointStyle: PointStyle` (visibility, radius, optional color; legend uses `pointStyle.color` or a default when null)

Core planning:
- `ChartPlanner.planScatterChart(...)` → `ScatterChartPlan` (`layout: ChartLayoutPlan`, `selection`)
- Hit-testing uses `ScatterPlotPoints` + `NearestPointResolver` on the **same** candidate set as rendering (decimated when a cap is active).

### Combined cartesian (multi-layer)
Main types:
- `CombinedCartesianChartSpec` — ordered `layers: List<CombinedCartesianLayer>` (`Line` / `Bar` / `Scatter` variants), shared `xAxis` / `yAxis`, optional `yAxisSecondary` when any layer uses `CombinedCartesianYAxisBinding.SecondaryY`, `lineRender` (line layers), `scatterRender` (scatter layers), `valueFormatter` / `barValueFormatter`, `grid`, `legend`, `selectionStyle`, `contentPadding`, `accessibility: ChartAccessibilitySpec`.
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

### Bar chart
Main types:
- `BarChartSpec`
- `BarGroup`
- `BarEntry`
- `AxisSpec`
- `GridSpec`
- `LegendSpec`

`BarChartSpec` now supports:
- `accessibility: ChartAccessibilitySpec` (`contentDescription`, `exposeSelectionState`, `requestFocusForAccessibility`, `keyboardViewportNudgeEnabled`, `keyboardSelectionRoamEnabled`, `requestFocusOnMarker`, `selectionLiveRegionMode`)
- `valueFormatter: (BarEntry) -> String`
- `selectionStyle: SelectionStyle`

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

## Layout input and font scale
`chart-core` plans charts from `ChartLayoutInput` (`canvasSize`, `viewport`, optional `ChartTextMeasurementInput`, **`labelLayoutScale`** defaulting to `1.0`). Tick label **boxes** come from measured text (Compose two-pass `TextMeasurer`); **`labelLayoutScale`** scales the planner’s fixed gaps and padding (horizontal/vertical tick label spacing, axis offsets, legend spacing) so accessibility font scaling does not squeeze chrome. The bundled chart composables set `labelLayoutScale` from `LocalDensity.current.fontScale`. The planner also hides tick labels whose boxes would extend past the canvas and reserves bottom axis space using measured descender depth when it exceeds a nominal margin. Optional **`AxisSpec.xTickLabelOverlapStrategy.StaggerAdjacent`** alternates a second row of X labels; **`LegendStyle.labelMaxChars`** ellipsizes legend text; optional **`minVisibleXTickLabels` / `minVisibleYTickLabels`** and **`raiseTickDensityCapFromMeasuredLabelWidth`** tune label visibility and tick-density caps (see **`AxisSpec`** KDoc). A full baseline policy for all label paths and tighter iOS/Android measurement parity under stress remain open; see **`docs/production-readiness-next-steps.md`** (layout / text measurement).

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

**Bar** charts use **direct rect** hit testing (`BarHitResolver.resolvePlanned`) on `PlannedBarSegment` bounds — the same pixel rectangles the planner draws (`DrawCommand.Rect`). Hit-test order is **top segment first** (`List<PlannedBarSegment>.forHitTestFromDrawOrder()`, same policy as `BarChartPlan.barSegmentsForHitTest()`). Segments with non-finite rectangle edges or inverted axis alignment (`left` > `right` or `top` > `bottom`) are ignored. **`ChartSelection`:** for bars, `pixelOffset` is the top-center of the selection stroke; `markerAnchor` is typically the bar rect center; `hitBounds` is the segment bounds.

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
- `ChartNoDataDefaults.lineChart()` / `barChart()` / `scatterChart()` — preset copy for apps and showcase
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
- `Path`, `ClipRect`, `Group`, `RoundRect`, `Arc` (see **`production-readiness-next-steps.md`** → Draw commands)

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
`ChartAccessibilitySpec` on the spec supplies `contentDescription`, `exposeSelectionState`, **`requestFocusForAccessibility`**, **`keyboardViewportNudgeEnabled`**, **`keyboardSelectionRoamEnabled`**, **`requestFocusOnMarker`**, and **`selectionLiveRegionMode`** (`Off` / `Polite` / `Assertive` in `chart-core`, mapped to Compose live regions in `chart-compose`). **Cartesian** charts (**line**, **bar**, **scatter**, **combined**) and **pie** apply **Image** role semantics, optional **`stateDescription`** when a value is selected, optional **`accessibilityCustomActions`**, and **`focusable`** when focus is requested, **`requestFocusOnMarker`** is on, keyboard viewport shortcuts are enabled, or **`keyboardSelectionRoamEnabled`** is on. Descendants are **merged** by default; when **`requestFocusOnMarker`** is true (and `marker` is non-null), the chart node stops merging so the **marker** can be a separate accessibility/focus target; selection changes **request focus** on the marker container (`accessibilitySummary`-style description). The marker container itself uses **unmerged** semantics so **focusable controls inside** custom `marker` content (buttons, fields) participate in Tab and TalkBack traversal. When **`keyboardViewportNudgeEnabled`** is true and transform gestures are on, a focused chart accepts **unmodified arrow-key** pan (10% of visible span per press), **+ / −** zoom at the window center, and **Escape** clears tap selection if tap selection is enabled. When **`keyboardSelectionRoamEnabled`** is true and tap selection is enabled, **Shift+Left/Right** moves the selection along domain-ordered points (line/scatter), along bars in increasing **X** (bar), along a flat **layer order** walk (combined), or among pie slices (**Shift+Left/Right**); **Shift+Up/Down** switches series on **line** and **scatter** multi-series charts. Unmodified arrows still pan when viewport nudge is on. Default content descriptions append keyboard, selection-roam, and optional marker-focus hints when those flags are on. See **`docs/accessibility-custom-actions.md`** for composing **`chartViewportAccessibilityActions`** with app actions.

Reduced motion: **`ChartAnimationPolicy.respectSystemReducedMotion`** (default true) combines with platform settings to disable programmatic viewport easing, gesture chase, line data morph, and **marker highlight** transitions (fade-in and fade-out), and to force transition durations to zero when the user prefers reduced animation.

**Gesture vs programmatic:** pinch/drag pan/zoom updates the viewport **target** each frame. By default the **rendered** viewport snaps to that target. **Programmatic** `ChartViewportState` changes can ease when `viewportChangesEnabled` is true. When **`gestureViewportEasingEnabled`** is true, the rendered viewport **chases** the gesture target each frame instead of snapping (see **`rememberAnimatedChartViewport`**).

Parent layouts can add screen-level semantics (e.g. `ShowcaseApp` and per-chart showcase screens).

## Current limitations
- **Layout:** text and legend still imperfect under stress (long labels, font scale, iOS parity)
- **Gestures:** cartesian **pan/zoom** keyboard shortcuts do not apply to **pie**; **pie** supports **Shift+Left/Right** selection roam when `keyboardSelectionRoamEnabled` is on; iOS gesture parity remains a manual checklist item
- **Marker:** placement and viewport-edge clipping are basic
- **Theming:** style system exists; deeper Material integration and docs are ongoing
- **Animation:** defaults keep motion off (`viewportChangesEnabled` / `gestureViewportEasingEnabled` / `selectionHighlightEnabled` / `dataTransitionEnabled` false); opt in per chart. **`LineChart`** can morph coordinates between specs when structure matches (`canMorphLineSpecs` in **chart-core**).
- **Axes:** category and epoch-ms time scales are supported; locale/time-zone labeling is custom-formatter territory (`CompactUtcAxisLabelFormatter` / `formatEpochMillisUtcYmdHm` for UTC)
- **Multi-axis:** no right Y-axis or dual-axis plans yet
- **Performance:** large-dataset and decimation story not proven

## How To Add New Public API Safely
When adding new public API:

1. Define new immutable data/spec types in `chart-core`
2. Decide whether the feature is chart semantics or Compose behavior
3. Add planner support in `chart-core`
4. Add rendering or state wrappers in `chart-compose`
5. Add a showcase screen
6. Update documentation
