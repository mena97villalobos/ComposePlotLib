# Module Guide

## Overview
This document explains what belongs in each module and where to implement future work.

## `chart-core`
Path: `chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core`

### Owns
- math primitives
- ranges and data domains
- viewport and transform logic
- visible-range calculations
- selection/highlight models
- immutable data models
- immutable chart specs
- planning output types
- planner logic

### Should not own
- Compose `Color`
- Compose `Modifier`
- Compose `Canvas`
- pointer input handlers
- text measurement APIs
- app navigation

### Files worth knowing
- `layout/ChartPlanner.kt` (line, bar, scatter — `planScatterChart`, shared cartesian frame; **pie** — `planPieChart`; **heatmap** — `planHeatmapChart`; **candlestick** — `planCandlestickChart`; **gauge** — `planGaugeChart`)
- `layout/ChartLayoutPlan.kt`, `layout/LineChartPlan.kt`, `layout/BarChartPlan.kt`, `layout/ScatterChartPlan.kt`, `layout/PieChartPlan.kt`, `layout/PieChartLayoutMath.kt`, `layout/HeatmapChartPlan.kt`, `layout/CandlestickChartPlan.kt`, `layout/GaugeChartPlan.kt`
- `spec/LineChartSpec.kt`, `spec/BarChartSpec.kt`, `spec/ScatterChartSpec.kt`, `spec/PieChartSpec.kt`, `spec/HeatmapChartSpec.kt`, `spec/CandlestickChartSpec.kt`, `spec/GaugeChartSpec.kt`
- `model/ScatterSeries.kt`, `model/PieChartSlice.kt`
- `selection/PieHitResolver.kt`
- `visible/ScatterPlotPoints.kt` (scatter hit-test candidate points)
- `viewport/ViewportState.kt`
- `transform/ChartTransform.kt`
- `selection/NearestPointResolver.kt`

### When to edit `chart-core`
Edit `chart-core` when a change affects:
- coordinates
- layout semantics
- chart geometry
- chart meaning
- selection logic
- reusable configuration

## `chart-compose`
Path: `chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart`

### Owns
- public composables
- public viewport/selection state wrappers
- default viewport helpers
- gesture adapters
- Compose canvas rendering
- internal renderer orchestration

### Public API files (non-exhaustive)
- `LineChart.kt`, `BarChart.kt`, `ScatterChart.kt`, `PieChart.kt`, `RadarChart.kt`, `CombinedCartesianChart.kt`, `SankeyChart.kt`, `HeatmapChart.kt`, `CandlestickChart.kt`, `GaugeChart.kt`, `Sparkline.kt`
- `theme/ChartChromeTheme.kt` (`chartChromeTheme()`, `ChartChromeTheme.Default`); `theme/MaterialChartPalette.kt` (`materialChartPalette()`, `Color.toChartColor()`); `theme/MaterialChartSpecTheme.kt` (`seriesAccentColors`, `withMaterialSeriesColors`, …)
- `ChartGestureConfig.kt`, `ChartMarker.kt`, `ChartNoData.kt`, `ChartAnimationPolicy.kt`, `ChartCustomAccessibilityAction.kt`
- `ChartViewports.kt` (e.g. `defaultViewport`)
- `state/ChartViewportState.kt`, `state/ChartSelectionState.kt`, `state/RememberChartStates.kt`
- `ComposePlotApp.kt` — shared Android/iOS milestone host; **must** include a preview for each shipped chart family (**`AGENTS.md` → Obligations**)

### Platform source sets (expect/actual)
- `androidMain`, `iosMain` — e.g. reduced-motion probe for `ChartAnimationPolicy` (see `internal/accessibility/`)

### Internal implementation files
- `internal/canvas/ComposeCanvasAdapter.kt`
- `internal/gesture/ChartGestureHandler.kt`, `ChartFlingRuntime.kt`, related gesture wiring
- `internal/accessibility/ChartSemantics.kt` and platform motion preference
- `internal/render/LineChartRenderer.kt`, `internal/render/ScatterChartRenderer.kt`, `internal/render/BarChartRenderer.kt`, `internal/render/PieChartRenderer.kt`, `internal/render/HeatmapChartRenderer.kt`, `internal/render/CandlestickChartRenderer.kt`, `internal/render/GaugeChartRenderer.kt`

### When to edit `chart-compose`
Edit `chart-compose` when a change affects:
- the public API shape
- Compose state management
- Compose rendering
- pointer gestures
- how planner output is drawn

## `composeApp`
Path: `composeApp`

### Purpose
A thin host app for the current milestone. It should not become the main showcase or a place for library internals. It hosts **`ComposePlotApp()`**, which must include a preview for **each shipped chart family** (line, bar, stacked bar, horizontal bar, scatter, pie, radar, combined, sankey, heatmap, gauge, candlestick, sparkline, domain axes — see **`AGENTS.md` → Obligations**).

### Good uses
- quickly preview the current chart milestone on Android and iOS (shared `ComposePlotApp` UI)
- keep the shared app entrypoint functional for multiplatform hosting

### Bad uses
- adding library math
- adding long-lived demo logic
- adding host-specific chart behavior that should live in `chart-compose`

## `samples/android-showcase`
Path: `samples/android-showcase`

### Purpose
The main demonstration app and manual verification surface for the library.

### Owns
- curated sample data
- chart-specific showcase screens
- demo navigation (`ShowcaseApp`: line, bar, scatter, pie, radar, combined, sankey, heatmap, gauge, candlestick, domain axes — no dedicated Sparkline screen)
- realistic examples for documentation

### File areas
- `data`
- `screens`
- `ShowcaseApp.kt`

### When to edit `samples/android-showcase`
Edit it when:
- a new public chart feature needs a visual example
- documentation needs a sample screen
- you want a manual verification path for rendering or interaction changes

## Decision Rules
If you are unsure where code belongs:
- If it can be tested without UI and reused by multiple chart types, put it in `chart-core`
- If it depends on Compose runtime or canvas, put it in `chart-compose`
- If it is demo-only, put it in `samples/android-showcase`
- If it is just a current milestone preview, `composeApp` is acceptable
