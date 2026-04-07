# Style and theme guide

ComposePlotLib splits styling into two layers:

1. **`chart-core`** — data and axis appearance as portable numeric types (`ChartColor`, style data classes on specs). No Compose `Color`; the planner emits colors into `DrawCommand` payloads the same way on every target.
2. **`chart-compose`** — **chrome** around the plot (frame, legend panel, clipping) as `androidx.compose.ui.graphics.Color` via **`ChartChromeTheme`** (`chart-compose/.../theme/ChartChromeTheme.kt`).

Full API surfaces for each chart family live in **[`api-and-state.md`](api-and-state.md)**. This page is the **discoverability** map for colors and strokes.

## `ChartColor` (ARGB in `chart-core`)

`ChartColor` in `chart-core` wraps `0xAARRGGBB` as a `Long`. Predefined companions (`Black`, `White`, `Gray`, `LightGray`, `Blue`, `Green`, `Red`) match the defaults in `ChartStyles.kt`.

Custom colors:

```kotlin
val brand = ChartColor(0xFF6750A4)
```

The Compose adapter maps ARGB to `Color` when stroking/filling. Use the same definition on Android and iOS.

## Series and axis styles (specs)

All of these live under `chart-core/.../spec/` and are **immutable** fields on the relevant `*ChartSpec` or nested models.

| Type | Role | Typical home |
|------|------|----------------|
| `LineStyle` | Polyline stroke (`color`, `strokeWidth`) | `LineSeries.lineStyle` |
| `PointStyle` | Vertex circles (`visible`, `radius`, optional `color`) | `LineSeries.pointStyle`, `ScatterSeries.pointStyle` |
| `BarStyle` | Bar fill and column width | `BarGroup.style` |
| `SelectionStyle` | Crosshair / highlight stroke and selected point | `LineChartSpec.selectionStyle`, bar/scatter/combined/pie specs |
| `AxisLabelStyle` | Tick label color | `AxisSpec.labelStyle` |
| `AxisLineStyle` | Baseline visibility and color | `AxisSpec.lineStyle` |
| `GridLineStyle` | Major grid stroke (`color`, `strokeWidth`, optional dashed `strokeStyle`) | `GridSpec.lineStyle` |
| `LegendStyle` | Legend text and swatch radius; optional **`labelMaxChars`** ellipsizes legend labels (planner) | `LegendSpec.style` |

**Pie / donut:** slice fill uses `PieChartSlice.color` (`ChartColor` per slice), not `LineStyle` / `BarStyle`.

**Combined cartesian:** each layer carries the same style types as its standalone counterpart (e.g. line layers use `LineStyle` + `PointStyle` on the embedded series).

Defaults lean on the `ChartColor.*` companions so a minimal spec is visibly usable without tuning.

## Plot chrome (`ChartChromeTheme`)

Cartesian and pie composables accept an optional `chromeTheme` parameter. When omitted, they use **`chartChromeTheme()`**, a `@Composable` that reads **Material 3** `MaterialTheme.colorScheme` and builds:

- Plot border color/width (set width to `0` to hide the frame).
- Legend panel fill, border, and corner radius (`null` fill or zero border width turns those off).
- Flags to clip drawing to canvas / legend bounds (usually left on).
- **`layerCompositingForTranslucentGroups`** (default `false`) — when `true`, `DrawCommand.Group` with fractional alpha uses a canvas save-layer so overlapping translucent children blend in one pass (slightly more GPU work).

For unit tests or hosts without Material, use **`ChartChromeTheme.Default`**.

There is **no** global singleton theme in the library: pass `chromeTheme` per chart if you need static branding independent of `MaterialTheme`.

## Light, dark, and dynamic color

- **Chrome** follows Material when you use `chartChromeTheme()` inside a `MaterialTheme` that switches `colorScheme` (light/dark/dynamic).
- **Data colors** (`ChartColor` on series, bars, pie slices, selection) are **your** responsibility. Common patterns:
  - Hard-code `ChartColor` constants for brand palettes.
  - **`MaterialChartPalette` helpers (M13):** map Compose / Material colors to **`ChartColor`** without hand-packing ARGB:
    - `Color.toChartColor(fallback = …)` — converts via Compose `Color.toArgb()` into sRGB ARGB (color-space conversion when needed); **`Color.Unspecified`** returns `fallback` (default `ChartColor.Black`).
    - `ColorScheme.toMaterialChartPalette()` / `materialChartPalette()` — snapshot of Material 3 tokens as **`ChartColor`**: `primary`, `onPrimary`, `primaryContainer`, `secondary`, `tertiary`, `error`, `onSurface`, `onSurfaceVariant`, `outline`, `outlineVariant`, plus **`surface`**, **`surfaceVariant`**, **`primaryFixed`**, **`secondaryFixed`** (tonal accents and surfaces—still not a full token bridge).
    - Optional spec helpers ([`MaterialChartSpecTheme.kt`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/theme/MaterialChartSpecTheme.kt)): **`seriesAccentColors()`**, **`LineChartSpec.withMaterialSeriesColors`**, **`BarChartSpec.withMaterialGroupColors`**, **`ScatterChartSpec.withMaterialScatterSeriesColors`** — cycle accents across series/groups without hand-mapping each style.

    ```kotlin
    val palette = materialChartPalette()
    LineChart(
        spec = spec.withMaterialSeriesColors(palette),
        ...
    )
    ```

The planner does not auto-shift series colors for dark mode.

## Roborazzi theme matrix (starter)

Android showcase JVM tests include a small **line showcase** matrix:

| Test | Theme / size |
|------|----------------|
| `lineChartShowcaseScreen` | Default `MaterialTheme`, 400×720 dp |
| `lineChartShowcaseScreen_darkMaterialTheme` | `MaterialTheme(colorScheme = darkColorScheme())`, 400×720 dp |
| `lineChartShowcaseScreen_compactWidth` | Default Material, **320×720** dp (narrow width) |
| `lineChartShowcaseScreen_elevatedFontScale` | Default Material, **fontScale 1.4**, 400×720 dp |

Goldens live under `samples/android-showcase/src/androidUnitTest/resources/roborazzi/`. After intentional UI changes, run `./gradlew :samples:android-showcase:recordRoborazziDebug`; CI uses `verifyRoborazziDebug`. See **`docs/showcase-guide.md`**.

## Font scale and label density

Tick gaps, axis insets, and legend spacing account for **accessibility font scale** via `ChartLayoutInput.labelLayoutScale` (built-in charts set this from `LocalDensity.fontScale`). This affects **layout**, not the ARGB values in `AxisLabelStyle`. See **Layout and text measurement** in [`production-readiness-next-steps.md`](production-readiness-next-steps.md).

## Related docs

- **[`api-and-state.md`](api-and-state.md)** — parameters and spec fields per chart
- **[`marker-tooltip-placement.md`](marker-tooltip-placement.md)** — marker/tooltip visuals use your composable; placement uses Dp gaps from spec defaults
- **[`axis-category-time-cookbook.md`](axis-category-time-cookbook.md)** — axis tick density and formatters (orthogonal to color)
