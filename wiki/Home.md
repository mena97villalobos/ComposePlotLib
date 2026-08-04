# ComposePlotLib

A charting library for Kotlin Multiplatform and Compose Multiplatform — **26 chart families**, one
declarative API, Android and iOS. A modern successor to MPAndroidChart, built Compose-first around
immutable specs and explicit state.

<img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/line-chart.png" width="240"> <img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/radar-chart.png" width="240"> <img src="https://raw.githubusercontent.com/mena97villalobos/ComposePlotLib/main/docs/images/sankey-chart.png" width="240">

## Start here

| If you want to… | Go to |
|-----------------|-------|
| Render your first chart | [Getting Started](Getting-Started) |
| Add the dependency | [Installation](Installation) |
| See what charts exist | [Chart Gallery](Chart-Gallery) |
| Understand the design | [Core Concepts](Core-Concepts) |
| Add pan, zoom, selection | [Interactivity and State](Interactivity-and-State) |
| Change colors and styling | [Styling and Theming](Styling-and-Theming) |
| Use it from an iOS app | [iOS Guide](iOS-Guide) |
| Solve a problem | [FAQ and Troubleshooting](FAQ-and-Troubleshooting) |
| Contribute | [Contributing](Contributing) |

## In one snippet

```kotlin
val spec = LineChartSpec(
    series = listOf(
        LineSeries(
            id = "downloads",
            label = "Downloads",
            points = listOf(ChartPoint(0.0, 1.0), ChartPoint(1.0, 3.0), ChartPoint(2.0, 2.0)),
        ),
    ),
)

LineChart(
    spec = spec,
    viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
    selectionState = rememberChartSelectionState(),
)
```

## About this wiki

This wiki is the **onboarding and navigation** layer. The exhaustive reference documentation lives
in the repository under [`docs/`](https://github.com/mena97villalobos/ComposePlotLib/tree/main/docs)
and is the canonical source — wiki pages link into it rather than duplicating it, so the two cannot
drift apart.

Wiki pages are generated from
[`wiki/`](https://github.com/mena97villalobos/ComposePlotLib/tree/main/wiki) on the `main` branch.
**Edit the files there, not the wiki UI** — wiki edits are overwritten on the next sync.

| Canonical reference | |
|---|---|
| Public API and state | [`docs/api-and-state.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md) |
| All 26 chart families | [`docs/chart-families-overview.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/chart-families-overview.md) |
| Architecture | [`docs/architecture.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/architecture.md) |
| Changelog | [`CHANGELOG.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/CHANGELOG.md) |
| Roadmap | [`docs/roadmap.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/roadmap.md) |

Licensed under [Apache 2.0](https://github.com/mena97villalobos/ComposePlotLib/blob/main/LICENSE).
