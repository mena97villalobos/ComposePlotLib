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

## Domain axes (not a separate chart type)

**`AxisScale.Category`** and **`AxisScale.TimeEpochMillis`** work on line (and other cartesian) specs. See **`axis-category-time-cookbook.md`** and the **Domain axes** tab in **`ComposePlotApp`**.

## Samples

- **`ComposePlotApp`** — minimal cross-platform cycle: line → bar → stacked bar → horizontal bar → scatter → pie → combined → domain axes.
- **`samples/android-showcase`** — richer demos and Roborazzi baselines (Android).
