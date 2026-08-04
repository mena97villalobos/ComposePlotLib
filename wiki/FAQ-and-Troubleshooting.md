# FAQ and Troubleshooting

Answers to the things that actually bite people, grouped by when they bite: getting the dependency,
compiling against it, running it, and contributing to it. Everything here is verified against
`1.2.0` and the current `main`; where the two differ, it says so.

## Dependency resolution

### Why do I need a GitHub token just to download a public library?

Because GitHub's Maven registry requires authentication **even for public packages**. That is a
GitHub Packages policy, not a project decision — an anonymous build gets a `401 Unauthorized`, not
a 404. Every consumer and every consumer CI job needs a token with `read:packages`.

Maven Central publishing is already wired into the build (vanniktech maven-publish, signing, sources
and Dokka javadoc jars) but is gated on namespace verification for the `com.mena97villalobos` group
id, so it is not a channel yet. Full setup in [Installation](Installation).

### Gradle cannot resolve the dependency on my Intel Mac

There is no `iosX64` target. Compose Multiplatform `1.11.1` publishes no `ios_x64` artifacts, so a
Compose-dependent `iosX64` target cannot resolve its own dependencies — Intel-Mac simulators are
blocked upstream. Supported Apple targets are `iosArm64` and `iosSimulatorArm64`. The full rationale
is in the [iOS Guide](iOS-Guide).

### Do I need to depend on `chart-core` too?

No. `chart-compose` declares `chart-core` with Gradle's `api` configuration, so every spec, model
and viewport type is on your compile classpath transitively. Depend on `chart-core` alone only when
you want the planner with no Compose dependency at all (server-side planning, JVM tests, a
non-Compose renderer) — it is the only module with a JVM artifact.

## Compilation

### "Unresolved reference: Modifier" (or `ColorScheme`) when I call the chart API

In **1.2.0**, `chart-compose` declared its Compose dependencies as `implementation`, so Compose types
that appear in its public signatures — `Modifier`, `Color`, `ImageBitmap`, `BoxScope`, and material3's
`ColorScheme` via `toMaterialChartPalette` — were not on your compile classpath unless you declared
`compose.runtime` / `compose.foundation` / `compose.ui` / `compose.material3` yourself. Any Compose
Multiplatform app already does, which is why this mostly hits minimal or non-Compose-first modules.

This is fixed on `main` (those four are now `api`) but **is not in 1.2.0**. Until the next release,
declare the Compose dependencies in the module that calls the chart API. Note that when the fix does
ship it puts material3 `1.11.0-alpha07` on the consumer compile classpath.

### My custom formatter compiles for Android but not for iOS

`String.format` and `"…".format(…)` are JVM-only, and `commonMain` compiles for iOS too. The same
applies to any `java.*` import. Build label strings with `kotlin.math` plus manual string building —
this constraint applies to your own `AxisSpec.labelFormatter` implementations exactly as it applies
to library code.

## Rendering and behavior

### My chart is blank

If **every** series has zero points, the cartesian composables deliberately render an empty state
instead of a plot, and skip drawing and hit-testing entirely. Supply your own via the `noData` slot,
or structured copy via `noDataUi`:

```kotlin
LineChart(
    spec = spec,
    noDataUi = ChartNoDataUi(title = "No data yet", message = "Pull to refresh"),
)
```

If some series have points and the chart still looks empty, check the viewport: `rememberChartViewportState`
clamps to `domain`, so an `initialViewport` outside your data shows blank plot area.

### Calling `series.domain()` crashes while data is loading

In **1.2.0**, `DataDomain.fromPoints` threw `IllegalArgumentException` on an empty list, and the public
`LineSeries.domain()` / `ScatterSeries.domain()` inherit that — so calling them mid-load crashed the
host app. On `main` an empty list is treated as the benign state it is: both axes collapse to the
degenerate `0.0..0.0` already used for an axis with no finite values. No signature changed.

This only affects code that calls `domain()` / `fromPoints` directly. The planner and `defaultViewport`
never did, and have always had their own fallback for empty input.

### How do I show a gap for a missing sample?

Use the `ChartPoint.gap(x)` factory — it marks a missing sample (non-finite y, readable back via
`ChartPoint.isGap`) and is ignored when computing bounds, so it cannot corrupt your domain. Lines and
area fills break at a gap:

```kotlin
LineSeries(
    id = "sensor",
    label = "Sensor",
    points = listOf(ChartPoint(0.0, 1.0), ChartPoint.gap(1.0), ChartPoint(2.0, 2.0)),
)
```

To bridge gaps instead of breaking, set `LineRenderSpec.connectAcrossGaps = true` on
`LineChartSpec.lineRender`.

### How do I get a logarithmic axis?

Set `AxisSpec.scale`. `AxisScale.Log(base = 10.0)` is for **strictly positive** data; use
`AxisScale.SymLog(base, linearThreshold)` when your data crosses zero, and `AxisScale.Broken(breaks,
compressionFactor)` to collapse empty stretches of the domain.

```kotlin
LineChartSpec(series = series, yAxis = AxisSpec(scale = AxisScale.Log()))
```

### How do I hide a series when its legend row is tapped?

Put the id in `hiddenSeriesIds` (available on `LineChartSpec`, `ScatterChartSpec` and `BarChartSpec`)
and drive it from `onLegendItemClick`. Hidden series are excluded from drawing but keep their legend
row, drawn dimmed:

```kotlin
var hidden by remember { mutableStateOf(emptySet<String>()) }
LineChart(
    spec = spec.copy(hiddenSeriesIds = hidden),
    onLegendItemClick = { id -> hidden = if (id in hidden) hidden - id else hidden + id },
)
```

### The chart swallows the scroll gesture of its parent

A chart with pan enabled consumes drags. Either disable the conflicting axis with
`ViewportAxisPolicy.HorizontalExploration` (Y locked to domain — locked axes receive no drag or pinch
deltas at all) on `rememberChartViewportState`, or drop pan/zoom entirely with
`ChartGestureConfig.readOnly()`. `exploreOnly()` and `cartesianInteractiveFullExploration()` are the
other documented bundles. See
[viewport-configuration.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/viewport-configuration.md).

### How do I export a chart as an image?

Attach a capture controller to the chart, then snapshot it from a coroutine after at least one frame
has drawn:

```kotlin
val capture = rememberChartCaptureController()
LineChart(spec = spec, modifier = Modifier.chartCaptureTarget(capture))
// later, from an onClick + rememberCoroutineScope():
val png: ByteArray = capture.capturePng()
```

`capturePng()` throws `IllegalStateException` if the platform encoder returns nothing — in practice a
zero-sized bitmap or OOM on a very large capture — so guard user-triggered exports. `captureImageBitmap()`
gives you the un-encoded `ImageBitmap`. For a resolution-independent export, `chartLayoutPlanToSvg(plan)`
serializes a planned `ChartLayoutPlan` with no rasterization at all.

## Contributor build failures

The full CI command and the failure-to-fix table live in [Contributing](Contributing); `apiCheck` and
`verifyChartCorePurity` are the two people forget. The one rule worth restating: `verifyChartCorePurity`
fails when `chart-core/src/commonMain` imports `android.*`, `androidx.*`, `java.*`, `javax.*` or
`org.jetbrains.compose` — move that code to `chart-compose` or to an `androidMain` source set.

## Known limitations

Tracked in [roadmap.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/roadmap.md):

- **Time axes are UTC / fixed-offset only.** IANA time zones and DST-aware ticks are deferred — they
  need a bundled KMP time-zone database.
- **iOS is validated by compilation, not by screenshots.** Android has Roborazzi goldens; iOS has no
  equivalent tooling yet, and the physical-device gesture matrix is still a manual workstream. Text
  measurement parity between the platforms under font scale and non-Latin scripts is an open item.
- **Label and marker layout has rough edges.** Tick-label overlap has residual cases, and multi-marker
  collision solving is limited to rect nudge/stack helpers today.
- **Three or more Y axes are not supported.** Dual-Y ships on `CombinedCartesianChart` (with an
  independent `secondaryYViewport` channel); a third overlaid axis is an explicit deferral. Stacked
  panels are achievable with `ChartFacetGrid(columns = 1)` plus `SyncChartViewports(axis = ChartSyncAxis.X)`.
- **Treemap, sunburst and circle-pack are single-level**, and parallel coordinates has no per-axis
  brushing yet.

Something not covered here? Open an issue with the chart family, a minimal spec that reproduces it,
what you expected, your platform (Android API level or iOS version), and the library version.
