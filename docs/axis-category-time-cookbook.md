# Category and time axes cookbook

This guide describes how **cartesian** charts (line, bar, scatter) use `chart-core` axis scales and formatters. See also **`api-and-state.md`** and **`viewport-configuration.md`**.

## Model overview

- **`AxisScale.Linear`** — “nice” numeric ticks via `AxisSpec.tickCount` and optional `tickMin` / `tickMax` hints intersected with the viewport.
- **`AxisScale.Category`** — tick **values** are consecutive indices `0 … categories.lastIndex`. Labels come from the `categories` strings (not from `labelFormatter`). Pan/zoom on X uses the same domain math as linear axes; only visible indices get ticks.
- **`AxisScale.TimeEpochMillis`** — X or Y values are **Unix epoch milliseconds** as `Double`. Ticks use **duration-based “nice” steps** (seconds, minutes, hours, days, …). Labels go through `AxisSpec.labelFormatter` except for one special case below.

`AxisSpec.tickDensity` and `defaultDensitySpacingPx` still cap how many ticks survive after layout (see `TickDensityReducer`), including category subsampling.

## Category axis

1. Choose a consistent mapping from data to indices: usually `0, 1, 2, …` for each category in order.
2. Set  
   `xAxis = AxisSpec(scale = AxisScale.Category(listOf("Jan", "Feb", …)))`  
   (or Y; most samples use category on X).
3. Use **integer** (or half-integer for grouped bars) **x** values in `ChartPoint` / `BarEntry` that line up with those indices. Fractional viewport ranges still resolve to the floor/ceil index range for ticks.
4. Optional: `tickMin` / `tickMax` intersect with the viewport to restrict which category indices are considered for tick search (useful with wide viewports).
5. **Long labels:** set `categoryLabelMaxChars` on `AxisSpec` so tick **labels** truncate with a `…` suffix (reservation uses the truncated string; full strings remain in `AxisScale.Category` for your own tooltips/legend if needed).

**Bar charts:** Bars often use `BarEntry(0.0, y)`, `BarEntry(1.0, y)`, … or centered bands at `0.5`, `1.5`, … depending on grouping; both work as long as the category indices you need fall inside the viewport.

## Time axis (epoch milliseconds)

1. Store X (or Y) as `Double` epoch ms from your timeline (`instant.toEpochMilliseconds().toDouble()` on JVM, etc.).
2. Set  
   `xAxis = AxisSpec(scale = AxisScale.TimeEpochMillis, tickCount = 6)`  
   `tickCount` is a **target** count for the initial tick pass before density reduction.
3. **Default labels:** If `labelFormatter` is **`DefaultAxisLabelFormatter`** (the default for `AxisSpec()`), the planner uses **`CompactUtcAxisLabelFormatter`**, which renders **`yyyy-MM-dd HH:mm`** in **UTC**. This is deterministic on all KMP targets (no `java.time` in `chart-core`).
4. **Custom labels:** Assign any `(Double) -> String` to `labelFormatter` (e.g. `kotlinx-datetime` with a `TimeZone`) for locale- or zone-aware strings. The planner will **not** replace your formatter.
5. Helpers:
   - **`formatEpochMillisUtcYmdHm(Long)`** — same UTC layout as the compact formatter; safe to reuse in specs or tests.
   - **`DefaultEpochMillisAxisLabelFormatter`** — raw millis string; rarely needed in UI.
6. **Tick policies (`EpochMillisTickPolicy`):** Post-processes nice **tick values** (not labels). Policies compose with `EpochMillisTickPolicy.Combined(listOf(...))` in **list order** (e.g. weekdays then exclusions).
   - **`All`** — no filtering (default).
   - **`WeekdaysUtc`** — drop ticks on Saturday/Sunday in the **UTC** calendar day (`isUtcWeekendCalendarDay`). If too few ticks remain, the planner retries with denser nice steps (bounded).
   - **`WeekdaysFixedOffset(offsetMillisFromUtc)`** — weekend detection uses **shifted** civil days `floor((epochMs + offset) / 86_400_000)` (fixed offset only; **no DST**).
   - **`ExcludeUtcDays(excludedUtcDayIndices)`** — drop ticks whose UTC day index `floor(epochMs / 86_400_000)` is in the set. Apps supply **holiday-like** exclusions (NYSE rules stay in app data).
   - **`SessionUtcMinutes(startMinuteOfDay, endMinuteOfDay)`** — keep ticks whose **UTC** minute-of-day is in `[start, end]` inclusive (`0..1439`). Same calendar day only (no overnight split).
   - **`Combined`** — chain the above in order.

## Locale and time zone (app-level)

`chart-core` only sees numeric epoch milliseconds for tick **values**. Locale and time zone affect **labels** via `AxisSpec.labelFormatter` (and your data pipeline). Keep `java.time` / `kotlinx-datetime` in **app** modules—do not pull them into `chart-core`.

### Copy-paste: `kotlinx-datetime` label formatter

Add the dependency to your **application** Gradle module, not the library:

```kotlin
// build.gradle.kts (app)
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
```

Example: show ticks in `Europe/Paris` local time (same tick *values*, different *strings*):

```kotlin
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val paris = TimeZone.of("Europe/Paris")

val xAxis = AxisSpec(
    scale = AxisScale.TimeEpochMillis,
    tickCount = 6,
    labelFormatter = { v ->
        val local = Instant.fromEpochMilliseconds(v.toLong()).toLocalDateTime(paris)
        "${local.date} ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    },
)
```

**Device locale** (month/day names, 12h/24h) can be folded into the same formatter using `java.text` on Android/JVM or platform-specific APIs on iOS; keep formatting in Compose or a thin expect/actual helper—not in `chart-core`.

### When `WeekdaysUtc` is not enough

`WeekdaysUtc` uses **UTC** boundaries. For “business weekdays in New York,” typical options are:

- Pre-shift your plotted X values into a **synthetic UTC business timeline** and document the mapping, or
- Use `EpochMillisTickPolicy.WeekdaysFixedOffset` with a **fixed** offset (e.g. standard offset for a region), knowing **DST is not modeled**, or
- Use `EpochMillisTickPolicy.ExcludeUtcDays` with UTC day indices from your app’s holiday list, optionally combined with `WeekdaysUtc`, or
- Leave `epochMillisTickPolicy = All` and implement rules in data aggregation, or
- Use a custom `labelFormatter` that clarifies session boundaries while keeping core ticks dense.

Full **exchange calendars** (DST, IANA zones, session open/close in local civil time) are still app-level; [`ExcludeUtcDays`] and [`SessionUtcMinutes`] are small, testable building blocks in `chart-core`.

## Density and overlap

- **`AxisTickDensity.Auto`**, **`MaxTicks`**, **`MinSpacingPx`** apply after the first tick list is built; numeric and time paths re-run nice-step generation when shrinking; category ticks are **evenly subsampled** in index order.
- Label overlap suppression still runs in the planner (`minHorizontalTickLabelGapPx` / `minVerticalTickLabelGapPx` on `AxisSpec`).

## Where to see it in the repo

- **`samples/android-showcase`** — **Domain axes** screen (`DomainAxesShowcaseScreen`): short category + long-label truncation + epoch time + UTC weekday tick policy.
Unit tests: `TickValuePlannerTest`, `UtcEpochAxisFormattingTest`.
