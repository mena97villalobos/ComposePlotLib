# Public API release checklist

Use this before tagging a release or publishing artifacts. Canonical API narrative lives in **[`api-and-state.md`](api-and-state.md)**; this page is the **gate** (what to verify, what is intentionally supported).

## Module boundaries

- [ ] **`chart-core`** contains **no** Compose types (`Modifier`, `Color`, `Dp`, `Canvas`, `Path`, …).
- [ ] Planning math, specs, selection hit resolvers, and `DrawCommand` generation stay in **`chart-core`**.
- [ ] **`chart-compose`** owns composables, gesture adapters, canvas rendering, and accessibility wiring on top of plans.

## Intended public surface (`chart-compose`)

Treat these as the **supported** app-facing API (names may grow; update this list when you add types):

- **Charts:** `LineChart`, `BarChart`, `ScatterChart`, `PieChart`, `CombinedCartesianChart`, `ComposePlotApp`
- **State:** `ChartViewportState`, `ChartSelectionState`, `rememberChartViewportState`, `rememberChartSelectionState`, `ViewportChangeOrigin`
- **Viewport helpers:** `defaultViewport(...)` overloads (`chart/ChartViewports.kt`)
- **Gestures:** `ChartGestureConfig`, presets on its companion (`cartesianInteractive`, `cartesianInteractiveFullExploration`, …)
- **Animation:** `ChartAnimationPolicy`
- **Chrome / theme:** `ChartChromeTheme`, `materialChartPalette()`, `MaterialChartPalette`, `Color.toChartColor()`, `ColorScheme.toMaterialChartPalette()`, `seriesAccentColors()`, `LineChartSpec.withMaterialSeriesColors`, `BarChartSpec.withMaterialGroupColors`, `ScatterChartSpec.withMaterialScatterSeriesColors`
- **Markers / no-data:** `ChartMarker`, `ChartMarkerPlacement`, `ChartMarkerCalloutLeader` / `ChartMarkerCustomCallout`, `ChartMarkerData` builders, rect helpers (`nudgeAxisAlignedRectToAvoidOverlap`, `stackAxisAlignedRectBelow`), `ChartNoData*` / `DefaultNoDataOverlay`
- **Accessibility:** `ChartCustomAccessibilityAction`, `chartViewportAccessibilityActions`
- **Planner hook (advanced):** `ChartPlanner.computeCartesianTickValuePass` — documented in **`api-and-state.md`**

Everything under **`.../internal/`** in `chart-compose` is **not** a semver-stable contract unless explicitly promoted.

## Intended public surface (`chart-core`)

Summarize against **[`module-guide.md`](module-guide.md)** and **`api-and-state.md` → Current Spec Model**. At minimum on release:

- [ ] Spec types (`*ChartSpec`, axes, styles, accessibility spec) remain **immutable** data.
- [ ] No new public types drag in UI frameworks.

## Naming and defaults

- [ ] New parameters extend existing charts in the **same order/shape** as siblings (line / bar / scatter / combined) unless a family truly differs (document in **`api-and-state.md`**).
- [ ] Sensible defaults match docs (gestures, pie transform off, optional pie `startAngleDegrees` / `sliceGapDegrees`, combined secondary *Y* data-derived policy).

## Leakage sweeps

- [ ] Grep **`chart-core`** for Compose imports — should be **zero**.
- [ ] Avoid exporting **`internal`** planner-only types from **`chart-compose`** public entry points.

## Cross-platform

- [ ] **`ComposePlotApp()`** cycle on **`composeApp`** and **`iosApp`** matches docs: **line → bar → scatter → pie → combined → domain axes** (**`AGENTS.md`** obligations).
- [ ] **Android showcase** + **Roborazzi** are **Android-only**; iOS validation stays manual (**[`ios-host-runbook.md`](ios-host-runbook.md)**).

## Documentation when the API changes

Update at least **`api-and-state.md`**, **`architecture.md`** if the pipeline moves, **`AGENTS.md`** if obligations shift, **`docs/README.md`** index if you add a new guide, and **`production-readiness-next-steps.md`** if backlog claims become false.
