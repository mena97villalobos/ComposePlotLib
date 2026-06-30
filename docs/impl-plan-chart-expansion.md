# Implementation Plan — All Charts in the Library

Status: **In progress** — Library `1.1.0` primitives/charts A1–A7 done (A8 publish pending on a `v1.1.0` tag); Wave 1 B1 + B2 done · Owner: TBD · Created: 2026-06-29 · Updated: 2026-06-29

**Guiding principle: every chart renders through `ComposePlotLib`. No chart is hand-rolled (`Canvas`/`drawArc`/native `Box` bars) in the app.**

Two parts:

- **Part A — `ComposePlotLib`** (private library, repo `mena97villalobos/ComposePlotLib`): grow the library so it can render **100%** of the app's charts — including the ones currently drawn by hand.
- **Part B — `LifeCompanion` app**: once the new library version ships, migrate every remaining custom/native chart onto library chart types, and adopt the library chart types the app never used.

> Discovery context: `ComposePlotLib` 1.0.11 already ships **9 chart types** (Line, Pie/Donut, Bar, StackedBar, Scatter, Radar, Heatmap, Sankey, CombinedCartesian) but the app uses only **2** (`LineChart` + donut `PieChart`). Six app charts are currently rendered **locally** and must be eliminated. To do that, the library needs a handful of new primitives (area fill, reference lines, marker shapes, a gauge/ring) — not just whole new chart types.

---

## Progress tracker

Legend: ✅ done · 🟡 in progress · ⬜ not started · 🔒 blocked

### Library — `ComposePlotLib` → `1.1.0`
- ✅ **A1** Area / threshold-band fill on `LineChart`
- ✅ **A2** Reference / threshold lines
- ✅ **A3** Marker shapes + per-point color
- ✅ **A4** Gauge / progress-ring chart
- ✅ **A5** Bubble (per-point sizing on `ScatterChart`)
- ✅ **A6** Candlestick / OHLC — **library shipped** (bodies/wicks, bull/bear, decimation, tap-to-OHLC); app side **B15** still gated on an OHLC data feed (§6)
- ✅ **A7** Sparkline wrapper
- ⬜ **A8** Publish `1.1.0` to GitHub Packages (triggers on a `v1.1.0` git tag)

### App — cross-cutting prep
- ⬜ Extract `Color.toChartColor()` → shared `ui/finance/ChartTheme.kt` (+ shared axis/grid/legend builders)
- ⬜ Bump `composePlotLib` → `1.1.0` in `gradle/libs.versions.toml`

### App — Wave 1 (no library change; in progress)
- ✅ **B1** Reports income-vs-spend → clustered `BarChart` — `ReportsScreen.kt`
- ✅ **B2** Reports category breakdown → horizontal `BarChart` — `ReportsScreen.kt`
- ⬜ **B3** Budgets spent-vs-budget → clustered horizontal `BarChart`
- ⬜ **B4** Net-worth breakdown → `StackedBarChart`
- ⬜ **B5** Spend/P&L intensity `HeatmapChart` (new analytics tile)
- ⬜ **B6** Money-flow `SankeyChart` (new view)
- ⬜ **B7** Combined bars + line dual-Y (`CombinedCartesianChart`)

### App — Wave 2 (eliminate local charts; needs A1/A2/A3/A4/A7)
- ⬜ **B8** Net-worth line+area migration (A1, A2)
- ⬜ **B9** Net-worth sparkline migration (A7)
- ⬜ **B10** Trade equity-curve migration (A1, A2)
- ⬜ **B11** Forecast migration (A1, A2, A3)
- ⬜ **B12** Goal-ring migration (A4)

### App — Wave 3 (new data-shaped charts)
- ⬜ **B13** Goal-progress line + projection (A1, A2 + data)
- ⬜ **B14** Portfolio holdings bubble (A5)
- 🔒 **B15** Signal candlestick (A6 + OHLC feed)

**Whole-effort acceptance:** no `Canvas(` / `drawArc` / `drawPath` remains in `composeApp/.../ui/finance` chart code.

---

## 0. Architecture boundary (the rule this plan enforces)

| Concern | Lives in | Examples |
|---|---|---|
| **Chart rendering** — Canvas drawing, geometry, scales, axes, gridlines, fills, arcs, markers, gestures, selection, a11y | **`ComposePlotLib` only** | `LineChart`, area fill, floor line, the candlestick wick math, the progress-ring arc |
| **Data mapping** — domain → library `*Spec` | App (thin `*Card` composables) | building `PieChartSlice`s from `state.data.transactions` |
| **Theming** — finance colors → `ChartColor` | App | `financeColors.AccentGreen.toChartColor()` |
| **Card chrome** — surrounding `Card`, titles, legend rows, empty states | App | `SpendingDonutCard` shell |

The library stays **finance-agnostic** — no `FinanceState`/`Loan`/`SavingsGoal` types cross into it. After this plan, **searching the app for `Canvas(`/`drawArc`/`drawPath` in chart code returns nothing.**

> Open to override: if you want the finance chart *cards* (`DashboardCharts.kt`, etc.) physically relocated into the library, that's a different boundary (library would need a generic theming hook to stay domain-free). Say the word and I'll re-scope.

---

## 1. Goals & non-goals

**Goals**
- Make `ComposePlotLib` capable of rendering every chart the app shows.
- Delete all local chart rendering (6 charts: net-worth line+area, net-worth sparkline, equity curve, forecast, goal ring, Reports bars/progress bars).
- Adopt already-shipped library charts the app ignored (Bar/Stacked/Heatmap/Sankey/Combined) and light up new high-value visualizations.

**Non-goals**
- No redesign of the already-library charts (dashboard donut/line, loan charts, portfolio charts) beyond restyling that falls out of new primitives.
- No new market-data backend here beyond flagging it as a hard dependency for candlestick.

---

## 2. The six local charts to eliminate, and what the library needs for each

| # | Local chart (file) | How it renders today | Library capability required |
|---|---|---|---|
| 1 | Net-worth line+area (`NetWorthChart.kt`) | Canvas: line + area fill + dashed zero baseline | **A1** area fill (+ baseline=0) · **A2** reference line |
| 2 | Net-worth sparkline (`NetWorthChart.kt`) | Canvas: line only, tight padding | **A7** sparkline wrapper (axis-less `LineChart`) |
| 3 | Trade equity curve (`TradePnlEquityChart.kt`) | Canvas: line + gradient area + dashed zero | **A1** area fill + gradient · **A2** reference line |
| 4 | Cash-flow forecast (`ForecastChart.kt`) | Canvas: line + shortfall band below floor + dashed floor + 4 shaped/colored event markers | **A1** threshold-band fill · **A2** reference line · **A3** marker shapes + per-point color (via `CombinedCartesian` line+scatter) |
| 5 | Goal progress ring (`GoalDetailScreen.kt`) | Canvas `drawArc`: track + progress arc, rounded caps, center % | **A4** gauge / progress-ring chart |
| 6 | Reports bars + category bars (`ReportsScreen.kt`) | native Compose `Box`/`ProgressBar` | existing `BarChart` (vertical clustered + horizontal) — **no library change** |

So the new library primitives needed are **A1 area/threshold fill**, **A2 reference lines**, **A3 marker shapes**, **A4 gauge/ring**. Plus the previously-identified gaps **A5 bubble** and **A6 candlestick**, and optional **A7 sparkline**.

---

## 3. Library architecture (extension cost reference)

`ComposePlotLib` splits cleanly:
- **`chart-core`** (pure Kotlin): `spec/`, `model/` (`ChartColor`, `ChartPoint`, `*Series`), `layout/` (`ChartPlanner`, `*ChartPlan`), `selection/` (hit resolvers), `visible/` (decimation), `viewport/`.
- **`chart-compose`** (Compose MP): public composables + `internal/render/` (`*Renderer`, `ComposeCanvasAdapter`), `internal/{canvas,gesture,marker,animation,accessibility}`.

Costs:
- **Add a field/mode to an existing chart** → spec/style data class (core) + renderer draw logic (compose); planner only if layout bounds change. Backward-compatible if optional with defaults. *(A1, A2, A3, A5.)*
- **New chart type** → ~7–10 files in `chart-core` (spec, style, model, plan, `ChartPlanner.planX`, hit resolver) + ~5–6 in `chart-compose` (composable, renderer, marker builder, `defaultViewport`). *(A4 gauge, A6 candlestick.)*

All new code must build for `android` + `iosArm64` + `iosSimulatorArm64` (the only published variants).

---

## Part A — `ComposePlotLib` library work

Additive & backward-compatible → ship as **`1.1.0`**.

### A1 — Area / threshold-band fill on `LineChart`  ·  **HIGH**  ·  S–M
Covers charts 1, 3, and the base of 4.
```kotlin
data class LineStyle(
    val color: ChartColor = ChartColor.Blue,
    val strokeWidth: Double = 2.0,
    // NEW (optional; defaults preserve stroke-only behavior)
    val areaFill: ChartColor? = null,        // null = no fill
    val areaFillEnd: ChartColor? = null,      // optional vertical gradient end (alpha-fade look)
    val areaFillMode: AreaFillMode = AreaFillMode.ToPlotBottom,
)
sealed interface AreaFillMode {
    data object ToPlotBottom : AreaFillMode
    data class ToBaseline(val value: Double) : AreaFillMode      // e.g. 0.0 — net worth / equity
    data class BelowThreshold(val value: Double) : AreaFillMode  // fill only where line < value — forecast shortfall band
}
```
**Files:** `chart-core/spec/ChartStyles.kt`; `chart-compose/internal/render/LineChartRenderer.kt` + `internal/canvas/ComposeCanvasAdapter.kt` (build fill path / gradient brush, clip the `BelowThreshold` region).
**Done when:** stroke-only unchanged with defaults; samples for to-zero fill, gradient fill, and below-threshold band render correctly on both platforms.

### A2 — Reference / threshold lines  ·  **HIGH**  ·  S–M
Covers the dashed zero baselines (charts 1, 3) and the forecast floor (chart 4).
```kotlin
data class ReferenceLine(
    val orientation: ReferenceOrientation,   // Horizontal(yValue) or Vertical(xValue)
    val value: Double,
    val color: ChartColor,
    val strokeWidth: Double = 1.0,
    val dashIntervals: List<Double>? = null, // null = solid
    val label: String? = null,
)
// add `val referenceLines: List<ReferenceLine> = emptyList()` to LineChartSpec,
// BarChartSpec, ScatterChartSpec, CombinedCartesianChartSpec
```
**Files:** `chart-core/spec/*Spec.kt` (new field) + plan to map value→pixel; `chart-compose` renderers draw lines (+ optional label) under the series.
**Done when:** a horizontal dashed reference at y=0 and a labeled floor render and track pan/zoom.

### A3 — Marker shapes + per-point color  ·  **HIGH** (forecast)  ·  M
Forecast event markers are circle/square/triangle, colored by kind.
```kotlin
enum class MarkerShape { Circle, Square, Triangle, Diamond }
data class PointStyle(
    val visible: Boolean = true,
    val radius: Double = 3.0,
    val color: ChartColor? = null,
    val shape: MarkerShape = MarkerShape.Circle,   // NEW
)
// allow per-point override: add optional `shape`/`color`/`radius` on the scatter point type
// (null => fall back to series PointStyle) — also unlocks A5 bubble sizing
```
**Files:** `chart-core/spec/ChartStyles.kt` (+ scatter point model for per-point overrides), scatter/line plan + hit resolver; `chart-compose` scatter & line renderers draw the shape.
**Done when:** a scatter renders mixed shapes/colors per point; hit-testing still selects the right marker.

### A4 — Gauge / progress-ring chart  ·  **MED**  ·  M–L (new type)
Replaces the goal `drawArc`; reusable for budget rings and the home/Wear "Budget Ring" widget.
Shipped signature (see §8 and [`api-and-state.md`](api-and-state.md) → Gauge chart for the authoritative form):
```kotlin
data class GaugeChartSpec(
    val progress: Double,                              // 0.0..1.0 (clamped when planned)
    val id: String = "gauge",
    val trackColor: ChartColor = ChartColor.LightGray,
    val progressColor: ChartColor = ChartColor.Blue,
    val strokeWidthPx: Double = 24.0,                  // ring thickness in pixels
    val roundedCaps: Boolean = true,
    val startAngleDegrees: Double = -90.0,             // PieChartLayoutMath.START_ANGLE_DEGREES (12 o'clock)
    val sweepDegrees: Double = 360.0,                  // full ring; <360 = arc gauge
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(/* 24 on each edge */),
)
@Composable fun GaugeChart(spec: GaugeChartSpec, modifier: Modifier = Modifier,
                           centerContent: (@Composable BoxScope.() -> Unit)? = null,
                           chromeTheme: ChartChromeTheme? = null)
```
**Files:** `chart-core` — `spec/GaugeChartSpec.kt`, `layout/GaugeChartPlan.kt`, `ChartPlanner.planGaugeChart`; `chart-compose` — `GaugeChart.kt`, `internal/render/GaugeChartRenderer.kt`.
**Done when:** track + rounded-cap progress arc + center slot render; matches the current goal ring visually on both platforms.

### A5 — Bubble (per-point sizing on `ScatterChart`)  ·  **MED**  ·  M
Reuses the per-point override added in A3 (just the `radius` field). Renderer honors per-point radius; series with none behave as today. *(Portfolio concentration/risk.)*

### A6 — Candlestick / OHLC  ·  **HIGH value, DATA-GATED**  ·  L (new type)
Blocked on data: client has only `.close` (no O/H/L). Build the library piece in parallel; app B-side cannot land until an OHLC feed exists (see §6).
Shipped signature (see §8 and [`api-and-state.md`](api-and-state.md) → Candlestick chart for the authoritative form). `CandlestickStyle` lives on each `CandlestickSeries` (per-series), not on the chart spec:
```kotlin
data class OHLCPoint(val x: Double, val open: Double, val high: Double, val low: Double, val close: Double)
data class CandlestickStyle(val bullColor: ChartColor = ChartColor.Green,
                            val bearColor: ChartColor = ChartColor.Red,
                            val bodyWidth: Double = 8.0, val wickWidth: Double = 1.5,
                            val wickColorFollowsBody: Boolean = true, val wickColor: ChartColor? = null)
data class CandlestickSeries(val id: String, val label: String, val points: List<OHLCPoint>,
                             val style: CandlestickStyle = CandlestickStyle())
data class CandlestickChartSpec(val series: List<CandlestickSeries>,
    val candleRender: CandlestickRenderSpec = CandlestickRenderSpec(),
    val xAxis: AxisSpec = AxisSpec(), val yAxis: AxisSpec = AxisSpec()
    /* + valueFormatter, grid, legend, selectionStyle, accessibility, contentPadding */)
// CandlestickChart(...) also exposes a chart-level accessibilityCustomActions parameter
// and Shift+arrow keyboard selection-roam (KeyboardSelectionRoam.candlestickChartRoam).
```
**Files:** full new-type set (`chart-core`: spec, style, model, plan, planner fn, hit resolver; `chart-compose`: composable, renderer, marker builder, viewport default).
**Done when:** bodies+wicks, bull/bear coloring, decimation for long series, tap-to-OHLC marker; builds on all 3 targets.
> Fallback: Vico (`com.patrykandpatrick.vico:multiplatform`) ships candlestick + full CMP targets — a second dependency for one chart. Prefer our own lib unless time-pressured.

### A7 — `Sparkline` wrapper  ·  **LOW**  ·  S
Thin convenience over `LineChart` (axes/grid/legend off, zero padding, optional `areaFill`). Replaces the net-worth sparkline (chart 2).

### A8 — Release
Bump + publish `chart-core`/`chart-compose` **1.1.0** to GitHub Packages (android + iosArm64 + iosSimulatorArm64). Changelog: area/threshold fill, reference lines, marker shapes, gauge, bubble, candlestick, sparkline.

---

## Part B — App migration (consumes `1.1.0`)

First: bump `composePlotLib` to `1.1.0` in `gradle/libs.versions.toml` (L40). Extract `Color.toChartColor()` from `DashboardCharts.kt:64` into a shared `ui/finance/ChartTheme.kt` (already used by Loan/Portfolio cards; every migrated chart needs it), plus shared `AxisSpec`/`GridSpec`/`LegendSpec` builders for visual consistency.

### Wave 1 — Adopt existing library charts (no library dependency; can start before `1.1.0`)
| ID | Status | Change | Screen / file | Library chart | Size |
|---|---|---|---|---|---|
| **B1** | ✅ | Reports income-vs-spend → clustered bars (delete native `ChartBar` boxes) | `ReportsScreen.kt` | `BarChart` Vertical/Clustered | S |
| **B2** | ✅ | Reports category breakdown → horizontal bars (delete native `ProgressBar`) | `ReportsScreen.kt` | `BarChart` Horizontal | S |
| **B3** | ⬜ | Budgets spent-vs-budget per category | Budgets by-category | `BarChart` Horizontal/Clustered | M |
| **B4** | ⬜ | Net Worth breakdown (assets vs liabilities) | Net Worth → Breakdown | `StackedBarChart` | M |
| **B5** | ⬜ | New spend/P&L intensity heatmap (weekday × week) | Reports / Trading analytics | `HeatmapChart` + `HeatmapColorScale.auto` | M |
| **B6** | ⬜ | New money-flow view (income → categories → savings/debt) | new screen / Reports | `SankeyChart` | M–L |
| **B7** | ⬜ | Combined spend bars + savings/running-balance line, dual-Y | Reports / dashboard | `CombinedCartesianChart` (Bar `SharedY` + Line `SecondaryY`) | M |

Note: B5 does **not** replace the Trading P&L *calendar* (a true calendar widget — keep it); the heatmap is a *new* analytics tile.

### Wave 2 — Eliminate local charts using A1/A2/A3/A4/A7
| ID | Change | File → outcome | Library deps |
|---|---|---|---|
| **B8** | Net-worth line+area → `LineChart` (`areaFill`, `ToBaseline(0.0)`) + zero `ReferenceLine`; **delete custom Canvas** | `NetWorthChart.kt` line chart | A1, A2 |
| **B9** | Net-worth sparkline → `Sparkline`; **delete custom Canvas** | `NetWorthChart.kt` sparkline | A7 (or A1 axis-less) |
| **B10** | Equity curve → `LineChart` (`areaFill`+`areaFillEnd` gradient, sign-based color) + zero `ReferenceLine`; **delete custom Canvas** | `TradePnlEquityChart.kt` | A1, A2 |
| **B11** | Forecast → `CombinedCartesianChart`: Line layer (projection + `BelowThreshold(floor)` fill) + Scatter layer (event markers via `MarkerShape` + per-point color) + floor `ReferenceLine`; **delete custom Canvas** | `ForecastChart.kt` | A1, A2, A3 |
| **B12** | Goal ring → `GaugeChart` (center % via `centerContent`); **delete `drawArc`** | `GoalDetailScreen.kt` | A4 |

After Wave 2, the six local charts are gone. **Acceptance for the whole effort:** no `Canvas(`/`drawArc`/`drawPath` remains in `ui/finance` chart code.

### Wave 3 — New data-shaped charts
| ID | Change | File | Deps |
|---|---|---|---|
| **B13** | Goal-progress line + dashed projection-to-target | `GoalDetailScreen.kt` | A1, A2 + data* |
| **B14** | Portfolio holdings bubble (size=value, color=gain/loss) | `PortfolioCharts.kt` | A5 |
| **B15** | Signal-tab candlestick | `ui/tradesignal/TradeSignalScreen.kt` | A6 **+ OHLC feed** |

\* **B13 data work:** goal balance history isn't a series today (only scalar `goal.saved` + a `GoalContribution` list). Derive a running-balance series in domain (`contributions.sortedBy{date}.scan(0.0){a,c->a+c.amount}`) and expose it on state/`GoalProgress`.

---

## 4. Sequencing & dependency graph
```
Library (1.1.0)                          App
A1 area/threshold fill ─┐
A2 reference lines      ─┤  ── Wave 1 (B1–B7) starts NOW (no lib dep)
A3 marker shapes        ─┤
A4 gauge/ring           ─┤  [bump composePlotLib -> 1.1.0]
A5 bubble               ─┤        │
A6 candlestick (gated)  ─┤  Wave 2 (B8–B12)  needs A1,A2,A3,A4,A7  ← eliminates ALL local charts
A7 sparkline (opt)      ─┤        │
A8 release 1.1.0 ◀──────┘  Wave 3 (B13 A1/A2+data · B14 A5 · B15 A6 + OHLC feed)
```
Value order: **A1+A2 → B8/B10** (kill two custom charts) · **Wave 1 B1–B4** in parallel (cheap) · **A4 → B12** (ring) · **A3 → B11** (forecast, most complex) · **A5 → B14** · **A6 + data → B15** last.

---

## 5. Testing & verification
- **Library:** `chart-core` unit tests (fill geometry incl. below-threshold clipping, reference-line value→pixel, marker hit-testing, gauge sweep math, candlestick body/wick geometry); render smoke on a sample app for all 3 targets.
- **App:** host tests via `:module:testAndroidHostTest` (not `testDebugUnitTest`, which is `:app`-only). New domain derivations (B5/B6 aggregation, B13 running-balance) get `commonTest` tests.
- **Both platforms compile** after each wave; never pipe Gradle output (masks exit codes).
- **detekt:** ≤ 11 functions/file (split chart cards aggressively); for KMP common code the CI `detekt` task is a no-op — verify with `detektCommonMainSourceSet` and eyeball changed files.

---

## 6. Risks & open questions
1. **OHLC data (A6/B15) — hard blocker.** No O/H/L client-side. Decide source: extend the Trade Signal server to return an OHLC history series, or use a market-data API. Until then A6 ships library-only with a sample.
2. **Forecast fidelity (B11)** is the riskiest migration — shortfall band + floor + four marker glyphs. A1 `BelowThreshold` + A2 + A3 should cover it via `CombinedCartesian`; validate against the current look before deleting `ForecastChart.kt`.
3. **iOS targets** — every new primitive must build for `iosArm64` + `iosSimulatorArm64` (no iosX64). Validate gradient fills, gauge arcs, candlesticks on iOS specifically.
4. **Library publishing access** — GitHub Packages needs an authed token; confirm the publish pipeline before depending on `1.1.0`.
5. **Boundary confirmation** — this plan keeps thin app `*Card` composables (data mapping + tokens + Card chrome) and moves only *rendering* into the library. If you want the cards themselves in the library, re-scope (library needs a generic theming hook to stay finance-free).
6. **`toChartColor` extraction** touches `DashboardCharts.kt`/`LoanCharts.kt`/`PortfolioCharts.kt` imports — do it first to avoid churn.

---

## 7. Effort & priority summary
| Item | Status | Part | Priority | Size | Blocking dep |
|---|---|---|---|---|---|
| A1 area/threshold fill | ✅ | Lib | High | S–M | — |
| A2 reference lines | ✅ | Lib | High | S–M | — |
| A3 marker shapes + per-point color | ✅ | Lib | High | M | — |
| A4 gauge / progress ring | ✅ | Lib | Med | M–L | — |
| A5 bubble sizing | ✅ | Lib | Med | M | A3 |
| A6 candlestick | ✅ | Lib | High* | L | library shipped; B15 app gated on OHLC feed |
| A7 sparkline wrapper | ✅ | Lib | Low | S | A1 |
| B1 Reports bars → BarChart | ✅ | App | High | S | — |
| B2 Reports category → BarChart | ✅ | App | High | S | — |
| B3 Budgets spent-vs-budget | ⬜ | App | High | M | — |
| B4 Net-worth stacked breakdown | ⬜ | App | Med | M | — |
| B5 Heatmap (new) | ⬜ | App | Med | M | — |
| B6 Sankey money-flow (new) | ⬜ | App | Med | M–L | — |
| B7 Combined dual-Y | ⬜ | App | Low–Med | M | — |
| B8 Net-worth line+area migration | ⬜ | App | High | M | A1, A2 |
| B9 Net-worth sparkline migration | ⬜ | App | Med | S | A7 |
| B10 Equity-curve migration | ⬜ | App | High | S–M | A1, A2 |
| B11 Forecast migration | ⬜ | App | High | L | A1, A2, A3 |
| B12 Goal-ring migration | ⬜ | App | High | S–M | A4 |
| B13 Goal-progress line (new) | ⬜ | App | Med | M | A1, A2 + data |
| B14 Portfolio bubble | ⬜ | App | Med | M | A5 |
| B15 Signal candlestick | 🔒 | App | High* | M | A6 + OHLC feed |

\* High value, gated on a market-data feed that doesn't exist yet.

---

## 8. Changelog

### 2026-06-29 — Library `1.1.0`: A1–A7 (gauge + candlestick new families)
- **A1** — area / threshold-band fill on `LineChart`: `LineStyle.areaFill`, `LineStyle.areaFillEnd` (vertical gradient), `LineStyle.areaFillMode` (`AreaFillMode.ToPlotBottom` / `ToBaseline(value)` / `BelowThreshold(value)`); rendered via the one new draw primitive **`PathDrawStyle.VerticalGradientFill`**.
- **A2** — `ReferenceLine` (`ReferenceOrientation.Horizontal` / `Vertical`, value, color, strokeWidth, dashIntervals, label) added via `referenceLines: List<ReferenceLine>` on `LineChartSpec`, `BarChartSpec`, `ScatterChartSpec`, and `CombinedCartesianChartSpec`.
- **A3** — `MarkerShape` (`Circle` / `Square` / `Triangle` / `Diamond`) + `PointStyle.shape`; per-point overrides `ChartPoint.markerShape` / `markerColor` / `markerRadius`.
- **A4** — new **gauge / progress-ring** family: `GaugeChartSpec` (`progress`, `id`, `trackColor`, `progressColor`, `strokeWidthPx`, `roundedCaps`, `startAngleDegrees`, `sweepDegrees`, `accessibility`, `contentPadding`), `planGaugeChart`, `GaugeChart` with optional `centerContent` slot; non-interactive.
- **A5** — **bubble** sizing on `ScatterChart` via the per-point `ChartPoint.markerRadius` override (reuses A3).
- **A6** — new **candlestick / OHLC** family (**library only**): `CandlestickChartSpec` (+ `OHLCPoint`, `CandlestickSeries`, `CandlestickStyle` with `bullColor`/`bearColor`/`wickColorFollowsBody`/`wickColor`, `CandlestickRenderSpec`, `CandlestickValueFormatter`), `planCandlestickChart`, `CandlestickChart`; bodies + wicks, bull/bear coloring, decimation, pan/zoom, tap-to-OHLC marker. Chart-level **Image-role accessibility**, an **`accessibilityCustomActions`** parameter (parity with the other cartesian charts), and **Shift+arrow keyboard selection-roam** (Shift+Left/Right steps candles within a series, Shift+Up/Down switches series, plain arrows pan) backed by the public **`KeyboardSelectionRoam.candlestickChartRoam(plan, current, deltaPoint, deltaSeries)`**. App-side **B15** remains gated on an OHLC data feed.
- **A7** — `Sparkline` wrapper over `LineChart` (axes/grid/legend off, zero padding, non-interactive, optional area fill).
- Added `ComposePlotApp` previews, `samples/android-showcase` screens, `chart-core` / Roborazzi tests, and refreshed BCV (`chart-*/api/*.api`) baselines for all of the above. Publish (**A8**) happens on a `v1.1.0` git tag.

### 2026-06-29 — Wave 1: B1 + B2 (uncommitted on `main`)
- **B1** — replaced the hand-rolled native `Box` bars (`MonthColumn`/`ChartBar`) in `ReportsScreen.kt` with a clustered vertical `BarChart` (income green / expense gold, compact-money Y axis, month labels on X, tap tooltip via `DefaultChartMarker`).
- **B2** — replaced the `CategoryRow` `ProgressBar` list with a horizontal `BarChart` — one themed `BarGroup` per category to preserve per-category colors, truncated category labels, tap tooltip; height scales with category count.
- Removed dead code: `MonthColumn`, `ChartBar`, `CategoryRow`, the `CHART_HEIGHT`/`MIN_BAR` consts, the `maxValue`/`maxCat` aggregates, and 4 now-unused imports.
- **Verified:** `:composeApp:compileKotlinIosSimulatorArm64` ✅ + `:composeApp:compileAndroidMain` ✅; `detektCommonMainSourceSet` adds zero new findings (the 3 pre-existing `ReportsScreen.kt` findings live in `MiniStat`, untouched).
- **Reused** the existing `Color.toChartColor()` in `DashboardCharts.kt`; the cross-cutting extraction to `ChartTheme.kt` is still pending.
- **Open tradeoff:** the old category list showed each exact amount inline; the bar chart shows magnitude on the value axis + exact amount on tap. Always-visible per-bar value labels would need a small library addition (end-of-bar labels on `BarChart`).
- **Watch on first run:** the horizontal category chart maps category→`yAxis`, value→`xAxis` (matches the lib's `defaultViewport` for `Horizontal`) — needs a visual confirm.
- **Build-env quirks (not from this change):** `compileCommonMainKotlinMetadata` fails on `iosX64` compose-runtime resolution + a config-cache serialization error; compile the real targets directly with `--no-configuration-cache`.
