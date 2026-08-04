# Installation

ComposePlotLib 1.2.0 is published as a Kotlin Multiplatform library under the group id
`com.mena97villalobos.composeplotlib`. Two modules are published: `chart-compose` (the Compose API
you render with) and `chart-core` (the non-UI planning engine).

## Compatibility

| | |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| Android | `minSdk` 28, `compileSdk` 36 |
| JVM target | 11 (both modules) |
| iOS | `iosArm64`, `iosSimulatorArm64` |
| Building the repo itself | JDK 17, Gradle 8.14.3, AGP 8.13.2 |

Versions come from
[`gradle/libs.versions.toml`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/gradle/libs.versions.toml).

### There is deliberately no `iosX64`

Compose Multiplatform 1.11.1 ships no `ios_x64` artifacts, so Intel-Mac simulators are unsupported;
Apple-silicon simulators (`iosSimulatorArm64`) and devices (`iosArm64`) are fully supported. Why
that is an upstream limitation rather than a project decision is explained in the
[iOS Guide](iOS-Guide).

## Which module to depend on

Most apps depend only on **`chart-compose`**. It declares `chart-core` with Gradle's `api`
configuration, so every spec, model and viewport type is on your compile classpath transitively —
you do not need to add `chart-core` yourself.

```kotlin
// build.gradle.kts — Compose Multiplatform app
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mena97villalobos.composeplotlib:chart-compose:1.2.0")
        }
    }
}
```

Depend on **`chart-core`** alone only when you want the planner without any Compose dependency —
for example computing a `ChartLayoutPlan` on a server, in a JVM test, or feeding draw commands to a
non-Compose renderer:

```kotlin
implementation("com.mena97villalobos.composeplotlib:chart-core:1.2.0")
```

Note the platform difference: `chart-compose` targets **Android and iOS only** (no JVM/desktop
target), while `chart-core` additionally publishes a **JVM** artifact.

## GitHub Packages setup

1.2.0 is distributed through GitHub Packages. GitHub's Maven registry requires authentication
**even for public packages** — an anonymous build will fail with a 401. Every consumer, and every
consumer CI job, needs a personal access token with `read:packages` (classic PAT) or the equivalent
GitHub Packages read scope (fine-grained PAT).

Add the repository:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://maven.pkg.github.com/mena97villalobos/ComposePlotLib")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Put the credentials in your **user-level** Gradle properties so they never land in the repo:

```properties
# ~/.gradle/gradle.properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_TOKEN_WITH_read:packages
```

In CI, set `GITHUB_ACTOR` and `GITHUB_TOKEN` (or your own secrets) as environment variables instead.

## Published artifacts

Releases are cut from semver tags (`v1.2.0` publishes version `1.2.0`) across two jobs, so both
modules end up complete:

| Module | Android | JVM | `iosArm64` / `iosSimulatorArm64` | KMP metadata |
|--------|:-------:|:---:|:-------------------------------:|:------------:|
| `chart-core` | yes | yes | yes | yes |
| `chart-compose` | yes | — | yes | yes |

The `kotlinMultiplatform` metadata publication is what lets you depend on the root coordinates
(`com.mena97villalobos.composeplotlib:chart-compose:1.2.0`) from `commonMain` and have Gradle pick
the right platform variant per target. Apple artifacts are published from a macOS job — no local Mac
build or manual publishing step is required on your side.

## Maven Central

Publishing to Maven Central is already wired into the build (vanniktech maven-publish 0.34.0, with
sources and Dokka javadoc jars on every publication including the metadata module), but it is
**pending namespace verification** for the `com.mena97villalobos` group id and is gated behind a
repository variable, so 1.2.0 is not on Central yet. Until then, GitHub Packages with a token is the
only channel. The remaining setup is tracked in
[docs/github-ci-packages-and-pages.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/github-ci-packages-and-pages.md#maven-central-central-portal).

## Verify the setup

A minimal chart that proves resolution, the Compose API and the state helpers all work:

```kotlin
import androidx.compose.runtime.Composable
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec

@Composable
fun InstallSmokeTest() {
    val spec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "installs",
                        label = "Installs",
                        points = listOf(ChartPoint(0.0, 1.0), ChartPoint(1.0, 3.0), ChartPoint(2.0, 2.0)),
                    ),
                ),
        )
    LineChart(
        spec = spec,
        viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
        selectionState = rememberChartSelectionState(),
    )
}
```

`LineSeries` and `LineChartSpec` come from `chart-core` — they compile without an explicit
`chart-core` dependency precisely because `chart-compose` re-exports it.

## Next steps

- [Getting Started](Getting-Started) — building your first chart end to end
- [Chart Gallery](Chart-Gallery) — all 26 chart families
- [iOS Guide](iOS-Guide) — consuming the library from a Compose Multiplatform iOS app
- [FAQ and Troubleshooting](FAQ-and-Troubleshooting) — 401s from GitHub Packages and other snags
- [CI, Packages and Pages](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/github-ci-packages-and-pages.md)
  — the canonical release/publishing reference
