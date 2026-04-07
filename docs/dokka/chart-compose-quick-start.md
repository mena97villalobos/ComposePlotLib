# Module chart-compose

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
            url = uri("https://maven.pkg.github.com/OWNER/REPO")
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
            implementation("com.mena97villalobos.composeplotlib:chart-compose:VERSION")
        }
    }
}
```

## Minimal usage

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

