# Module ComposePlotLib

## Quick start

`ComposePlotLib` is split into:

- `chart-core`: platform-agnostic specs + planning (no Compose UI types)
- `chart-compose`: Compose Multiplatform composables + rendering + interaction state

## Install (GitHub Packages)

Add the GitHub Packages Maven repo in your consumer project and authenticate with a token that can read packages.

Example `settings.gradle.kts`:

```kotlin
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

Add the dependency (common code usually depends on `chart-compose`, which re-exports `chart-core`):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mena97villalobos.composeplotlib:chart-compose:1.2.0")
        }
    }
}
```

## Minimal usage

Build an immutable spec, then render it with a composable. Viewport/selection state is explicit and can be
remembered or passed in.

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
fun BasicLineChart() {
    val spec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "s1",
                        label = "Series",
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

## Next steps

- Axis scales (numeric, category, epoch-millis time): see `docs/api-and-state.md` and `docs/axis-category-time-cookbook.md`
- Viewport constraints + axis lock: see `docs/viewport-configuration.md`
- Marker/tooltip placement: see `docs/marker-tooltip-placement.md`

