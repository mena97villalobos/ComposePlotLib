# Getting Started

Render your first ComposePlotLib chart, then make it interactive. About ten minutes, assuming you
already have a Compose Multiplatform or Android Compose project.

## Prerequisites

1.2.0 was built against **Kotlin 2.4.0** and **Compose Multiplatform 1.11.1**, targets Android
`minSdk` 28 and `iosArm64` / `iosSimulatorArm64`, and needs **JDK 17** to build. The full
compatibility matrix — including why there is no `iosX64` — is in [Installation](Installation);
the versions themselves come from
[`gradle/libs.versions.toml`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/gradle/libs.versions.toml).

## Add the dependency

Artifacts live on GitHub Packages, which requires a token with `read:packages` **even for public
packages**. Once the repository is configured:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mena97villalobos.composeplotlib:chart-compose:1.3.0")
        }
    }
}
```

`chart-compose` exposes `chart-core` transitively, so one dependency gives you both the composables
and every spec type. The repository block, credentials and `chart-core`-only usage are covered in
[Installation](Installation).

## Your first chart

A complete, self-contained composable:

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle

@Composable
fun WeeklyDownloadsChart() {
    val spec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "downloads",
                        label = "Downloads",
                        points =
                            listOf(
                                ChartPoint(0.0, 120.0),
                                ChartPoint(1.0, 180.0),
                                ChartPoint(2.0, 150.0),
                                ChartPoint(3.0, 240.0),
                                ChartPoint(4.0, 300.0),
                            ),
                        lineStyle = LineStyle(color = ChartColor.Blue, strokeWidth = 3.0),
                    ),
                ),
            xAxis = AxisSpec(scale = AxisScale.Category(listOf("Mon", "Tue", "Wed", "Thu", "Fri"))),
        )

    LineChart(
        spec = spec,
        modifier = Modifier.fillMaxWidth().height(240.dp),
        viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
        selectionState = rememberChartSelectionState(),
    )
}
```

That is the whole shape of the API: build a value, hand it to a composable.

## What each piece does

**`LineChartSpec` is immutable data.** It describes *what* to draw — series, axes, grid, legend,
labels — and nothing about *how* it is drawn on screen. There are no setters and no chart object to
mutate; to change the chart you build a new spec (usually with `copy(...)`) and recompose. Every
chart family has its own spec type, and every field beyond `series` has a default, so you only spell
out what differs from the defaults.

**`ChartPoint` carries domain coordinates**, not pixels — `x` and `y` are `Double` values in your
data's units. With `AxisScale.Category`, x values are the integer indices into the category list.
Pixel mapping is the planner's job, so the same spec renders identically at any size.

**Viewport and selection state are hoisted and explicit.** `rememberChartViewportState` owns the
visible window (pan/zoom); `rememberChartSelectionState` owns the currently highlighted point. Both
are ordinary Compose state you can lift into a ViewModel, share between charts to link them, or drive
programmatically. Both parameters have defaults identical to the code above — pass them explicitly
only when you need to read or write them.

**`defaultViewport(spec)`** computes the data extent for a spec, which is the natural starting
window. It is overloaded per spec type; there is no generic version.

## Make it interactive

Tap-to-select and pan/pinch are already on by default (`ChartGestureConfig()`). What is off by
default is double-tap zoom and inertial pan. Turn those on with a preset, add a tooltip with the
`marker` slot, and read the selection back:

```kotlin
import androidx.compose.material3.Text
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker

@Composable
fun InteractiveDownloadsChart() {
    val spec = /* as above */
    val viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec))
    val selectionState = rememberChartSelectionState()

    LineChart(
        spec = spec,
        modifier = Modifier.fillMaxWidth().height(240.dp),
        viewportState = viewportState,
        selectionState = selectionState,
        gestureConfig =
            ChartGestureConfig.cartesianInteractive(
                doubleTapZoomInEnabled = true,
                panFlingEnabled = true,
            ),
        marker = { data -> DefaultChartMarker(data) },
    )

    val target = selectionState.selection?.target
    if (target != null) {
        Text("${target.seriesId} @ ${target.point.x} = ${target.point.y}")
    }
}
```

`ChartGestureConfig` splits into three independent policies — `tap` (selection),
`transform` (pan/zoom/double-tap) and `fling` (inertial pan) — with companion presets for the common
bundles: `CartesianDefault`, `PieDefault`, `readOnly()`, `exploreOnly(...)`, `cartesianInteractive(...)`
and `cartesianInteractiveFullExploration()`. Because `viewportState` is yours, you can also drive it
from your own UI: `viewportState.reset()`, `fitToData()`, `panTo(...)`, `zoomTo(...)`, `centerOn(...)`.

The `marker` slot is a `BoxScope` composable receiving `ChartMarkerData` (selection plus preformatted
`seriesLabel` / `xLabel` / `yLabel` / `valueLabel` / `accessibilitySummary`), so a custom tooltip is
just your own composable in place of `DefaultChartMarker`.

## Next steps

- [Chart Gallery](Chart-Gallery) — all 26 chart families and which spec each one takes.
- [Core Concepts](Core-Concepts) — specs, the planner, draw commands, and why `chart-core` has no
  Compose types.
- [Interactivity and State](Interactivity-and-State) — viewport constraints, axis locks, linked
  viewports, range brushing, drill-down, accessibility.
- [Styling and Theming](Styling-and-Theming) — `ChartColor`, per-series styles, `ChartChromeTheme`,
  and mapping `MaterialTheme` into specs.

For the exhaustive parameter-by-parameter reference, go to
[`docs/api-and-state.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md)
in the repository — that is the canonical source, and it is kept in sync with the published API dumps.
