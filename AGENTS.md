# ComposePlotLib Agent Guide

This file is the working guide for AI agents and future contributors operating inside `ComposePlotLib`.

## Obligations for AI agents and contributors
- **Showcase (`samples/android-showcase`)** — When you add a **new public chart family** (new top-level composable wired to `chart-core` planning), you **must** add a showcase screen in [`samples/android-showcase/.../screens`](samples/android-showcase/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/samples/android/screens), wire it into **`ShowcaseApp.kt`**, and add a **Roborazzi** scenario so the family is screenshot-tested.
- **Documentation** — You **must** update documentation to match the **current** behavior and surface area: at minimum **`AGENTS.md`**, **`docs/api-and-state.md`**, **`docs/architecture.md`**, and any other `docs/*.md` file whose claims become false (e.g. **`docs/module-guide.md`**, **`docs/roadmap.md`**). For releases or broad API changes, walk **`docs/api-checklist.md`** and refresh it if the supported surface changes. Do not leave docs describing fewer charts than the code ships.

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
  Owns public Compose APIs (`LineChart`, `BarChart`, `StackedBarChart`, `ScatterChart`,
  `CombinedCartesianChart`, `PieChart`, `RadarChart`, `SankeyChart`, `HeatmapChart`,
  `CandlestickChart`, `BoxPlotChart`, `WaterfallChart`, `BubbleChart`, `BulletChart`, `GaugeChart`, `Sparkline`),
  Compose-managed interaction state, gesture adapters, and Compose canvas rendering. Also compiles
  for iOS so the library can be consumed from Compose Multiplatform apps.
- `samples/android-showcase`
  Primary (Android-only) showcase app for feature demos, manual verification, and Roborazzi
  screenshot tests; every shipped chart family has a showcase screen.

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
- **Every line of code must be Kotlin Multiplatform compatible.**
  `chart-core` and `chart-compose` both target Android **and** iOS via KMP `commonMain`. Never use
  JVM/Android-only APIs in `commonMain` source sets. Common violations to avoid:
  - `String.format` / `"...".format(...)` — JVM-only; use `kotlin.math` + manual string building instead.
  - `java.*` imports of any kind.
  - Android SDK types (`android.*`, `androidx.*`) outside of `androidMain` source sets.
  When in doubt, check that the symbol resolves in `kotlin-stdlib-common` or a KMP-compatible library.

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
  Add a sample/screen in **`samples/android-showcase`**, wire it into **`ShowcaseApp`** with a **Roborazzi** scenario, and update **`docs/*`** (see **Obligations** above).
- For a new showcase-only feature (no new chart family):
  Add sample data, then a dedicated screen in `samples/android-showcase`, then wire it into `ShowcaseApp`.

## Testing Expectations
- Add or update `chart-core` tests for new math, transform, tick-generation, or selection logic.
- **CI** — PR and release workflows (`.github/workflows/pr.yml`, `.github/workflows/release.yml`) run on **Ubuntu** only: `ktlintCheck`, JVM + Android unit tests, Roborazzi, and `:samples:android-showcase:assembleDebug`. See **`docs/github-ci-packages-and-pages.md`** for GitHub Packages consumption and Pages setup.
- Build at least:
  `./gradlew :chart-core:testDebugUnitTest :chart-compose:compileDebugKotlinAndroid :samples:android-showcase:verifyRoborazziDebug :samples:android-showcase:assembleDebug`
- On **macOS**, when changing shared model or Compose code paths used on iOS, also run:
  `./gradlew :chart-compose:compileKotlinIosSimulatorArm64` (and `:chart-compose:compileKotlinIosArm64` if you need device slices).
- After intentional UI changes to showcase charts, refresh screenshot baselines with:
  `./gradlew :samples:android-showcase:recordRoborazziDebug`
- Use the showcase app as a manual verification surface for rendering changes.
- **Perf / scale:** scatter decimation and planner invariants are covered by `chart-core` unit tests (`ScatterRenderPipelineTest`, `ChartPlannerTest.scatterChart_decimation_capsCircleCommands`); optional JVM microbenchmarks live in `:chart-core-benchmarks` (`./gradlew :chart-core-benchmarks:jmh --no-configuration-cache`; see `docs/performance-benchmarks.md`). Unit tests are regression checks, not timed benchmarks.
- **Gestures:** `ChartGestureConfig` companion presets (`CartesianDefault`, `PieDefault`, `readOnly`, `exploreOnly`, `cartesianInteractive`, `cartesianInteractiveFullExploration`); iOS touch QA matrices and maintainer log in `docs/ios-gesture-validation.md` (compile gate alone is `./gradlew :chart-compose:compileKotlinIosSimulatorArm64`; physical device row still required before release touch QA).

## Known current limitations
- Text/legend layout is measurement-assisted but still imperfect (long labels, font scale, iOS text parity).
- Tick generation defaults to nice **linear** numeric behavior when unspecified; **category** and **epoch-millis time** scales are first-class in `TickValuePlanner` (business-calendar ticks remain app-level). Optional **`AxisSpec.minVisibleXTickLabels` / `minVisibleYTickLabels`** (soft targets after overlap + clip) and **`raiseTickDensityCapFromMeasuredLabelWidth`** (raises density cap from measured label width) are planner-level hooks; see **`AxisSpec`** KDoc and **`docs/api-and-state.md`**.
- Marker/tooltip placement is plot/canvas aware (flip/slide); optional **elbow** or **`ChartMarkerCustomCallout`** on `ChartMarkerPlacement.callout`; **`nudgeAxisAlignedRectToAvoidOverlap`** / **`stackAxisAlignedRectBelow`** for multi-overlay layouts; with **`requestFocusOnMarker`**, marker container uses unmerged semantics so focusables inside custom `marker` content work (showcase: custom callout + button).
- Accessibility: Image role, focus opt-in, custom actions, configurable **selection live region** (`ChartLiveRegionMode`), optional **keyboard viewport** shortcuts on cartesian charts (arrow pan, +/− zoom, Escape clears tap selection), optional **`keyboardSelectionRoamEnabled`** (Shift+arrow roam; Shift+Up/Down switch series on line/scatter), **`chartViewportAccessibilityActions`** and **`chartAccessibilityActionsMergeViewport`**, optional **`ChartAccessibilitySpec.requestFocusOnMarker`** (separate marker focus target; showcase line sample), and reduced-motion integration for chart motion are in place; see **`docs/accessibility-custom-actions.md`**.
- Viewport/selection **animation**: opt-in via `ChartAnimationPolicy` (`viewportChangesEnabled`, `gestureViewportEasingEnabled` + `gestureViewportChaseStiffness`, `selectionHighlightEnabled`, `dataTransitionEnabled` / `dataTransitionDurationMillis` for **line** same-topology morph, durations); programmatic viewport updates can ease the rendered window; optional **gesture chase** eases toward gesture targets each frame when enabled (otherwise gestures snap). Marker highlight can **fade in and out** on selection clear when `selectionHighlightEnabled` and `selectionDurationMillis` > 0. `respectSystemReducedMotion` zeros motion when the user prefers reduced animation.
- iOS uses the same Compose pipeline (as a consumed library); automated visual regression is **Android (Roborazzi)** only.
- **Large datasets:** line series support `LineRenderSpec` decimation and optional `usePlotWidthPixelBudget` (`LineChart`). Scatter supports `ScatterRenderSpec` + `ScatterRenderPipeline` (domain pre-filter + decimation when capped; Compose pixel budget mirrors line; default path keeps all points then plot-disc culling). Large-series **invariant / smoke** tests live in `ScatterRenderPipelineTest` / `ChartPlannerTest`; JVM **JMH** harness ships in `:chart-core-benchmarks`; **CI** micro-benchmarks remain optional (`docs/performance-benchmarks.md`).
- **Pie / donut:** optional `PieSliceLabelsSpec` draws leader polylines and text from the planner; placement is per-slice with clamping to the plot — not a full collision-avoidance pass across labels.
- **Combined cartesian:** `CombinedCartesianChart` overlays line/bar/scatter layers; **secondary Y** is **data-derived** in the planner (not independently panned/zoomed via `ChartViewportState` in v1). Right-axis tick text uses `yAxisSecondary.labelStyle` via `DrawCommand.Text.isSecondaryYAxisTick`.

## When Adding New Features
- If the feature changes coordinate behavior, update:
  `chart-core/.../viewport`, `transform`, `layout`, or `selection`
- If the feature changes rendering semantics, update:
  `chart-core/.../layout/ChartLayoutPlan.kt`
- If the feature adds new public API, update:
  `chart-compose/.../chart/*.kt` and relevant `state/*`
- If the feature is a **new chart family** composable, add a **showcase screen** and update **docs** (see **Obligations**).
- If the feature needs a visual example, add a screen in:
  `samples/android-showcase/src/commonMain/kotlin/.../screens`

## New chart families
Prefer this order (see `docs/roadmap.md`):

1. **Scatter** — **shipped**; same cartesian pipeline as line/bar (`ScatterChartSpec`, `planScatterChart`, `ScatterChart`).
2. **Pie / donut** — **shipped**; radial planning, `DrawCommand.Arc` (+ optional `innerRadius`), `PieHitResolver`, `PieChart`, showcase (**`AGENTS.md` → Obligations**).
3. **Combined** — **shipped** (`CombinedCartesianChartSpec`, `planCombinedCartesianChart`, `CombinedCartesianChart`); design reference **`docs/design-combined-cartesian-layers.md`**. Do not layer pie with cartesian until both are solid alone.
4. **Heatmap** — **shipped** (`HeatmapChartSpec`, `HeatmapColorScale`, `HeatmapCell`,
   `planHeatmapChart`, `HeatmapChart`); 2-D grid of colored cells; color encodes value via linear
   channel lerp; `AxisScale.Category` forced on both axes; tap-only gesture config; selection
   highlights selected cell with a stroke border.
5. **Gauge / progress-ring** — **shipped (1.1.0)** (`GaugeChartSpec`, `planGaugeChart`, `GaugeChart`);
   radial progress ring with track + progress arc, optional rounded caps, configurable
   `startAngleDegrees` / `sweepDegrees`, and an optional `centerContent` slot; **non-interactive**
   (no pan/zoom/selection).
6. **Candlestick / OHLC** — **shipped (1.1.0, library-only)** (`CandlestickChartSpec`, `OHLCPoint`,
   `CandlestickSeries`, `CandlestickStyle`, `CandlestickRenderSpec`, `planCandlestickChart`,
   `CandlestickChart`); cartesian body + wick rendering, bull/bear coloring, decimation, pan/zoom,
   tap-to-OHLC marker. App-side adoption stays gated on an OHLC data feed.
7. **Sparkline** — **shipped (1.1.0)** (`Sparkline`); a thin axis-less / grid-less / non-interactive
   wrapper over `LineChart` with optional area fill.

Additive (no new family) in **1.1.0**: **area / threshold-band fill** (`LineStyle.areaFill` /
`areaFillEnd` / `areaFillMode`), **reference lines** (`ReferenceLine` via `referenceLines` on line /
bar / scatter / combined specs), **marker shapes + per-point styling** (`MarkerShape`,
`PointStyle.shape`, `ChartPoint.markerShape` / `markerColor` / `markerRadius`), and **bubble** sizing
on `ScatterChart` (per-point `markerRadius`).

## Planner structure (`chart-core`)
Cartesian planning is split for clarity: tick/chrome (`ChartChromeLayout.kt`), final frame (`CartesianLayoutPlanner.kt`, `CartesianFrame`), orchestration + draw commands (`ChartPlanner.kt`). See **`docs/architecture.md`** → *Planner passes*.

## File areas to read first
- `docs/roadmap.md`
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
- `chart-compose/src/commonMain/kotlin/.../theme/MaterialChartPalette.kt`, `.../theme/MaterialChartSpecTheme.kt`
- `docs/ios-library-usage.md`

## Documentation map
- **`docs/README.md`** — index of all documentation files  
- **`docs/roadmap.md`** — **single source of truth for open work**: current state, priorities, feature backlog, production-hardening workstreams, and showcase roadmap  
- **`docs/architecture.md`**, **`docs/api-and-state.md`**, **`docs/api-checklist.md`**, **`docs/viewport-configuration.md`**, **`docs/style-and-theme.md`**, **`docs/marker-tooltip-placement.md`**, **`docs/chart-families-overview.md`**, **`docs/module-guide.md`**, **`docs/showcase-guide.md`**  
- **`docs/ios-library-usage.md`** — KMP+iOS integration for consuming apps

<!-- code-review-graph MCP tools -->

## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool                        | Use when                                               |
|-----------------------------|--------------------------------------------------------|
| `detect_changes`            | Reviewing code changes — gives risk-scored analysis    |
| `get_review_context`        | Need source snippets for review — token-efficient      |
| `get_impact_radius`         | Understanding blast radius of a change                 |
| `get_affected_flows`        | Finding which execution paths are impacted             |
| `query_graph`               | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes`     | Finding functions/classes by name or keyword           |
| `get_architecture_overview` | Understanding high-level codebase structure            |
| `refactor_tool`             | Planning renames, finding dead code                    |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.
