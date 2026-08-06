# Chart Gallery

Visual index of the 26 chart families shipped in ComposePlotLib 1.3.0. Every family follows the same
shape: build an immutable spec type in `chart-core`, hand it to a composable in `chart-compose`.

```kotlin
import com.mena97villalobos.composeplotlib.chart.Sparkline
import com.mena97villalobos.composeplotlib.core.model.ChartPoint

@Composable
fun Trend() {
    Sparkline(
        points = listOf(ChartPoint(0.0, 12.0), ChartPoint(1.0, 18.0), ChartPoint(2.0, 15.0)),
        modifier = Modifier.height(32.dp),
    )
}
```

Every other family takes its spec as the first parameter and a `Modifier` as the second — e.g.
`LineChart(spec = lineChartSpec, modifier = Modifier.fillMaxWidth())`. Which families are
*interactive* is per-family, not per-category: `LineChart`, `BarChart`, `StackedBarChart`,
`ScatterChart`, `BubbleChart`, `CombinedCartesianChart`, `CandlestickChart`, `BoxPlotChart`,
`WaterfallChart`, `PieChart`, `RadarChart`, `SankeyChart` and `HeatmapChart` additionally accept
`ChartViewportState`, `ChartSelectionState`, `ChartGestureConfig` and marker / no-data slots;
`StackedAreaChart` accepts a viewport and gesture config but no selection; the remaining families
take only spec, modifier and `chromeTheme: ChartChromeTheme?` (plus `GaugeChart`'s `centerContent`
slot). See [Getting Started](Getting-Started) for a full first chart and
[Installation](Installation) for dependency setup.

Screenshots below are the Roborazzi baselines from the Android showcase app.

## Cartesian (13)

**`LineChart`** — continuous series over a numeric, category, or time X axis; supports area fill,
decimation, and missing-data gaps. Spec: `LineChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/line-chart.png" width="320">

**`BarChart`** — categorical comparisons as vertical columns or horizontal bars, independent or
clustered. Spec: `BarChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/bar-chart.png" width="320">

**`StackedBarChart`** — part-to-whole comparison per category; a preset over `BarChart` with stacking
enabled. Spec: `StackedBarChartSpec`.

**`StackedAreaChart`** — composition over a continuous X axis, absolute, 100%-expanded, or as a
streamgraph silhouette. Spec: `StackedAreaChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/stackedarea-chart.png" width="320">

**`ScatterChart`** — correlation between two numeric variables, points only, no connecting line.
Spec: `ScatterChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/scatter-chart.png" width="320">

**`BubbleChart`** — a third magnitude dimension on a scatter plot, encoded as area-proportional
bubble size with an optional size legend. Spec: `BubbleChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/bubble-chart.png" width="320">

**`CombinedCartesianChart`** — line, bar, and scatter layers on one canvas with a shared X axis and an
optional secondary Y channel. Spec: `CombinedCartesianChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/combined-chart.png" width="320">

**`CandlestickChart`** — OHLC financial series; candles, hollow candles, OHLC bars, or Heikin-Ashi.
Spec: `CandlestickChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/candlestick-chart.png" width="320">

**`BoxPlotChart`** — five-number distribution summaries per category with whiskers and outliers.
Spec: `BoxPlotChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/boxplot-chart.png" width="320">

**`ViolinChart`** — distribution *shape* per category via mirrored kernel-density curves; the
read-only alternative to a box plot. Spec: `ViolinChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/violin-chart.png" width="320">

**`WaterfallChart`** — how a sequence of increases and decreases builds a running total, with total
markers. Spec: `WaterfallChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/waterfall-chart.png" width="320">

**`BulletChart`** — a single KPI against qualitative bands and a target, in a compact linear strip.
Spec: `BulletChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/bullet-chart.png" width="320">

**`ParallelCoordinatesChart`** — correlations and clusters across many dimensions, one polyline per
record crossing per-dimension axes. Spec: `ParallelCoordinatesChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/parallel-coordinates.png" width="320">

## Radial (6)

**`PieChart`** — part-to-whole for a small number of slices, with optional donut hole and leader-line
labels. Spec: `PieChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/pie-chart.png" width="320">

**`RadarChart`** — multi-series comparison across a fixed set of named axes (spokes).
Spec: `RadarChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/radar-chart.png" width="320">

**`PolarChart`** — directional magnitudes as equal-angle wedges; the classic wind rose.
Spec: `PolarChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/polar-chart.png" width="320">

**`GaugeChart`** — one progress value 0..1 as an arc, with a composable center slot.
Spec: `GaugeChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/gauge-chart.png" width="320">

**`RadialBarChart`** — several progress values as concentric rings. Spec: `RadialBarChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/radialbar-chart.png" width="320">

**`SunburstChart`** — a value hierarchy as concentric rings, one per depth level.
Spec: `SunburstChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/sunburst-chart.png" width="320">

## Hierarchical (2)

**`TreemapChart`** — area-proportional tiling of a flat set of values, squarified for readable aspect
ratios. Spec: `TreemapChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/treemap-chart.png" width="320">

**`CirclePackChart`** — the same value-by-area encoding as a treemap, but as non-overlapping packed
circles. Spec: `CirclePackChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/circlepack-chart.png" width="320">

## Flow (2)

**`SankeyChart`** — weighted flow between nodes arranged in columns, with gradient ribbons and
node/link selection. Spec: `SankeyChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/sankey-chart.png" width="320">

**`FunnelChart`** — drop-off through ordered stages as tapering trapezoids. Spec: `FunnelChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/funnel-chart.png" width="320">

## Matrix (2)

**`HeatmapChart`** — a value per (column, row) cell on two categorical axes, colored by scale.
Spec: `HeatmapChartSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/heatmap-chart.png" width="320">

**`CalendarHeatmapChart`** — daily values in a GitHub-contributions week × weekday grid; callers pass
0-based day offsets, so no date library is required. Spec: `CalendarHeatmapSpec`.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/calendar-heatmap.png" width="320">

## Minimal (1)

**`Sparkline`** — an inline trend for a list row, table cell, or KPI card: no axes, grid, legend, or
gestures. Takes `points: List<ChartPoint>` directly instead of a spec type.

## Full reference

Per-family configuration, spec fields, hit-testing, and gesture defaults live in the canonical repo
doc: [docs/chart-families-overview.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/chart-families-overview.md).
For state, gestures, and the shared composable parameters, see
[docs/api-and-state.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md).
