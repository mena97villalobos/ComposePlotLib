# Styling and Theming

ComposePlotLib splits appearance into two layers that never mix:

| Layer | Lives in | Color type | What it covers |
|---|---|---|---|
| **Data / axis style** | `chart-core` specs | `ChartColor` | Lines, bars, points, axes, grid, legend text, selection, data labels |
| **Plot chrome** | `chart-compose` | Compose `Color` | Plot frame, legend panel background/border, clipping |

Rule of thumb: if the planner has to emit it into a `DrawCommand`, it is a `ChartColor` on a spec. If it is
decoration drawn *around* the plan, it is on `ChartChromeTheme`.

The exhaustive per-family reference is
[docs/style-and-theme.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/style-and-theme.md);
this page is the orientation.

## ChartColor: why not Compose Color

`chart-core` is Compose-free by design (see [Core Concepts](Core-Concepts)) — the planner runs in plain unit tests
on the JVM, on Android, and on iOS with no UI toolkit loaded. `androidx.compose.ui.graphics.Color` would drag
Compose into the engine and break that, so `chart-core` ships its own `ChartColor`, a value wrapping `0xAARRGGBB`
as a `Long`.

```kotlin
import com.mena97villalobos.composeplotlib.core.model.ChartColor

val brand = ChartColor(0xFF6750A4)          // packed ARGB
val a = ChartColor.rgb(103, 80, 164)        // opaque, 0..255 channels
val b = ChartColor.argb(128, 103, 80, 164)  // leading 0..255 alpha
val c = ChartColor.hex("#6750A4")           // "#RGB" / "#RRGGBB" / "#AARRGGBB"
val d = ChartColor.hsl(258.0, 0.34, 0.48)   // hue 0..360, s/l 0..1
val faded = brand.withAlpha(0.4)            // alpha as a 0..1 fraction
```

Channels read back as `brand.alpha`, `.red`, `.green`, `.blue` (each `0..255`), and the raw value as `.argb`.
Companions `Black`, `White`, `Gray`, `LightGray`, `Blue`, `Green`, `Red`, `Transparent` back the spec defaults.
Extension functions `ChartColor.lighten(amount)` and `ChartColor.darken(amount)` produce tonal shades.

To bring a Compose or Material color across the boundary, use `Color.toChartColor()` from `chart-compose`:

```kotlin
import androidx.compose.ui.graphics.Color
import com.mena97villalobos.composeplotlib.chart.theme.toChartColor

val accent = Color(0xFF6750A4).toChartColor()
// Color.Unspecified returns the fallback (default ChartColor.Black)
val safe = MaterialTheme.colorScheme.primary.toChartColor(fallback = ChartColor.Blue)
```

## Per-spec style types

All of these are immutable data classes under `com.mena97villalobos.composeplotlib.core.spec`, attached to the
spec or model that owns them.

| Type | Key fields | Attached to |
|---|---|---|
| `LineStyle` | `color`, `strokeWidth`, `areaFill`, `areaFillEnd`, `areaFillMode`, `interpolation` | `LineSeries.lineStyle` |
| `PointStyle` | `visible`, `radius`, `color`, `shape` | `LineSeries.pointStyle`, `ScatterSeries.pointStyle` |
| `BarStyle` | `color`, `barWidth` | `BarGroup.style` |
| `AxisLabelStyle` | `color` | `AxisSpec.labelStyle` |
| `AxisLineStyle` | `visible`, `color` | `AxisSpec.lineStyle` |
| `GridLineStyle` | `color`, `strokeWidth`, `strokeStyle` (dashes) | `GridSpec.lineStyle` |
| `LegendStyle` | `textColor`, `indicatorRadius`, `labelMaxChars` | `LegendSpec.style` |
| `SelectionStyle` | `lineColor`, `lineStrokeWidth`, `pointColor`, `pointRadius` | `LineChartSpec.selectionStyle` and peers |

`MarkerShape` is `Circle`, `Square`, `Triangle`, `Diamond`. `PointStyle.shape` sets the series default; a single
`ChartPoint` can override it with `markerShape`, `markerColor`, and `markerRadius`.

`AreaFillMode` decides where an area fill is anchored: `ToPlotBottom` (default), `ToBaseline(value)`, or
`BelowThreshold(value)` — the last fills only where the line dips under a floor.

```kotlin
LineSeries(
    id = "cash",
    label = "Net cash flow",
    points = points,
    lineStyle = LineStyle(
        color = ChartColor(0xFF2166AC),
        strokeWidth = 2.5,
        areaFill = ChartColor(0xFF2166AC).withAlpha(0.25),
        areaFillEnd = ChartColor.Transparent,       // vertical gradient
        areaFillMode = AreaFillMode.ToBaseline(0.0),
    ),
    pointStyle = PointStyle(radius = 4.0, shape = MarkerShape.Diamond),
)
```

Pie slices carry their own color (`PieChartSlice.color`) rather than a `LineStyle`/`BarStyle`.

For ready-made color sets, `chart-core` ships `ChartPalettes` (`Tableau10`, `Category10`, `ColorblindSafe`, plus
`cyclic(palette, index)`) and `ColorGradient` (`Viridis`, `Plasma`, `Turbo`, `RdBu`, `BrBG`, with `colorAt(t)` and
`sample(count)`) for sequential and diverging encodings.

## ChartChromeTheme

Every chart composable takes an optional `chromeTheme: ChartChromeTheme`. It controls `plotBorderColor` /
`plotBorderStrokeWidthPx` (set the width to `0` to drop the frame), `legendBackgroundColor`, `legendBorderColor`,
`legendBorderStrokeWidthPx`, `legendCornerRadiusPx`, the two clipping flags, and
`layerCompositingForTranslucentGroups` (default `false`; `true` composites translucent `DrawCommand.Group`s through
a save-layer so overlaps blend once, at some GPU cost).

Three ways to get one:

- `chartChromeTheme()` — `@Composable`, derives chrome from the ambient `MaterialTheme.colorScheme`. This is the
  default when you omit the parameter.
- `chartChromeThemeAuto(darkTheme = isSystemInDarkTheme())` — fixed `Light`/`Dark` presets, for charts outside a
  Material theme.
- `ChartChromeTheme.Default` / `.Light` / `.Dark` — plain constants, ideal for unit and screenshot tests.

There is no global theme singleton. Pass `chromeTheme` per chart when you need branding that ignores `MaterialTheme`.

## Material 3 integration

`chart-compose` bridges Material tokens into `ChartColor` so you do not hand-pack ARGB:

- `materialChartPalette()` — `@Composable` snapshot of the ambient scheme.
- `ColorScheme.toMaterialChartPalette()` — the non-composable form.

Both produce a `MaterialChartPalette` with `primary`, `onPrimary`, `primaryContainer`, `secondary`, `tertiary`,
`error`, `onSurface`, `onSurfaceVariant`, `outline`, `outlineVariant`, `surface`, `surfaceVariant`, `primaryFixed`,
and `secondaryFixed`. It is a useful subset, not a full token bridge.

`MaterialChartPalette.seriesAccentColors()` returns the cycling order used by the spec extensions:
`primary, secondary, tertiary, primaryFixed, secondaryFixed, error`. The extensions themselves are:

| Extension | Receiver | Effect |
|---|---|---|
| `withMaterialSeriesColors(palette, themeSelection = true)` | `LineChartSpec` | Line + point color per series; optionally recolors `selectionStyle` to `primary` |
| `withMaterialScatterSeriesColors(palette, themeSelection = true)` | `ScatterChartSpec` | Point color per series; same selection option |
| `withMaterialGroupColors(palette)` | `BarChartSpec` | Bar fill per group |
| `withMaterialGroupColors(palette)` | `StackedBarChartSpec` | Bar fill per group |

```kotlin
@Composable
fun RevenueChart(spec: LineChartSpec) {
    val palette = materialChartPalette()
    LineChart(
        spec = spec.withMaterialSeriesColors(palette),
        modifier = Modifier.fillMaxWidth().height(240.dp),
    )
}
```

All four return a new spec — topology is untouched, only colors change.

### material3 dependency caveat

`ColorScheme` appears in the public signature of `toMaterialChartPalette()`, and `chartChromeTheme()` reads
`MaterialTheme`. In **1.2.0** `chart-compose` still declared `org.jetbrains.compose.material3:material3` as
`implementation`, so you must declare it yourself in the module that calls those helpers (see
[FAQ and Troubleshooting](FAQ-and-Troubleshooting)). On `main` it — along with `compose.runtime`,
`compose.foundation` and `compose.ui` — is an **`api`** dependency, which will put material3
**1.11.0-alpha07** (the build pinned by Compose Multiplatform 1.11.1; the JetBrains material3 1.10+ line has no
stable release) on your compile classpath, where an incompatible material3 version in your app can conflict.
If you do not use Material, the rest of the library still works: pass `ChartChromeTheme.Light` / `.Dark`
explicitly and skip the palette helpers entirely.

## Dark theme

Two halves, handled differently:

- **Chrome** follows Material automatically when you use `chartChromeTheme()` inside a `MaterialTheme` that swaps
  `colorScheme`, or you can pick `chartChromeThemeAuto()` / `ChartChromeTheme.Dark`.
- **Data colors are yours.** The planner never auto-shifts series colors for dark mode. Either recompute the spec
  from `materialChartPalette()` (which already reflects the active scheme), or lift a fixed palette with
  `ChartPalettes.forDarkBackground(ChartPalettes.Tableau10, amount = 0.2)`, which tints each color toward white.

## Data labels

Per-mark value labels are opt-in via `DataLabelsSpec`, available on `LineChartSpec.dataLabels`,
`BarChartSpec.dataLabels`, and `ScatterChartSpec.dataLabels`.

```kotlin
spec.copy(
    dataLabels = DataLabelsSpec(
        enabled = true,
        valueFormatter = { v -> "${v.toInt()}%" },
        placement = DataLabelPlacement.Above,
        color = ChartColor.Gray,
        offsetPx = 8.0,
        avoidOverlap = true,
    ),
)
```

`DataLabelPlacement` is `Above`, `Below`, `Left`, `Right`, `Center`. With `avoidOverlap = true` (the default) the
planner drops labels that would collide with an already-placed one or spill outside the plot, so the survivors stay
readable on dense series.

## See also

- [docs/style-and-theme.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/style-and-theme.md) — full style reference and the Roborazzi theme matrix
- [docs/api-and-state.md](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/api-and-state.md) — spec fields per chart family
- [Core Concepts](Core-Concepts) — why `chart-core` stays Compose-free
- [Chart Gallery](Chart-Gallery) — all 26 families
