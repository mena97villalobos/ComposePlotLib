# Chart families overview

Short orienteer for each **shipped** family. Configuration details, types, and gesture defaults are spelled out in **[`api-and-state.md`](api-and-state.md)**.

## Line

Continuous series with optional point markers. Strong **`LineRenderSpec`** controls: domain windowing, optional decimation / plot-width pixel budget, and clipping to plot bounds. Shares cartesian **`AxisSpec`** / **`GridSpec`** / **`LegendSpec`** with bar and scatter.

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

Explicit-column Sankey diagram: **`SankeyChartSpec`** provides nodes with a `column` index and weighted links between node ids. Layout is planned in normalized domain space (commonly `x=0..1`, `y=0..1`) so pan/zoom reuses **`ChartViewportState`**; hit-testing supports node and link selection. **`SankeyLayoutSpec`** adds SankeyMATIC-like controls for node ordering, per-column vertical alignment, deterministic link stacking via outgoing/incoming link ordering, and optional node/link labels.

## Candlestick (OHLC)

Cartesian OHLC chart: **`CandlestickChartSpec`** holds **`CandlestickSeries`** of **`OHLCPoint`** (`x`, `open`, `high`, `low`, `close`) with per-series **`CandlestickStyle`** (bull/bear colors, body/wick widths, wick coloring). Shares the line/bar/scatter axis + viewport stack: bodies + wicks, bull/bear coloring, **`CandlestickRenderSpec`** decimation for long series, pan/zoom, and a **tap-to-OHLC** marker. *(Library shipped in 1.1.0; app adoption is gated on an OHLC data feed.)*

## Gauge / progress-ring

Radial progress ring: **`GaugeChartSpec`** (`progress` 0..1, `trackColor`, `progressColor`, `strokeWidthPx`, `roundedCaps`, `startAngleDegrees`, `sweepDegrees`) draws a track + progress arc with an optional **`centerContent`** composable slot. **Non-interactive** — no pan/zoom/selection. *(Shipped in 1.1.0.)*

## Sparkline

A thin convenience wrapper over **`LineChart`** with axes, grid, and legend off, zero padding, and non-interactive defaults; optional area fill. Reuses `LineChartSpec` / `LineSeries` (no separate spec type). *(Shipped in 1.1.0.)*

## Domain axes (not a separate chart type)

**`AxisScale.Category`** and **`AxisScale.TimeEpochMillis`** work on line (and other cartesian) specs. See **`axis-category-time-cookbook.md`** and the **Domain axes** tab in **`ComposePlotApp`**.

## Samples

- **`ComposePlotApp`** — minimal cross-platform cycle: line → bar → stacked bar → horizontal bar → scatter → pie → radar → combined → sankey → candlestick → gauge → sparkline → domain axes.
- **`samples/android-showcase`** — richer demos and Roborazzi baselines (Android).
