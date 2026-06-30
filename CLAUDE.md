# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

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

## Project Overview

`ComposePlotLib` is a Kotlin Multiplatform (KMP) + Compose Multiplatform charting library — a modern
successor to MPAndroidChart. It targets Android and iOS.

## Build Commands

```bash
# Lint
./gradlew ktlintCheck

# Unit tests (JVM + Android)
./gradlew :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest

# Roborazzi screenshot verification
./gradlew :samples:android-showcase:verifyRoborazziDebug

# Full pre-PR build (mirrors CI)
./gradlew :chart-core:testDebugUnitTest :chart-compose:compileDebugKotlinAndroid :samples:android-showcase:verifyRoborazziDebug :composeApp:assembleDebug

# After intentional UI changes — refresh screenshot baselines
./gradlew :samples:android-showcase:recordRoborazziDebug

# iOS compilation check (macOS only)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# JMH microbenchmarks (optional)
./gradlew :chart-core-benchmarks:jmh --no-configuration-cache

# Documentation
./gradlew dokkaGenerateHtml
```

JDK 17 is required. iOS targets only build on macOS (or set `COMPOSEPLOTLIB_FORCE_IOS_TARGETS` env
var).

## Architecture

The codebase is structured around a strict separation between platform-agnostic planning and Compose
rendering.

### Modules

| Module                      | Role                                                                               |
|-----------------------------|------------------------------------------------------------------------------------|
| `:chart-core`               | Non-UI engine: math, geometry, specs, viewport, transforms, planner, draw commands |
| `:chart-compose`            | Public Compose API and rendering: composables, gesture handling, canvas adapter    |
| `:composeApp`               | Thin shared host for milestone previews (Android + iOS via `ComposePlotApp()`)     |
| `:samples:android-showcase` | Main demo/verification app with Roborazzi screenshot tests                         |
| `:chart-core-benchmarks`    | Optional JMH microbenchmarks                                                       |

Dependency direction: `chart-core` → `chart-compose` → `composeApp` / `android-showcase`

### Data Flow

```
ChartSpec + ChartViewportState + ChartSelectionState
    → ChartPlanner (chart-core)
    → ChartLayoutPlan containing DrawCommand list
    → ComposeCanvasAdapter (chart-compose)
    → Compose Canvas
```

All new features must fit this pipeline — extend planner output types before adding ad-hoc canvas
code.

### Architectural Rules

- **No Compose types in `chart-core`**: `Modifier`, `Color`, `Dp`, `Offset`, `Rect`, `Path`,
  `Canvas`, `TextStyle` are forbidden in `chart-core`.
- **Specs are immutable**: new configuration uses new spec/model types, not mutable setters.
- **State is explicit**: pan/zoom/selection lives in `ChartViewportState` and `ChartSelectionState`.
- **Extend the planner first**: layout/semantic changes go to `chart-core` planning types before
  renderer changes in `chart-compose`.
- **When in doubt about placement**: no-UI + reusable → `chart-core`; Compose/canvas dependency →
  `chart-compose`; demo-only → `samples/android-showcase`.
- **Every line must be KMP-compatible**: `chart-core` and `chart-compose` target Android *and* iOS
  via `commonMain`. Never use JVM/Android-only APIs there. Common violations to avoid:
  - `String.format` / `"...".format(...)` — JVM-only; use `kotlin.math` + manual string building.
  - `java.*` imports of any kind in `commonMain`.
  - `android.*` / `androidx.*` types outside `androidMain` source sets.
  Verify any stdlib symbol resolves in `kotlin-stdlib-common` or a KMP-compatible library.

### Shipped Chart Families

`LineChart`, `BarChart`, `StackedBarChart`, `ScatterChart`, `CombinedCartesianChart`, `PieChart`,
`RadarChart`, `SankeyChart`, `HeatmapChart`, `GaugeChart`, `CandlestickChart`, plus the axis-less
`Sparkline` wrapper.

Line/scatter charts also support area / threshold-band fill (`LineStyle.areaFill` + `AreaFillMode`),
reference lines (`ReferenceLine`), marker shapes with per-point styling (`MarkerShape`, `ChartPoint`
marker overrides), and bubble point sizing.

## Adding a New Chart Family

1. Add planner logic and spec types to `chart-core`.
2. Add the public composable to `chart-compose`.
3. **Required**: Add a preview tab in `chart-compose/.../ComposePlotApp.kt` so Android and iOS hosts
   stay aligned.
4. Add a showcase screen in `samples/android-showcase/src/commonMain/kotlin/.../screens` and wire it
   into `ShowcaseApp.kt`.
5. Update docs: `AGENTS.md`, `docs/architecture.md`, `docs/api-and-state.md`,
   `docs/module-guide.md`, `docs/production-readiness-next-steps.md`, and any other file whose
   claims become false.

## Key Files

- `chart-core/.../layout/ChartPlanner.kt` — planning orchestration
- `chart-core/.../layout/ChartLayoutPlan.kt` — plan output types and draw commands
- `chart-compose/.../chart/internal/canvas/ComposeCanvasAdapter.kt` — renders `DrawCommand` to
  Compose canvas
- `chart-compose/.../chart/state/ChartViewportState.kt` — viewport state
- `chart-compose/.../chart/ComposePlotApp.kt` — shared Android/iOS milestone host (must preview
  every shipped family)
- `samples/android-showcase/.../ShowcaseApp.kt` — showcase navigation host
- `docs/architecture.md`, `docs/api-and-state.md`, `docs/module-guide.md` — design docs
- `docs/production-readiness-next-steps.md` — primary backlog

## CI

PRs run on Ubuntu: `ktlintCheck`, JVM + Android unit tests, Roborazzi verification,
`:composeApp:assembleDebug`. Releases trigger on semver tags (`v*.*.*`) and publish to GitHub
Packages + GitHub Pages.

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
