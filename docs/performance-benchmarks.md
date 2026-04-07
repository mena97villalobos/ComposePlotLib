# Performance benchmarks and regression checks

This project treats **`chart-core` unit tests** as the primary **correctness and lightweight regression** gate for large-series behavior (`LineRenderPipelineTest`, `ScatterRenderPipelineTest`, `ChartPlannerTest`, `VisibleRangeCalculatorTest`, `LineRenderSpecAutoBudgetTest`). They are not timed benchmarks.

## JVM microbenchmarks (JMH)

The **`:chart-core-benchmarks`** module runs [JMH](https://github.com/openjdk/jmh) on the JVM against the **`chart-core` JVM target** (hot paths such as `VisibleRangeCalculator`, `LineRenderPipeline.prepareIndexed`, `ScatterRenderPipeline.prepareIndexed`).

Run locally:

```bash
./gradlew :chart-core-benchmarks:jmh --no-configuration-cache
```

(`--no-configuration-cache` avoids a known interaction between the JMH Gradle plugin and Gradle configuration cache in this repo.)

**Variance:** JMH results depend on CPU governor, thermal state, and background load. Treat numbers as **relative** (before/after on the same machine) unless you pin hardware and idle the system. CI baselines for microbenchmarks are **not** wired by default—run manually before releases or when changing visible-range or decimation code.

## Optional CI

A scheduled or non-blocking workflow step could invoke `:chart-core-benchmarks:jmh` if the team accepts flake risk; until machines are pinned, prefer **local** runs plus the unit tests above.
