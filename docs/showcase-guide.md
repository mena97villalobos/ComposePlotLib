# Showcase Guide

## Purpose
The Android showcase app is the main visual verification surface for `ComposePlotLib`.

Path:
- `samples/android-showcase`

## Current Structure
- `ShowcaseApp.kt`
  The root screen that switches between available chart demos (line, bar, scatter, pie, combined, candlestick, gauge, domain axes)
- `data/SampleLineChartData.kt`
  Curated sample data for the line chart
- `data/SampleBarChartData.kt`
  Curated sample data for the bar chart
- `data/SampleScatterChartData.kt`
  Curated sample data for the scatter chart
- `data/SamplePieChartData.kt`
  Curated sample data for pie and donut charts
- `data/SampleDomainAxesCharts.kt`
  Sample line specs for category X and epoch-millis time X
- `screens/LineChartShowcaseScreen.kt`
  End-to-end line-chart demo
- `screens/BarChartShowcaseScreen.kt`
  End-to-end bar-chart demo
- `screens/ScatterChartShowcaseScreen.kt`
  End-to-end scatter-chart demo
- `screens/PieChartShowcaseScreen.kt`
  End-to-end pie and donut demo
- `screens/CombinedChartShowcaseScreen.kt`
  Multi-layer cartesian (bar + line) demo
- `screens/DomainAxesShowcaseScreen.kt`
  Category-scale and epoch-ms time-scale line charts (`AxisScale`)

## Parity with `composeApp`
**`ComposePlotApp`** (in `chart-compose`, used by `composeApp` and `iosApp`) must stay aligned with shipped chart families: extend it whenever you add a new top-level chart composable. See **`AGENTS.md` → Obligations** and update **`docs/*`** accordingly.

## Current showcase goals
The showcase currently proves:
- chart composables can be used from app code
- viewport state can be created and reset externally (`ChartViewportState`; optional constraints/axis policy — see `viewport-configuration.md`)
- selection state is observable to the host
- line, bar, scatter, and pie render through the shared planning pipeline (cartesian families share tick/plot framing; pie is radial)
- **no-data** (`noDataUi` / `ChartNoDataDefaults`) and **accessibility** (descriptions, focus opt-in on styled specs, custom “Reset viewport” actions)
- **Roborazzi** JVM screenshot tests (`recordRoborazziDebug` / `verifyRoborazziDebug`) against goldens under `src/androidUnitTest/resources/roborazzi/`. Small pixel drift from platform font/renderer updates is expected; if verification fails with minor text/layout diffs, re-record (`recordRoborazziDebug`) after confirming the change is intentional.

### Roborazzi flakiness and tolerance (M15)

Goldens are **bitwise-fragile** in principle: any change in Robolectric/Skiko/Compose text shaping, host **OS**, or **JDK** can shift anti-aliasing and label metrics slightly even when app logic is unchanged.

**When to re-record:** after intentional UI/theming changes, or when CI/local failures show **small** consistent diffs (often a few pixels on text or axis ticks) and code review confirms there is no product regression.

**Stability practices:** keep test **DP** sizes and **API level** (`@Config(sdk = [34])`) stable; prefer **`ChartGestureConfig.readOnly()`** for frozen layout shots; avoid animation in selection goldens (`ChartAnimationPolicy` with selection motion off) so frames are deterministic.

**Line-chart scenario tests (M15):** `lineChart_roborazzi_emptyState_defaultOverlay` (default empty overlay), `lineChart_roborazzi_noDataUi_preset` (`ChartNoDataDefaults` preset), `lineChart_roborazzi_selectionHighlighted` (programmatic selection + marker). Fixtures: `samples/android-showcase/src/androidUnitTest/.../RoborazziLineChartScenarios.kt`.
- **Bar / scatter / pie scenario tests (M15):** `barChart_roborazzi_emptyState_defaultOverlay`, `barChart_roborazzi_selectionHighlighted`, `barChart_roborazzi_selectionHighlighted_elevatedFontScale`; `scatterChart_roborazzi_emptyState_defaultOverlay`, `scatterChart_roborazzi_selectionHighlighted`; `pieChart_roborazzi_emptyState_defaultOverlay`, `pieChart_roborazzi_selectionHighlighted`. Fixtures: `RoborazziBarChartScenarios.kt`, `RoborazziScatterChartScenarios.kt`, `RoborazziPieChartScenarios.kt`.
- **Theme matrix (M13):** `ShowcaseRoborazziTest` includes **line** showcase in four width/theme variants — default Material (400×720 dp), **`darkColorScheme()`** (400×720 dp), compact width (320×720 dp), and **elevated font scale** (1.4×, 400×720 dp). **Bar** selection scenarios add a second **elevated font scale** golden (`barChart_roborazzi_selectionHighlighted_elevatedFontScale`). Extend this pattern for other families as needed; keep names stable so golden filenames stay predictable.
- **Combined (M14):** `combinedChartShowcaseScreen` captures the dual-*Y* showcase (secondary axis tick styling); re-record when combined chrome changes.

## How To Add A New Showcase Screen
Recommended process:

1. Add or reuse sample data in `data/`
2. Create a dedicated screen in `screens/`
3. Keep the screen focused on one feature or one chart family
4. Add visible controls for reset, selection, or feature toggles if useful
5. Wire the screen into `ShowcaseApp.kt`

## Good Showcase Screen Characteristics
- Uses deterministic data
- Demonstrates a real public API use case
- Displays state changes visibly
- Can be used for manual QA after chart changes
- Can be referenced in docs later

## What The Showcase Should Not Do
- It should not contain planner or transform logic that belongs in the library
- It should not become the only place that demonstrates features without documentation
- It should not depend on unstable internal APIs where a public API is expected

## Planned showcase expansion
Future screens should include:
- formatter and **category/time-axis** examples (when planner supports them)
- richer tooltip/marker / clipping examples
- styling / theme stress (light, dark, font scale)
- large dataset / performance screen
- dedicated **viewport** demo (constraints, axis lock — partially covered by reset/zoom buttons today)
- new chart families per **`production-readiness-next-steps.md`** (pie/donut shipped — expand demos as needed; **combined** when planner-ready)
