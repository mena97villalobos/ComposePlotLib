# ComposePlotLib 1.2.0 — Release notes

A large feature release: **6 new chart families** (13 → 19), first-class **interaction** (linked pan/zoom,
range brush, minimap), **export** (PNG / SVG / CSV / JSON), richer **cartesian depth**, **theming**, and
**accessibility**, plus a full **Sankey rework**.

Upgrading from 1.1.0? See the **[migration guide](migration-1.1-to-1.2.md)** — the release is mostly
additive; the only behavior changes are in Sankey. Full detail lives in **[CHANGELOG](../CHANGELOG.md)**.

---

## New chart families (13 → 19)

| Family | Entry point | Notes |
|---|---|---|
| **Box-and-whisker** | `BoxPlotChart` / `BoxPlotChartSpec` | Q1–Q3 box, median, Tukey whiskers + outliers; `BoxPlotEntry.fromSamples`. Tap + keyboard roam + marker. |
| **Waterfall** | `WaterfallChart` / `WaterfallChartSpec` | Running-total floating bars with connectors; increase / decrease / total coloring. Tap + keyboard roam + marker. |
| **Bubble** | `BubbleChart` / `BubbleChartSpec` | Area-proportional `z` sizing (`BubbleSizeScale`) + size legend; reuses the scatter pipeline. |
| **Bullet graph** | `BulletChart` / `BulletChartSpec` | Linear KPI: qualitative bands + measure bar + target. Read-only. |
| **Funnel** | `FunnelChart` / `FunnelChartSpec` | Stacked centered trapezoids tapering by stage value. Read-only. |
| **Stacked area** | `StackedAreaChart` / `StackedAreaChartSpec` | `AreaStackOffset.Zero` / `Expand` (100%) / `Silhouette` (streamgraph). Cartesian pan/zoom. |
| **Radial bar** | `RadialBarChart` / `RadialBarChartSpec` | Concentric progress rings (track + value arc per ring). Read-only, legend. |

(Plus the **Sankey rework** — see below.)

## Cartesian depth

- **Error bars / uncertainty whiskers** — `ChartPoint.yLow`/`yHigh`, `BarEntry.yLow`/`yHigh`, styled by `ErrorBarsSpec` (line / scatter / vertical bar).
- **Trendline / regression overlays** — `TrendlineSpec` (`Linear` OLS with optional confidence band, or `MovingAverage(window)`); public `core.math.LinearRegression`.
- **Line interpolation** — `LineStyle.interpolation`: stepped (`StepBefore`/`StepAfter`/`StepMiddle`) and shape-preserving `MonotoneCubic`; stroke **and** area fill follow the curve.
- **Reversed axis** — `AxisSpec.inverted` (X right→left, Y top→bottom) across every cartesian family.
- **100% / percent bar stacking** — `BarChartSpec.stackMode = BarStackMode.PercentOfTotal`.

## Interaction & exploration

- **Crosshair / axis-pointer** — `CrosshairSpec` (line / scatter): guidelines + axis-value chips on the selection.
- **Cross-chart sync** — `SyncChartViewports(vararg states, ChartSyncAxis.X/Y/Both)`: `syncId`-style linked pan/zoom.
- **Range brush / box selection** — `LineChart(rangeBrush = ChartRangeBrush(...))` → domain `ChartRangeSelection`; pairs with `zoomTo` for zoom-to-selection.
- **Range selector / minimap** — `ChartRangeSelector(spec, viewportState)`: draggable overview window bound to the shared viewport.

## Export

- **Raster (PNG)** — `rememberChartCaptureController()` + `Modifier.chartCaptureTarget(...)` → `capturePng()` / `captureImageBitmap()`; `expect`/`actual` `ImageBitmap.encodeToChartPng()`.
- **Vector (SVG)** — `chartLayoutPlanToSvg(plan)` (any planned layout, no DOM) + `lineChartToSvg(spec, w, h)`.
- **Data (CSV / JSON)** — `ChartDataTable.toCsv()` / `.toJson()` over the line/scatter/bar data-table builders.

## Theming & color

- **`ChartColor` factories** — `rgb` / `argb` / `hex` / `hsl`, `withAlpha`, channel accessors, `ChartColor.Transparent`.
- **Named palettes** — `ChartPalettes` (Tableau10 / Category10 / ColorblindSafe) + `ColorGradient` (Viridis / Plasma / Turbo / RdBu / BrBG) with `colorAt` / `sample`.
- One new draw primitive: `PathDrawStyle.LinearGradientFill`.

## Accessibility

- **"View data" table** — `ChartDataTable` + `lineChartDataTable` / `scatterChartDataTable` / `barChartDataTable` + the `ChartDataTable(...)` composable (row-level semantics).
- **Auto data narrative** — `lineChartNarrative` / `scatterChartNarrative` / `barChartNarrative`, opt-in via `ChartAccessibilitySpec.includeDataNarrative`.
- **Keyboard selection-roam** parity — box-plot, waterfall, and heatmap grid navigation (`KeyboardSelectionRoam.*`).

## Realtime & performance

- **Streaming helpers** — `StreamingSeriesBuffer` (bounded ring buffer) + `rememberStreamingSeries` / `StreamingSeriesState`.
- **LTTB decimation** — `LineDecimationStrategy.Lttb` (shape-preserving downsampling).

## Sankey rework

Orientation (`SankeyOrientation.Horizontal` / `Vertical`), orientation-neutral `columnAlignment`, Tableau-10
node palette + source→target gradient ribbons (`SankeyLinkColorMode`), adjacent d3-style node labels, and
full parity with the cartesian charts (no-data, double-tap zoom, keyboard nudge, fling, selection
semantics). **The default look changed and a vertical-mirror bug was fixed** — see the
[migration guide](migration-1.1-to-1.2.md) §3.

---

## Install

Pushing a semver tag (`v1.2.0`) publishes `1.2.0` to GitHub Packages via the Release workflow. See
[`../README.md`](../README.md) → Install for coordinates.
