# Performance benchmarks and regression checks

This project treats **`chart-core` unit tests** as the primary **correctness and lightweight regression** gate for large-series behavior (`LineRenderPipelineTest`, `ScatterRenderPipelineTest`, `ChartPlannerTest`, `VisibleRangeCalculatorTest`, `LineRenderSpecAutoBudgetTest`). They are not timed benchmarks.

## JVM microbenchmarks (JMH)

The **`:chart-core-benchmarks`** module runs [JMH](https://github.com/openjdk/jmh) on the JVM against the **`chart-core` JVM target** (hot paths such as `VisibleRangeCalculator`, `LineRenderPipeline.prepareIndexed`, `ScatterRenderPipeline.prepareIndexed`, and `ChartPlanner.planTreemapHierarchy`). Benchmarks may be written in Java (`src/jmh/java`) or Kotlin (`src/jmh/kotlin`).

Run locally:

```bash
./gradlew :chart-core-benchmarks:jmh --no-configuration-cache
```

(`--no-configuration-cache` avoids a known interaction between the JMH Gradle plugin and Gradle configuration cache in this repo.)

**Variance:** JMH results depend on CPU governor, thermal state, and background load. Treat numbers as **relative** (before/after on the same machine) unless you pin hardware and idle the system. CI baselines for microbenchmarks are **not** wired by default—run manually before releases or when changing visible-range or decimation code.

## Hierarchical treemap

### Layout (JMH)

`TreemapLayoutBenchmark` (`chart-core-benchmarks/src/jmh/kotlin`) times `ChartPlanner.planTreemapHierarchy` on a
1080 x 1400 px canvas with labels and a value formatter, for 100, 500 and 2,000 nodes, flat and two-level (about √n
folders with the files spread over them), with and without minimum-tile aggregation (`minTileSidePx` 0 and 126, that
is 48 dp at 420 dpi). Sizes follow a Zipf-like long tail. Text is measured with the fixed-advance
`TreemapLabelMeasurer.estimate`, so these numbers are the layout algorithm alone.

Measured 2026-11-11 on an Apple M3 Pro (12 cores, 18 GB), macOS 27, OpenJDK 17.0.14, JMH average time, 5 x 1 s
iterations after 3 x 1 s warm-up:

| Nodes | Shape | No aggregation | `minTileSidePx = 126` |
|------:|-------|---------------:|----------------------:|
| 100 | flat | 30 µs | 6 µs |
| 100 | two-level | 27 µs | 15 µs |
| 500 | flat | 47 µs | 12 µs |
| 500 | two-level | 59 µs | 20 µs |
| 2,000 | flat | 109 µs | 34 µs |
| 2,000 | two-level | 179 µs | 29 µs |

Aggregation is faster, not slower, because far fewer tiles are drawn and labeled.

### Layout plus first draw on Android

The showcase's **Treemap performance** screen builds a 500-node, two-level treemap (no aggregation, labels on) and
measures the time from composing the chart to the first frame that draws its tiles. That includes composition,
planning with real Compose text measurement, layout, the per-tile accessibility nodes and drawing, plus the frame
that follows `onSizeChanged`. It does so once cold (the first chart after the activity starts) and ten more times warm
(recreated with a new spec, so the plan and text cache start empty), and also times the planner alone. Run it on a
device with the non-debuggable `benchmark` build type:

```bash
./gradlew :samples:android-showcase:assembleBenchmark
adb install -r samples/android-showcase/build/outputs/apk/benchmark/android-showcase-benchmark.apk
adb shell am start -W -n com.mena97villalobos.composeplotlib.samples.android/.MainActivity --es destination TreemapPerformance
adb logcat -d -s TreemapPerf:I
```

| Device | Cold first draw | Warm first draw (median of 10) | Planner alone | Same harness, 1 tile (warm) |
|--------|----------------:|-------------------------------:|--------------:|----------------------------:|
| Emulator: Pixel 10 Pro XL image, Android 17, arm64, on an Apple M3 Pro, 60 Hz, 5 runs | 145–500 ms | 45–64 ms | 0.5–1.6 ms | 24–48 ms |
| Mid-range phone | not measured yet | | | |

Against the 100 ms target for 500 tiles: warm, the emulator is well inside it, and about two frames of the warm figure
are the harness (see the one-tile column). Cold is not: the first chart after app start runs the library's code before
it is JIT-compiled. A baseline profile for `chart-compose` is the follow-up (#41). An emulator on a desktop CPU is not a
mid-range phone; record a real device in the empty row before relying on these numbers.

Hot spots found and fixed while measuring: tiles too short for a line of text are now rejected before their label is
measured, and the treemap's `TextMeasurer` keeps up to 512 layouts, so labels measured by the planner are not measured
again on every draw. Neither moved the first-draw numbers on the emulator noticeably; both remove repeated work on
later frames.

## Optional CI

A scheduled or non-blocking workflow step could invoke `:chart-core-benchmarks:jmh` if the team accepts flake risk; until machines are pinned, prefer **local** runs plus the unit tests above.
