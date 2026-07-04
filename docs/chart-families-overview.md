# Chart families overview

Short orienteer for each **shipped** family. Configuration details, types, and gesture defaults are spelled out in **[`api-and-state.md`](api-and-state.md)**.

## Line

Continuous series with optional point markers. Strong **`LineRenderSpec`** controls: domain windowing, optional decimation / plot-width pixel budget, clipping to plot bounds, and **missing-data gaps** (`ChartPoint.gap(x)` breaks the line/area; `connectAcrossGaps` bridges them). Shares cartesian **`AxisSpec`** / **`GridSpec`** / **`LegendSpec`** with bar and scatter.

## Bar

Grouped or stacked columns or rows; **`BarChartOrientation`** selects vertical columns vs horizontal bars. **`BarGroup`** + **`BarEntry`** (`x` = category position, `y` = value) with per-group **`BarStyle`**. **`BarLayoutMode`** chooses independent, clustered, or stacked placement. **`StackedBarChart`** / **`StackedBarChartSpec`** are thin presets over **`BarChart`** with stacking enabled when group sizes match. Hit-testing resolves bars in pixel space; selection overlay matches the tapped **`seriesId`** / entry.

## Scatter

Point-only cartesian chart: **`ScatterSeries`** + **`PointStyle`**, no polylines. **`NearestPointResolver`** among drawn points inside the plot. Same marker/gesture/no-data shape as line/bar.

## Combined (multi-layer)

Single canvas overlay of line, bar, and/or scatter layers with shared *X* and shared or secondary *Y*. **`CombinedCartesianChartSpec`** lists ordered layers; **`CombinedCartesianHitResolver`** tests **top layer first**. Secondary *Y* scaling is **data-derived** in the planner; it is **not** an independent zoom channel in **`ChartViewportState`** in v1.

**Design reference:** [`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md).

## Pie / donut

Radial **`PieChartSlice`** list; arcs + optional donut **`holeRadiusRatio`**. Pan/zoom defaults are off (**`ChartTransformGesturePolicy(enabled = false)`**). Optional **`PieSliceLabelsSpec`** adds leader polylines and labels planned in **`chart-core`**. **`PieHitResolver`** for wedge taps.

## Radar / spider

Radial multi-series polygon chart: **`RadarChartSpec`** defines ordered axes (spokes) and one or more **`RadarSeries`** (values aligned to axes). Default grid is a polygon “spider-web” (rings + spokes). Tap selects the nearest vertex via **`RadarHitResolver`**; optional marker uses the same `ChartMarkerData` surface as other families.

## Sankey (flow)

Explicit-column Sankey diagram: **`SankeyChartSpec`** provides nodes with a `column` index (any integer range — indices are normalized) and weighted links between node ids. Layout is planned in normalized domain space (commonly `x=0..1`, `y=0..1`) so pan/zoom reuses **`ChartViewportState`**; hit-testing samples the drawn Bézier ribbons so node and link taps match the visuals. **`SankeyLayoutSpec`** selects the flow direction (**`SankeyOrientation`** `Horizontal` / `Vertical`), applies a **Tableau-10 categorical node palette** (`nodePalette`) with **d3-style source→target gradient ribbons** (**`SankeyLinkColorMode`**, default `SourceToTargetGradient`), and adds SankeyMATIC-like controls for node ordering, cross-axis column alignment (**`SankeyColumnAlignment`**; legacy `columnVerticalAlignment` still maps), deterministic link stacking via outgoing/incoming link ordering, and optional node/link labels — node labels default to d3-style **adjacent** placement (**`SankeyNodeLabelPlacement`**) with inside placement (ellipsized when too long) still available. Node heights follow the d3-sankey scale rule so no column can overflow; selecting a node or ribbon dims the rest of the diagram and outlines the selected element along its actual geometry.

## Heatmap

2-D grid of colored cells: **`HeatmapChartSpec`** holds **`HeatmapCell`** values (`col`, `row`, `value`) with `columnLabels` (X) / `rowLabels` (Y, row 0 at the bottom) and a **`HeatmapColorScale`** (`minValue`/`maxValue` → `minColor`/`maxColor`, or **`HeatmapColorScale.auto(...)`** from the data). Both axes render as categories. Tap selects a cell via **`HeatmapHitResolver`**; pan/zoom reuse **`ChartViewportState`**. Image-role accessibility with optional **`accessibilityCustomActions`** (no keyboard selection-roam). *(Shipped in 1.1.0.)*

## Candlestick (OHLC)

Cartesian OHLC chart: **`CandlestickChartSpec`** holds **`CandlestickSeries`** of **`OHLCPoint`** (`x`, `open`, `high`, `low`, `close`) with per-series **`CandlestickStyle`** (bull/bear colors, body/wick widths, wick coloring). Shares the line/bar/scatter axis + viewport stack: bodies + wicks, bull/bear coloring, **`CandlestickRenderSpec`** decimation for long series, pan/zoom, and a **tap-to-OHLC** marker. Chart-level Image-role accessibility with optional **`accessibilityCustomActions`** and **keyboard selection-roam** (Shift+Left/Right steps candles within a series, Shift+Up/Down switches series, plain arrows pan), backed by **`KeyboardSelectionRoam.candlestickChartRoam`**. **`CandlestickChartSpec.variant`** switches the glyph: **`Candles`** (default), **`HollowCandles`** (bullish bodies outlined), **`OhlcBars`** (open/close ticks), or **`HeikinAshi`** (smoothed — `heikinAshi(points)` is public). *(Shipped in 1.1.0; variants + screenshot tests in 1.2.0.)*

## Box-and-whisker (boxplot)

Cartesian distribution chart: **`BoxPlotChartSpec`** holds **`BoxPlotSeries`** of **`BoxPlotEntry`** (`x`, `min`, `q1`, `median`, `q3`, `max`, `outliers`) with per-series **`BoxPlotStyle`** (box fill/alpha, border, median, whisker, outlier colors + widths). Each category draws the Q1–Q3 box, a median line, whiskers with end caps, and outlier points. **`BoxPlotEntry.fromSamples(x, values, whiskerMultiplier)`** computes the five-number summary (linear-interpolation quartiles + Tukey 1.5·IQR whiskers/outliers). Shares the cartesian axis + viewport stack: pan/zoom, tap **and keyboard selection-roam** (Shift+Left/Right steps entries within a series, Shift+Up/Down switches series — via **`BoxPlotHitResolver`** / **`KeyboardSelectionRoam.boxPlotChartRoam`**), an optional **marker** slot (five-number summary), no-data, and Image-role accessibility with `accessibilityCustomActions`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Waterfall

Cartesian running-total chart: **`WaterfallChartSpec`** holds an ordered list of **`WaterfallEntry`** (`delta`, `isTotal`, `label`). Each step's bar floats from the running total before the step to the total after it — increases, decreases, and **`isTotal`** markers (spanning `0..runningTotal`) are colored distinctly — with optional connector lines. Shares the cartesian axis + viewport stack: pan/zoom, tap **and keyboard selection-roam** (Shift+Left/Right steps the sequence — via **`WaterfallHitResolver`** / **`KeyboardSelectionRoam.waterfallChartRoam`**), an optional **marker** slot (contribution + running total), no-data, and Image-role accessibility with `accessibilityCustomActions`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Bubble

Cartesian magnitude chart: **`BubbleChartSpec`** holds **`BubbleSeries`** of **`BubblePoint`** (`x`, `y`, `z`, optional `color`). Each bubble's **area** scales with `z` via an **area-proportional** **`BubbleSizeScale`** (`radiusFor` / `auto` / `legendTicks`). Rendering, gestures, selection, and markers reuse the scatter pipeline — **`BubbleChartSpec.toScatterSpec()`** projects bubbles to scatter points with per-point `markerRadius`. The `BubbleChart` composable overlays an optional **size legend**. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Bullet

Compact linear KPI display (Stephen Few's bullet graph): **`BulletChartSpec`** holds a featured `value` (measure bar), qualitative range **`BulletBand`**s (background), an optional `target` marker, and a `[domainMin, domainMax]` scale. **Read-only** — no axes/selection/pan-zoom (like the Gauge); optional min/max bound labels. Compose entry: `BulletChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Stacked area

Cartesian composition chart: **`StackedAreaChartSpec`** holds shared **`xValues`** and index-aligned **`StackedAreaSeries`** bands, accumulated per **`AreaStackOffset`** — **`Zero`** (absolute), **`Expand`** (100%), or **`Silhouette`** (streamgraph). Filled bands over axes + grid + legend, with pan/zoom (no per-point selection). Compose entry: `StackedAreaChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Funnel

Non-cartesian stage chart: **`FunnelChartSpec`** holds ordered **`FunnelStage`**s (`label`, `value`, optional `color`), drawn as stacked, centered trapezoids tapering from each stage's width (∝ value, widest = full plot) to the next — the classic conversion funnel. Palette-colored with optional centered labels. **Read-only** (no axes/selection/pan-zoom, like Gauge/Bullet). Compose entry: `FunnelChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Circle-packing / bubble-pack

Read-only enclosure chart: **`CirclePackChartSpec`** holds **`CirclePackNode`** leaves (`label`, `value`, `color`), packed as non-overlapping circles whose **area** encodes value via a greedy tangent packing (`circlePackLayout` + `fitPackedCircles` in `chart-core`), then fit to the plot. Labels show when a circle is large enough. Single-level (flat). **Non-interactive**. Compose entry: `CirclePackChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Sunburst

Read-only hierarchical radial chart: **`SunburstChartSpec`** holds a **`SunburstNode`** tree drawn as concentric rings (one per depth), where each node's angular span is proportional to its value within its parent (`sunburstLayout` in `chart-core`). The root is an optional center disk (`showRoot`); top-level branches take Tableau palette colors and descendants inherit a lighter shade. **Non-interactive**. Compose entry: `SunburstChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Treemap

Read-only area-proportional tiling: **`TreemapChartSpec`** holds **`TreemapNode`** leaves (`label`, `value`, `color`), laid out by a **squarified** algorithm (`squarifiedTreemap` in `chart-core`) that keeps tile aspect ratios close to 1; tiles are filled with the node color and labeled when large enough. Single-level (flat). **Non-interactive**. Compose entry: `TreemapChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Calendar / year heatmap

Read-only GitHub-contribution-style grid: **`CalendarHeatmapSpec`** holds **`CalendarHeatmapDay`** (`dayIndex`, `value`) placed in a week-column × weekday-row grid (`column = (dayIndex + startWeekday) / 7`, `row = … % 7`, row 0 at top) and colored via the shared **`HeatmapColorScale`**. Callers supply 0-based day offsets (KMP-safe — no date library); optional weekday and **`CalendarMonthLabel`** labels sit in the reserved padding. **Non-interactive**. Compose entry: `CalendarHeatmapChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Radial bar / circular progress

Read-only concentric progress rings: **`RadialBarChartSpec`** holds an ordered list of **`RadialBarTrack`** (`label`, `value`, `color`), drawn outermost-first as a background track plus a foreground value arc spanning `sweepDegrees * value / maxValue` (auto-scaled to the largest track when `maxValue` is null). Shares the Gauge/Pie arc geometry; an optional legend labels the rings. **Non-interactive** (no axes/selection/pan-zoom). Compose entry: `RadialBarChart`. *(Shipped in 1.2.0; screenshot-tested in the Android showcase.)*

## Gauge / progress-ring

Radial progress ring: **`GaugeChartSpec`** (`progress` 0..1, `trackColor`, `progressColor`, `strokeWidthPx`, `roundedCaps`, `startAngleDegrees`, `sweepDegrees`) draws a track + progress arc with an optional **`centerContent`** composable slot. **Non-interactive** — no pan/zoom/selection. *(Shipped in 1.1.0.)*

## Sparkline

A thin convenience wrapper over **`LineChart`** with axes, grid, and legend off, zero padding, and non-interactive defaults; optional area fill. Reuses `LineChartSpec` / `LineSeries` (no separate spec type). *(Shipped in 1.1.0.)*

## Domain axes (not a separate chart type)

**`AxisScale.Category`** and **`AxisScale.TimeEpochMillis`** work on line (and other cartesian) specs. See **`axis-category-time-cookbook.md`** and the **Domain axes** screen in the Android showcase.

## Samples

- **`samples/android-showcase`** — the Android demo app: per-family screens (line, bar, stacked bar, horizontal bar, scatter, pie, radar, combined, sankey, heatmap, gauge, candlestick, sparkline via the Line screen, domain axes) with Roborazzi baselines.
