# Architecture

## Purpose
`ComposePlotLib` is a multiplatform charting library built around a separation between:
- platform-agnostic chart semantics and math
- Compose-facing public APIs and rendering
- a single Android showcase sample (the library itself targets Android and iOS; iOS is library-only)

The current implementation is intentionally small, but the architecture is already shaped for extension.

**Roadmap and next steps:** [`roadmap.md`](roadmap.md).

## Module Graph
Current modules:
- `:chart-core`
- `:chart-compose`
- `:samples:android-showcase`
- `:chart-core-benchmarks` (optional JMH tooling module; not shipped)

Dependency direction:

```mermaid
flowchart TD
    chartCore[chart-core] --> chartCompose[chart-compose]
    chartCompose --> androidShowcase[android-showcase]
    chartCore --> chartCoreBenchmarks[chart-core-benchmarks]
```

## Core Design Principle
All chart behavior should flow through a planning pipeline:

```mermaid
flowchart TD
    chartSpec[ChartSpec] --> planner[ChartPlanner]
    viewportState[ChartViewportState] --> planner
    selectionState[ChartSelectionState] --> planner
    planner --> layoutPlan[ChartLayoutPlan]
    layoutPlan --> drawCommands[DrawCommand]
    drawCommands --> composeAdapter[ComposeCanvasAdapter]
    composeAdapter --> canvasOutput[ComposeCanvas]
```

This separation is the most important architectural decision in the codebase.

## `chart-core`
`chart-core` is the non-UI engine.

It currently contains:
- geometry primitives
- numeric ranges
- data-domain types
- viewport and transform math
- visible-range filtering
- selection/highlight logic
- immutable chart models/specs
- layout planning types and draw commands

`chart-core` must stay free of Compose UI types.

### Main packages
- `geometry`
  Shared numeric primitives like `OffsetD`, `RectD`, `SizeD`, `InsetsD`
- `math`
  `FpMath` floating-point helpers (degenerate span checks, absolute and relative near-equality, zoom scale sanitization)
- `range`
  Numeric interval types like `ClosedRangeD`
- `domain`
  Data-space types such as `XyPoint` and `DataDomain`
- `viewport`
  Chart window state and constraints (`ViewportState` canonicalizes domains and sanitizes viewports each update so clamps and span limits stay finite)
- `transform`
  Domain-to-pixel and pixel-to-domain conversion
- `visible`
  Visible range derivation from viewport (line windowing uses the domain **x × y** rectangle; vertical bars use pixel bar width plus a small horizontal slack at plot edges; horizontal bars use vertical band slack; scatter/line vertex markers keep points whose **disc** intersects the plot in pixels via `RectD.intersectsDisc`)
- `selection`
  Highlight target and nearest-point resolution
- `model`
  Immutable chart data like `LineSeries`, `ScatterSeries`, `BarGroup`, `PieChartSlice`, `ChartPoint`
- `spec`
  Immutable chart configuration such as `LineChartSpec`, `ScatterChartSpec`, `BarChartSpec`, `PieChartSpec`, `RadarChartSpec`, `SankeyChartSpec`, `HeatmapChartSpec`, `CandlestickChartSpec`, `BoxPlotChartSpec`, `WaterfallChartSpec`, `GaugeChartSpec`, `AxisSpec`, `ChartAccessibilitySpec` / `ChartLiveRegionMode`; additive 1.1.0 types `ReferenceLine` / `ReferenceOrientation`, `MarkerShape`, `AreaFillMode`; additive 1.2.0 types `ErrorBarsSpec`, `TrendlineSpec`, `CrosshairSpec`
- `layout`
  Planner inputs, outputs, ticks, legend entries, and draw commands

## `chart-compose`
`chart-compose` is the public Compose-facing module.

It currently contains:
- public chart composables: `LineChart`, `BarChart`, `StackedBarChart`, `ScatterChart`, `CombinedCartesianChart`, `PieChart`, `RadarChart`, `SankeyChart`, `HeatmapChart`, `CandlestickChart`, `BoxPlotChart`, `WaterfallChart`, `BubbleChart`, `BulletChart`, `FunnelChart`, `StackedAreaChart`, `RadialBarChart`, `CalendarHeatmapChart`, `TreemapChart`, `SunburstChart`, `CirclePackChart`, `GaugeChart`, `Sparkline`; plus the `ChartDataTable` "view data" composable and `rememberStreamingSeries` streaming helper
- public state types: `ChartViewportState`, `ChartSelectionState`
- helpers: default viewport, gesture config (**`ChartGestureConfig`** + companion presets for hosts / iOS QA), markers + **`ChartMarkerPlacement`** / **`ChartMarkerCalloutLeader`**, no-data, **`ChartAnimationPolicy`** (opt-in programmatic viewport easing, optional gesture chase, marker highlight fade in/out on selection / clear, optional **`LineChart`** data morph), **`MaterialChartPalette`** / **`withMaterialSeriesColors`**-style helpers (`materialChartPalette()`, `Color.toChartColor()`), custom accessibility actions
- internal gesture adapter, fling runtime, accessibility semantics (including optional **marker focus handoff** when `ChartAccessibilitySpec.requestFocusOnMarker` is set on a spec, and optional **Shift+arrow selection roam** when `keyboardSelectionRoamEnabled` is set)
- internal Compose canvas adapter and family renderers
- **`androidMain` / `iosMain`** for small platform probes (e.g. reduced-motion preference)

This module is allowed to depend on Compose types, pointer input, text measurement, and canvas APIs.

## Host Modules
### `samples/android-showcase`
This is the main demo surface and should be treated as the visual verification app.

It currently contains:
- sample data providers
- dedicated chart showcase screens
- a small host that can switch between **line**, **bar**, **scatter**, **pie**, **radar**, **combined**, **sankey**, **heatmap**, **gauge**, **candlestick**, and **domain axes** demos (the showcase has no dedicated Sparkline screen)
- Android **Roborazzi** screenshot tests for regression detection (`verifyRoborazziDebug`)

## Current Rendering Pipeline
The current rendering pipeline is:

1. App builds `LineChartSpec`, `BarChartSpec`, `StackedBarChartSpec`, `ScatterChartSpec`, `PieChartSpec`, `RadarChartSpec`, `SankeyChartSpec`, `HeatmapChartSpec`, `CandlestickChartSpec`, `BoxPlotChartSpec`, `WaterfallChartSpec`, or `GaugeChartSpec`
2. Composable resolves a default viewport if no state is supplied
3. `ChartPlanner` runs on `ChartLayoutInput` (with **`labelLayoutScale`** aligned to host font scale in built-in composables); Compose typically performs a **measured second pass** after `TextMeasurer` fills tick/legend metrics (and **pie slice label** metrics when `PieChartSpec.sliceLabels` is enabled). Cartesian composables reuse **`ChartPlanner.computeCartesianTickValuePass`** (`TickValuePass`) across those passes when the viewport and primary axes are unchanged, then the planner computes:
   - plot bounds
   - axis ticks (overlap suppression + canvas-edge hiding)
   - legend entries
   - draw commands
4. `ComposeCanvasAdapter` draws commands to Compose canvas (baseline-aligned `Text` / legend when planner supplies metrics; optional save-layer `Group` compositing via `ChartChromeTheme`)
5. Gesture handling updates viewport and selection; optional **animated render viewport** eases programmatic `ChartViewportState` changes toward the target, and optionally **chases** gesture-driven targets when **`ChartAnimationPolicy.gestureViewportEasingEnabled`** is true (otherwise gestures snap each frame)
6. Recomposition regenerates the plan and redraws

## Planner passes (`chart-core`)

The planner matches a **multi-pass** shape (measurement is host-driven; core reads `ChartTextMeasurementInput`):

| Stage | Role | Primary API |
|-------|------|-------------|
| Bounds | Canvas size + viewport | `ChartLayoutInput` → `toPlanningBounds()` |
| Tick values | Domain ticks before pixel density | `TickValuePass`, `ChartPlanner.computeCartesianTickValuePass` |
| Chrome | Legend rows + reserved insets for axes | `ChartChromeLayout.kt` (`buildLegendEntries`, `buildReservedRegions`) |
| Final cartesian layout | Plot `RectD`, `ChartTransform`, resolved `AxisTick` lists | `CartesianLayoutPlanner` → `CartesianFrame` |
| Draw plan | `DrawCommand` lists (grid, series, axes, selection) | `ChartPlanner.planLineChart` / `planBarChart` / `planScatterChart` (bubble reuses this) / `planCombinedCartesianChart` / `planSankeyChart` / `planHeatmapChart` / `planCandlestickChart` / `planBoxPlotChart` / `planWaterfallChart` / `planBulletChart` / `planFunnelChart` / `planStackedAreaChart` / `planRadialBarChart` / `planCalendarHeatmapChart` / `planTreemapChart` / `planSunburstChart` / `planCirclePackChart` / `planGaugeChart` |

`planPieChart` uses chrome helpers + radial layout only (empty axis ticks). See **[`roadmap.md`](roadmap.md)** for planner-evolution direction.

## Important Current Constraints
- Text placement still uses some fixed offsets and heuristic optical alignment
- Legend and axis ticks use measurement-assisted layout, but long labels, extreme font scale, and iOS parity remain stress cases
- Draw commands stay a small sealed set (`Line` through `Arc`, plus `Path`, `ClipRect`, `Group`); new paint features should extend this set before ad-hoc canvas code. **1.1.0** added exactly one new draw primitive — **`PathDrawStyle.VerticalGradientFill`** for line area / threshold-band gradient fill; the pipeline is otherwise unchanged.
- The selection model is point-based and sufficient for line/scatter/bar first slices, but not yet generalized
- **Chart families (product order):** scatter, **pie/donut**, and **combined** cartesian (multi-layer) are **shipped**; **1.1.0** adds **heatmap** (color grid), **gauge** (radial progress ring), and **candlestick** (OHLC) families plus a thin **sparkline** wrapper over `LineChart`; see **[`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md)** and [`roadmap.md`](roadmap.md).

## Combined cartesian layers (shipped)
**Combined** charts overlay line, bar, and/or scatter layers with shared *X* and one or two *Y* scales. Planning entry point: **`ChartPlanner.planCombinedCartesianChart`** → **`CombinedCartesianChartPlan`** (wraps a single **`ChartLayoutPlan`** for the adapter; secondary *Y* ticks live on the plan wrapper). Compose: **`CombinedCartesianChart`**. Semantics and extension options: **[`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md)**.

## Why This Architecture Matters
This structure makes it possible to:
- add new chart types without rewriting viewport math
- test transforms and selection without UI
- reuse the same planning layer across Android and iOS
- keep public API declarative instead of mutating hidden chart objects
