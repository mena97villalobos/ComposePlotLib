# Style and theme guide

ComposePlotLib splits styling into two layers:

1. **`chart-core`** — data and axis appearance as portable numeric types (`ChartColor`, style data classes on specs). No Compose `Color`; the planner emits colors into `DrawCommand` payloads the same way on every target.
2. **`chart-compose`** — **chrome** around the plot (frame, legend panel, clipping) as `androidx.compose.ui.graphics.Color` via **`ChartChromeTheme`** (`chart-compose/.../theme/ChartChromeTheme.kt`).

Full API surfaces for each chart family live in **[`api-and-state.md`](api-and-state.md)**. This page is the **discoverability** map for colors and strokes.

## `ChartColor` (ARGB in `chart-core`)

`ChartColor` in `chart-core` wraps `0xAARRGGBB` as a `Long`. Predefined companions (`Black`, `White`, `Gray`, `LightGray`, `Blue`, `Green`, `Red`, `Transparent`) match the defaults in `ChartStyles.kt`.

Custom colors — pack ARGB directly, or use the ergonomic factories (all KMP-safe, no platform types):

```kotlin
val brand = ChartColor(0xFF6750A4)          // packed 0xAARRGGBB
val a = ChartColor.rgb(103, 80, 164)        // opaque, 0..255 channels
val b = ChartColor.argb(128, 103, 80, 164)  // with 0..255 alpha
val c = ChartColor.hex("#6750A4")           // "#RGB" / "#RRGGBB" / "#AARRGGBB" (alpha-first)
val d = ChartColor.hsl(258.0, 0.34, 0.48)   // hue 0..360, saturation/lightness 0..1
val faded = brand.withAlpha(0.4)            // replace alpha with a 0..1 fraction
```

Channels are readable as `brand.alpha` / `brand.red` / `brand.green` / `brand.blue` (each `0..255`).

The Compose adapter maps ARGB to `Color` when stroking/filling. Use the same definition on Android and iOS.

## Series and axis styles (specs)

All of these live under `chart-core/.../spec/` and are **immutable** fields on the relevant `*ChartSpec` or nested models.

| Type | Role | Typical home |
|------|------|----------------|
| `LineStyle` | Polyline stroke (`color`, `strokeWidth`), area fill (`areaFill`, `areaFillEnd`, `areaFillMode`) and `interpolation` | `LineSeries.lineStyle` |
| `PointStyle` | Vertex markers (`visible`, `radius`, optional `color`, `shape`) | `LineSeries.pointStyle`, `ScatterSeries.pointStyle` |
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

All chart composables accept an optional `chromeTheme` parameter. When omitted, they use **`chartChromeTheme()`**, a `@Composable` that reads **Material 3** `MaterialTheme.colorScheme` and builds:

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
  - **`MaterialChartPalette` helpers:** map Compose / Material colors to **`ChartColor`** without hand-packing ARGB:
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

## Theming from a non-Material design system

The Material helpers above only read `MaterialTheme.colorScheme`. If your app styles itself with its own color
scheme instead (for example ComposeDesignSystem's `FinanceColorScheme`), map that scheme onto the chart's color slots
yourself. The library deliberately takes no dependency on any design system; everything below is existing public API:
`Color.toChartColor()`, `ChartChromeTheme(...)`, and the color fields on the specs.

### Which role goes where

Pick roles by **meaning**; every design system has some version of each.

| Chart slot | API | Design-system role to use |
|---|---|---|
| Series / category / top-level tile colors | series styles, `BarGroup` colors, `PieChartSlice.color`, `TreemapHierarchySpec.palette`, `SunburstNode.color` | the categorical or accent set (data-viz colors if it has them), at least 3 that are distinguishable from each other **and** reach 3:1 against the chart background |
| Plot border | `ChartChromeTheme.plotBorderColor` | the divider / hairline border role |
| Grid lines | `GridSpec.lineStyle.color` | the divider role, at about half its alpha |
| Axis lines | `AxisSpec.lineStyle.color` | the divider or border role |
| Axis tick labels, legend text | `AxisSpec.labelStyle.color`, `LegendSpec.style.textColor` | the muted / secondary text role (it must still reach 4.5:1 on the background) |
| Legend panel | `ChartChromeTheme.legendBackgroundColor` / `legendBorderColor` | the card or elevated-surface role and its border, or `null` for no panel |
| Treemap tile labels | `TreemapHierarchySpec.labelColor` | leave it `null`: the chart picks black or white per tile for contrast |
| Treemap "smaller items" tile | `TreemapHierarchySpec.aggregateColor` | a neutral fill role (chip or track fill) |
| Positive / negative values | waterfall increase/decrease, candlestick bull/bear | the success and danger roles |

Check text against your background with `ChartColor.contrastRatio(other)` (WCAG 2.x); `readableContentColor()`
returns black or white, whichever reads better on a fill.

### Light and dark

Build the mapping from whichever scheme is current, inside the composable that draws the chart, so a theme switch
recomposes the chart with the matching colors. The chart composables never read your scheme themselves.

```kotlin
/** Your design system's roles, named for what they mean (illustrative; use your scheme's real properties). */
class BrandColors(
    val background: Color,
    val accents: List<Color>,
    val divider: Color,
    val mutedText: Color,
    val card: Color,
)

/** One function from the design-system scheme to everything a chart needs. */
class BrandChartColors(colors: BrandColors) {
    val series: List<ChartColor> = colors.accents.map { it.toChartColor() }
    val chrome =
        ChartChromeTheme(
            plotBorderColor = colors.divider,
            plotBorderStrokeWidthPx = 1f,
            legendBackgroundColor = colors.card,
            legendBorderColor = colors.divider,
            legendBorderStrokeWidthPx = 1f,
            legendCornerRadiusPx = 6f,
            clipCanvasToCanvasBounds = true,
            clipLegendToLegendBounds = true,
        )
    val axisLabels = AxisLabelStyle(color = colors.mutedText.toChartColor())
    val grid = GridSpec(lineStyle = GridLineStyle(color = colors.divider.copy(alpha = 0.5f).toChartColor()))
    val legendText = colors.mutedText.toChartColor()
}

@Composable
fun StorageTreemap(spec: TreemapHierarchySpec, colors: BrandColors) {
    val chart = remember(colors) { BrandChartColors(colors) }
    TreemapChart(spec = spec.copy(palette = chart.series), chromeTheme = chart.chrome)
}
```

`colors` comes from your theme's composition local (light or dark). A compiled copy of this adapter, with a contrast
check for both modes, lives in the showcase tests (`DesignSystemColorBridgeTest`).

## Roborazzi theme matrix (starter)

Android showcase JVM tests include a small **line showcase** matrix:

| Test | Theme / size |
|------|----------------|
| `lineChartShowcaseScreen` | Default `MaterialTheme`, 400×720 dp |
| `lineChartShowcaseScreen_darkMaterialTheme` | `MaterialTheme(colorScheme = darkColorScheme())`, 400×720 dp |
| `lineChartShowcaseScreen_compactWidth` | Default Material, **320×720** dp (narrow width) |
| `lineChartShowcaseScreen_elevatedFontScale` | Default Material, **fontScale 1.4**, 400×720 dp |

Goldens live under `samples/android-showcase/src/test/resources/roborazzi/`. After intentional UI changes, run `./gradlew :samples:android-showcase:recordRoborazziDebug`; CI uses `verifyRoborazziDebug`. See **`docs/showcase-guide.md`**.

## Font scale and label density

Tick gaps, axis insets, and legend spacing account for **accessibility font scale** via `ChartLayoutInput.labelLayoutScale` (built-in charts set this from `LocalDensity.fontScale`). This affects **layout**, not the ARGB values in `AxisLabelStyle`. See **Layout and text measurement** in [`roadmap.md`](roadmap.md).

## Related docs

- **[`api-and-state.md`](api-and-state.md)** — parameters and spec fields per chart
- **[`marker-tooltip-placement.md`](marker-tooltip-placement.md)** — marker/tooltip visuals use your composable; placement uses Dp gaps from spec defaults
- **[`axis-category-time-cookbook.md`](axis-category-time-cookbook.md)** — axis tick density and formatters (orthogonal to color)
