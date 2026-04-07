# ComposePlotLib Agent Guide

This file is the working guide for AI agents and future contributors operating inside `ComposePlotLib`.

## Obligations for AI agents and contributors
- **Shared host (`ComposePlotApp`)** — When you add a **new public chart family** (new top-level composable wired to `chart-core` planning), you **must** add a preview tab (or equivalent) in [`chart-compose/.../ComposePlotApp.kt`](chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ComposePlotApp.kt) so **Android `composeApp` and iOS `iosApp`** stay aligned for manual QA. Pie/donut and other non-cartesian families should still get a minimal host preview once they exist.
- **Documentation** — You **must** update documentation to match the **current** behavior and surface area: at minimum **`AGENTS.md`**, **`docs/api-and-state.md`**, **`docs/architecture.md`**, and any other `docs/*.md` file whose claims become false (e.g. `docs/ios-host-runbook.md`, **`docs/module-guide.md`**, **`docs/production-readiness-next-steps.md`**). For releases or broad API changes, walk **`docs/api-checklist.md`** and refresh it if the supported surface changes. Do not leave docs describing fewer charts or hosts than the code ships.

## Project Intent
`ComposePlotLib` is a Kotlin Multiplatform and Compose Multiplatform charting library intended to become a modern successor to MPAndroidChart.

Current priorities:
- Keep `chart-core` platform-agnostic
- Keep public APIs in `chart-compose` declarative and immutable-first
- Use `samples/android-showcase` as the primary demo and verification surface
- Prefer extending the shared planning/render pipeline over adding isolated chart-specific hacks

## Module Responsibilities
- `chart-core`
  Owns non-UI math, geometry, ranges, domains, viewport state, transforms, visible-range logic, selection models, immutable chart specs, and layout planning.
- `chart-compose`
 Owns public Compose APIs (`LineChart`, `BarChart`, `StackedBarChart`, `ScatterChart`, `CombinedCartesianChart`, `PieChart`, `SankeyChart`), Compose-managed interaction state, gesture adapters, and Compose canvas rendering.
- `composeApp`
 Thin shared host for previewing the current milestone; entry **`ComposePlotApp()`** must include **every** shipped chart family (line, bar, stacked bar, horizontal bar, scatter, pie/donut, **combined cartesian**, **sankey**) plus the small **domain axes** demo tab (category, long-label truncation, epoch time, UTC weekday tick policy); extend the cycle when new chart families ship.
- `samples/android-showcase`
  Primary showcase app for feature demos and manual verification.

## Architectural Rules
- Do not introduce Compose UI types into `chart-core`.
  Avoid `Modifier`, `Color`, `Dp`, `Offset`, `Rect`, `Path`, `Canvas`, and `TextStyle` in `chart-core`.
- Keep chart specs immutable.
  New public configuration should prefer new spec/model types over mutable setters.
- Keep interaction state explicit.
  Pan/zoom/selection state should live in `ChartViewportState` and `ChartSelectionState`, not be hidden inside composables.
- Extend the planner before extending the renderer.
  If a new chart feature changes layout or semantics, update `chart-core` planning types first, then adapt rendering in `chart-compose`.
- Prefer shared primitives over chart-specific duplication.
  Reuse geometry, transforms, draw commands, tick generation, and selection logic whenever possible.

## Current Data Flow
The current implementation follows this flow:

1. App code constructs immutable specs such as `LineChartSpec`, `BarChartSpec`, `StackedBarChartSpec`, `ScatterChartSpec`, `CombinedCartesianChartSpec`, or `PieChartSpec`.
2. `chart-compose` creates or receives `ChartViewportState` and `ChartSelectionState`.
3. `ChartPlanner` in `chart-core` converts spec + viewport + selection into a `ChartLayoutPlan` (or `CombinedCartesianChartPlan` wrapping one).
4. `ComposeCanvasAdapter` renders `DrawCommand` values onto a Compose `Canvas`.
5. Gesture handlers update viewport or selection state.

When adding features, fit them into this flow instead of bypassing it.

## Preferred Editing Strategy
- For new shared behavior:
  Start in `chart-core`.
- For public API changes:
  Update `chart-compose` public files first, then internal adapters.
- For a **new chart family** composable:
  Add **`ComposePlotApp`** preview, then sample/screen in **`samples/android-showcase`** and **`ShowcaseApp`**, and update **`docs/*`** (see **Obligations** above).
- For a new showcase-only feature (no new chart family):
  Add sample data, then a dedicated screen in `samples/android-showcase`, then wire it into `ShowcaseApp`.

## Testing Expectations
- Add or update `chart-core` tests for new math, transform, tick-generation, or selection logic.
- **CI** — PR and release workflows (`.github/workflows/pr.yml`, `.github/workflows/release.yml`) run on **Ubuntu** only: `ktlintCheck`, JVM + Android unit tests, Roborazzi, and `:composeApp:assembleDebug`. See **`docs/github-ci-packages-and-pages.md`** for GitHub Packages consumption and Pages setup.
- Build at least:
  `./gradlew :chart-core:testDebugUnitTest :chart-compose:compileDebugKotlinAndroid :samples:android-showcase:verifyRoborazziDebug :composeApp:assembleDebug`
- On **macOS**, when changing shared model or Compose code paths used on iOS, also run:
  `./gradlew :composeApp:compileKotlinIosSimulatorArm64` (and `:composeApp:compileKotlinIosArm64` if you need device slices).
- After intentional UI changes to showcase charts, refresh screenshot baselines with:
  `./gradlew :samples:android-showcase:recordRoborazziDebug`
- Use the showcase app as a manual verification surface for rendering changes; use `docs/ios-host-runbook.md` for the Xcode host.
- **Perf / scale (M8):** scatter decimation and planner invariants are covered by `chart-core` unit tests (`ScatterRenderPipelineTest`, `ChartPlannerTest.scatterChart_decimation_capsCircleCommands`); optional JVM microbenchmarks live in `:chart-core-benchmarks` (`./gradlew :chart-core-benchmarks:jmh --no-configuration-cache`; see `docs/performance-benchmarks.md`). Unit tests are regression checks, not timed benchmarks.
- **Gestures (M10):** `ChartGestureConfig` companion presets (`CartesianDefault`, `PieDefault`, `readOnly`, `exploreOnly`, `cartesianInteractive`, `cartesianInteractiveFullExploration`); iOS touch QA matrices and maintainer log in `docs/ios-gesture-validation.md` (compile gate alone is `./gradlew :composeApp:compileKotlinIosSimulatorArm64`; physical device row still required before release touch QA).

## Known current limitations
- Text/legend layout is measurement-assisted but still imperfect (long labels, font scale, iOS text parity).
- Tick generation defaults to nice **linear** numeric behavior when unspecified; **category** and **epoch-millis time** scales are first-class in `TickValuePlanner` (business-calendar ticks remain app-level). Optional **`AxisSpec.minVisibleXTickLabels` / `minVisibleYTickLabels`** (soft targets after overlap + clip) and **`raiseTickDensityCapFromMeasuredLabelWidth`** (raises density cap from measured label width) are planner-level hooks; see **`AxisSpec`** KDoc and **`docs/api-and-state.md`**.
- Marker/tooltip placement is plot/canvas aware (flip/slide); optional **elbow** or **`ChartMarkerCustomCallout`** on `ChartMarkerPlacement.callout`; **`nudgeAxisAlignedRectToAvoidOverlap`** / **`stackAxisAlignedRectBelow`** for multi-overlay layouts; with **`requestFocusOnMarker`**, marker container uses unmerged semantics so focusables inside custom `marker` content work (showcase: custom callout + button).
- Accessibility: Image role, focus opt-in, custom actions, configurable **selection live region** (`ChartLiveRegionMode`), optional **keyboard viewport** shortcuts on cartesian charts (arrow pan, +/− zoom, Escape clears tap selection), optional **`keyboardSelectionRoamEnabled`** (Shift+arrow roam; Shift+Up/Down switch series on line/scatter), **`chartViewportAccessibilityActions`** and **`chartAccessibilityActionsMergeViewport`**, optional **`ChartAccessibilitySpec.requestFocusOnMarker`** (separate marker focus target; showcase line sample), and reduced-motion integration for chart motion are in place; see **`docs/accessibility-custom-actions.md`**.
- Viewport/selection **animation**: opt-in via `ChartAnimationPolicy` (`viewportChangesEnabled`, `gestureViewportEasingEnabled` + `gestureViewportChaseStiffness`, `selectionHighlightEnabled`, `dataTransitionEnabled` / `dataTransitionDurationMillis` for **line** same-topology morph, durations); programmatic viewport updates can ease the rendered window; optional **gesture chase** eases toward gesture targets each frame when enabled (otherwise gestures snap). Marker highlight can **fade in and out** on selection clear when `selectionHighlightEnabled` and `selectionDurationMillis` > 0. `respectSystemReducedMotion` zeros motion when the user prefers reduced animation.
- iOS uses the same Compose pipeline; automated visual regression is **Android (Roborazzi)** only; device soak is manual (`docs/ios-host-runbook.md`).
- **Large datasets:** line series support `LineRenderSpec` decimation and optional `usePlotWidthPixelBudget` (`LineChart`). Scatter supports `ScatterRenderSpec` + `ScatterRenderPipeline` (domain pre-filter + decimation when capped; Compose pixel budget mirrors line; default path keeps all points then plot-disc culling). Large-series **invariant / smoke** tests live in `ScatterRenderPipelineTest` / `ChartPlannerTest`; JVM **JMH** harness ships in `:chart-core-benchmarks`; **CI** micro-benchmarks remain optional (`docs/performance-benchmarks.md`).
- **Pie / donut:** optional `PieSliceLabelsSpec` draws leader polylines and text from the planner; placement is per-slice with clamping to the plot — not a full collision-avoidance pass across labels.
- **Combined cartesian:** `CombinedCartesianChart` overlays line/bar/scatter layers; **secondary Y** is **data-derived** in the planner (not independently panned/zoomed via `ChartViewportState` in v1). Right-axis tick text uses `yAxisSecondary.labelStyle` via `DrawCommand.Text.isSecondaryYAxisTick` (M14).

## When Adding New Features
- If the feature changes coordinate behavior, update:
  `chart-core/.../viewport`, `transform`, `layout`, or `selection`
- If the feature changes rendering semantics, update:
  `chart-core/.../layout/ChartLayoutPlan.kt`
- If the feature adds new public API, update:
  `chart-compose/.../chart/*.kt` and relevant `state/*`
- If the feature is a **new chart family** composable, update **`ComposePlotApp`** and **docs** (see **Obligations**).
- If the feature needs a visual example, add a screen in:
  `samples/android-showcase/src/commonMain/kotlin/.../screens`

## New chart families
Prefer this order (see `docs/production-readiness-next-steps.md` → **New Chart Families**):

1. **Scatter** — **shipped**; same cartesian pipeline as line/bar (`ScatterChartSpec`, `planScatterChart`, `ScatterChart`).
2. **Pie / donut** — **shipped**; radial planning, `DrawCommand.Arc` (+ optional `innerRadius`), `PieHitResolver`, `PieChart`, hosts/showcase (**`AGENTS.md` → Obligations**).
3. **Combined** — **shipped** (`CombinedCartesianChartSpec`, `planCombinedCartesianChart`, `CombinedCartesianChart`); design reference **`docs/design-combined-cartesian-layers.md`**. Do not layer pie with cartesian until both are solid alone.

## Planner structure (`chart-core`)
Cartesian planning is split for clarity: tick/chrome (`ChartChromeLayout.kt`), final frame (`CartesianLayoutPlanner.kt`, `CartesianFrame`), orchestration + draw commands (`ChartPlanner.kt`). See **`docs/architecture.md`** → *Planner passes*.

## File areas to read first
- `docs/production-readiness-next-steps.md`
- `docs/README.md`, `docs/architecture.md`, `docs/api-and-state.md`, `docs/api-checklist.md`, `docs/viewport-configuration.md`, `docs/style-and-theme.md`, `docs/chart-families-overview.md`
- `chart-core/src/commonMain/kotlin/.../layout/ChartPlanner.kt`
- `chart-core/src/commonMain/kotlin/.../layout/ChartLayoutPlan.kt`
- `chart-core/src/commonMain/kotlin/.../spec/ScatterChartSpec.kt` (with `LineChartSpec` / `BarChartSpec` as needed)
- `chart-core/src/commonMain/kotlin/.../spec/PieChartSpec.kt` (+ `PieSliceLabelsSpec`)
- `chart-compose/src/commonMain/kotlin/.../chart/LineChart.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/BarChart.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/ScatterChart.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/PieChart.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/CombinedCartesianChart.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/internal/canvas/ComposeCanvasAdapter.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/state/ChartViewportState.kt`
- `samples/android-showcase/src/commonMain/kotlin/.../ShowcaseApp.kt`
- `chart-compose/src/commonMain/kotlin/.../chart/ComposePlotApp.kt`
- `chart-compose/src/commonMain/kotlin/.../theme/MaterialChartPalette.kt`, `.../theme/MaterialChartSpecTheme.kt`
- `docs/ios-host-runbook.md`, `docs/ios-library-usage.md`

## Documentation map
- **`docs/README.md`** — index of all documentation files  
- **`docs/ai-agent-full-backlog-prompt.md`** — AI agent system prompt, **residual mission index**, verification  
- **`docs/ai-agent-milestone-prompts.md`** — copy-paste **residual** task blocks (open items only; retired M3→M8, M7, M12, M13, M14, M16)  
- **`docs/production-readiness-next-steps.md`** — **primary backlog** for production readiness  
- **`docs/architecture.md`**, **`docs/api-and-state.md`**, **`docs/api-checklist.md`**, **`docs/viewport-configuration.md`**, **`docs/style-and-theme.md`**, **`docs/marker-tooltip-placement.md`**, **`docs/chart-families-overview.md`**, **`docs/module-guide.md`**, **`docs/showcase-guide.md`**  
- **`docs/ios-host-runbook.md`**, **`docs/ios-library-usage.md`** — iOS validation and KMP+iOS integration
- **`docs/implementation-roadmap.md`**, **`docs/feature-inventory.md`**, **`docs/android-showcase-plan.md`** — long-range scope and showcase IA
