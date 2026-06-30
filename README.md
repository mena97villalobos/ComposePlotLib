# ComposePlotLib

`ComposePlotLib` is the Kotlin Multiplatform and Compose Multiplatform successor project being designed in this repository. The goal is to build a modern charting library inspired by MPAndroidChart, with support for Android and iOS and a declarative Compose-first API.

The library ships cartesian **line**, **bar**, **stacked bar**, **scatter**, **candlestick**, and **combined** charts, radial **pie / donut**, **radar**, and **gauge** (progress-ring), plus a **sankey** flow diagram, **heatmap** grids, a thin axis-less **sparkline** wrapper, and **domain-axis** line demos (category + epoch-ms time). **Line** charts add **area / threshold-band fill** (`LineStyle.areaFill`); **reference lines** apply to line, bar, scatter, and combined charts; **marker shapes** (`PointStyle.shape`) style line and scatter vertices; and **scatter** charts additionally support **per-point styling** and **bubble** point sizing (`ChartPoint.markerShape` / `markerColor` / `markerRadius`). All of this uses immutable specs, a shared `chart-core` planner, `chart-compose` rendering, Android **showcase** + **Roborazzi**, and the shared **`ComposePlotApp()`** host on both **Android** (`composeApp`) and **iOS** (`iosApp`). Documentation targets humans and AI agents.

## Start here
1. **[`docs/production-readiness-next-steps.md`](./docs/production-readiness-next-steps.md)** — what “production ready” still needs; concrete next tasks  
2. [`AGENTS.md`](./AGENTS.md) — contributor rules, **`ComposePlotApp`** + docs obligations for new charts, `gradlew` commands, module boundaries  
3. [`docs/README.md`](./docs/README.md) — index of all documentation  
4. [`docs/architecture.md`](./docs/architecture.md) and [`docs/api-and-state.md`](./docs/api-and-state.md) — design and public API  

## Documentation
| Topic | Doc |
|------|-----|
| Viewport constraints & axis lock | [`docs/viewport-configuration.md`](./docs/viewport-configuration.md) |
| Module ownership | [`docs/module-guide.md`](./docs/module-guide.md) |
| Android showcase & Roborazzi | [`docs/showcase-guide.md`](./docs/showcase-guide.md) |
| iOS Xcode host | [`docs/ios-host-runbook.md`](./docs/ios-host-runbook.md) |
| Long-range scope | [`docs/implementation-roadmap.md`](./docs/implementation-roadmap.md), [`docs/feature-inventory.md`](./docs/feature-inventory.md) |
| Public API release gate | [`docs/api-checklist.md`](./docs/api-checklist.md) |
| Showcase IA (target) | [`docs/android-showcase-plan.md`](./docs/android-showcase-plan.md) |

## Module structure
Current modules:

- `:chart-core`
- `:chart-compose`
- `:composeApp`
- `:samples:android-showcase`

## Install (GitHub Packages)

This repo publishes artifacts to **GitHub Packages** from the Release workflow (tags like `v1.2.3` publish version `1.2.3`).

### 1) Add the repository

In your consumer project’s `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://maven.pkg.github.com/OWNER/REPO")
            credentials {
                // Use Gradle properties (recommended) or env vars.
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

In `~/.gradle/gradle.properties` (or your project’s `gradle.properties`):

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_TOKEN_WITH_read:packages
```

### 2) Add dependencies (Compose Multiplatform / KMP)

Most apps only need `chart-compose` (it has an `api` dependency on `chart-core`):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mena97villalobos.composeplotlib:chart-compose:VERSION")
        }
    }
}
```

If you want planner-only usage (no Compose), depend on `chart-core`:

```kotlin
implementation("com.mena97villalobos.composeplotlib:chart-core:VERSION")
```

### Notes about iOS artifacts

Linux CI does not publish Kotlin/Native iOS klibs. If you need Apple artifacts in GitHub Packages, publish from a macOS job (or publish locally from a Mac).

## Quick start (Compose)

1. Build an immutable spec (e.g. `LineChartSpec`)
2. Render it with a chart composable (e.g. `LineChart`)
3. Keep interaction state explicit (`ChartViewportState`, `ChartSelectionState`)

See the Dokka **Quick start** page for a copy-pasteable sample.

## Current Project Layout

- [`chart-core`](./chart-core/src) contains the non-UI chart engine and planner.
- [`chart-compose`](./chart-compose/src) contains the public Compose chart API and rendering layer.
- [`samples/android-showcase`](./samples/android-showcase/src) contains the curated Android showcase screens.
- [`composeApp`](./composeApp/src) contains the shared host app for the current milestone.
- [`iosApp`](./iosApp/iosApp) contains the iOS entry point.

## Next implementation steps
See **[`docs/production-readiness-next-steps.md`](./docs/production-readiness-next-steps.md)** for the full backlog. In short: harden **layout/text** and **performance**, richer **axes** and **marker** UX, ship **animation** when ready, then **pie/donut** → **combined** (planner-gated). **Scatter** is already shipped (`ScatterChart`, `ScatterChartSpec`, Android showcase screen).

## Build and Run Android

```shell
./gradlew :composeApp:assembleDebug
```

## Build and Run iOS
Open [`iosApp`](./iosApp) in Xcode and run the iOS app entry point from there.