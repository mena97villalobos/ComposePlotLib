# Design: combined (multi-layer) cartesian charts

**Status:** **Implemented** in `chart-core` / `chart-compose` (`CombinedCartesianChartSpec`, `planCombinedCartesianChart`, `CombinedCartesianChart`). This note remains the semantic reference; **secondary Y** is **data-derived** from secondary-bound layers (not independently zoomed via `ChartViewportState` in v1). Right-axis tick labels live on **`CombinedCartesianChartPlan.yTicksSecondary`** (Option A wrapper) and are drawn via appended overlay commands.

**Related:** [`architecture.md`](architecture.md), [`api-and-state.md`](api-and-state.md), [`ChartPlanner.kt`](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/layout/ChartPlanner.kt), [`ChartLayoutPlan.kt`](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/layout/ChartLayoutPlan.kt).

## 1. Problem and goals

**Combined charts** overlay multiple cartesian series types (e.g. line + bar, or multiple lines with different *Y* scales) in one plot while preserving:

- One shared planning → draw pipeline (`ChartPlanner` → `DrawCommand` → `ComposeCanvasAdapter`)
- Correct **z-order** (what draws on top at pixel overlaps)
- Consistent **hit testing** and **selection** semantics (which layer “wins” a tap)
- **Shared *X*** domain/viewport in the common case, with an optional **secondary *Y*** axis (*dual axis*) when layers need incompatible *Y* scales

The existing stack already factors a reusable **cartesian frame** inside `ChartPlanner` (`layoutCartesianFrame` → `CartesianFrame`: canvas, viewport, plot bounds, transform, ticks, legend placement). Each of `planLineChart`, `planScatterChart`, and `planBarChart` builds one `ChartLayoutPlan` per call. That is **one layer of plot semantics per plan**. Supporting combined charts means **composing multiple layers** into a **single** layout/plan suitable for one composable surface.

## 2. Current architecture (baseline)

- **`ChartLayoutPlan`** holds `plotDrawCommands` (under axis labels) and `overlayDrawCommands` (axes + tick text on top), plus ticks, legend entries, and bounds. See [`ChartLayoutPlan.kt`](../chart-core/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/core/layout/ChartLayoutPlan.kt).
- **Per-family plans** wrap that layout: `LineChartPlan`, `ScatterChartPlan` (`layout` + `selection`); `BarChartPlan` adds `barSegments` and **`barSegmentsForHitTest()`** (topmost-first for taps, reverse of draw order). This pattern is the template for **layer-local hit geometry**.
- **Viewport** (`ChartViewport`) today describes one domain window; **one `ChartTransform`** maps domain ↔ plot pixels for cartesian charts.
- **Pie** uses a separate radial path (`planPieChart`); it must **not** be composed with cartesian in early work (see non-goals).

## 3. Axis binding modes

| Mode | *X* axis | *Y* axis | Viewport / transform |
|------|----------|----------|----------------------|
| **Shared axes** | One spec, one tick pass | One spec, one tick pass | Single `ChartViewport`, single `ChartTransform`; all layers map *Y* through the same scale |
| **Dual *Y* (secondary axis)** | One shared spec and tick pass | Primary left (or configurable side) + **secondary** right | Shared *X* window; **two *Y* domain ranges** (primary + secondary), two *Y*→pixel maps while *X*→pixel is shared. Secondary tick values and labels are planned in an extended chrome pass (see §6) |

**Rules:**

- **Never** mix unrelated *X* scales on one plot in v1; combined charts assume **one *X* domain** per surface.
- Secondary *Y* is optional and **per combined chart**, not per arbitrary layer if that implies N axes; cap at **two *Y* scales** for the first implementation unless product needs more later.

### 3.1 Secondary *Y* “viewport” policy (v1)

- The **secondary *Y* domain** is computed in `chart-core` on **each** `planCombinedCartesianChart` call from the **min/max of all *Y* values** in layers bound to `SecondaryY` (plus the same degenerate-span padding as other cartesian families; include `0` when any secondary bar layer participates, matching `combinedSecondaryViewport` in `ChartPlanner`).
- There is **no** separate secondary-**Y** pan/zoom in `ChartViewportState`: changing the shared **X** window (pan/zoom) or the **spec** data triggers a fresh secondary range and tick pass. Typing for the right axis (`DrawCommand.Text.isSecondaryYAxisTick`) is wired in the Compose adapter (`yAxisSecondary.labelStyle`).

## 4. Data model (spec, immutable)

The shipped top-level spec is **`CombinedCartesianChartSpec`** with a sealed **`CombinedCartesianLayer`** (`Line` / `Bar` / `Scatter`):

- **`layers`**: ordered list of **`CombinedCartesianLayer`** entries, each carrying:
  - **kind**: line / bar / scatter (reuse existing series + style types where possible, e.g. `LineSeries` + line-render options for a line layer)
  - **yAxisBinding**: the enum **`CombinedCartesianYAxisBinding`** (`SharedY` | `SecondaryY`)
  - **id**: stable string for legend and selection targeting
- **`xAxis`**, **`yAxis`**: primary axes (same role as today)
- **`yAxisSecondary`**: optional `AxisSpec` (typically enabled on the “end” side, e.g. right); when absent, all layers use **`SharedY`**
- **`legend`**: one merged legend; **entries** aggregate per-layer series labels/colors (order follows layer order unless a dedicated `LegendSpec` extension defines grouping)
- **`grid`**, **`contentPadding`**, **`selectionStyle`**, accessibility, etc.: shared at combined level, with per-layer overrides only if a clear use case appears (default: **no** per-layer grid in v1—use combined-level grid tied to primary ticks)

**Principle:** prefer **reuse** of `LineChartSpec` / `BarChartSpec` / `ScatterChartSpec` *building blocks* (series lists + family-specific options) inside each layer rather than duplicating fields, without nesting full independent specs (which would imply duplicate axes and padding).

## 5. Planner API

The shipped planner entry point is:

```kotlin
fun planCombinedCartesianChart(
    input: ChartLayoutInput,
    spec: CombinedCartesianChartSpec,
    selection: ChartSelection?,
    reserveRightYAxisSpace: Boolean = true,
    tickValuePass: TickValuePass? = null,
): CombinedCartesianChartPlan
```

**`CombinedCartesianChartPlan`** carries:

- **`layout`**: a single **`ChartLayoutPlan`** for the whole chart (renders through the same `ComposeCanvasAdapter` path as single-family plans)
- **`hitTestLayers`**: list of layer-scoped `CombinedHitTestLayer` structures for hit testing (see §7), e.g. bar segments per bar layer, scatter points, line polylines / nearest-point metadata; `getAllBarSegments()` exposes the flattened bar segments
- **`secondaryViewport`**: the data-derived secondary-*Y* window (`getSecondaryViewport`)
- **`yTicksSecondary`**: the right-axis tick values (Option A wrapper fields, see §8)
- **`selection`**: echo for compose, as today

**Planning steps (conceptual):**

1. **Unify domain for layout**  
   - Derive the **primary** `ChartViewport` window from shared `ChartViewportState` (compose-owned) as today.  
   - For dual *Y*, derive **secondary *Y* span** from all layers bound to `SecondaryY` (data min/max + padding policies aligned with existing axis auto-range behavior, or explicit ranges in spec).

2. **Single chrome pass**  
   - Run **one** `layoutCartesianFrame`-style pass for **primary** *X*/*Y* so plot bounds, primary ticks, and legend bins stay consistent.  
   - If secondary *Y* exists, compute **secondary tick values** using the same plot height but a **secondary `ChartTransform`** for *Y* only (implementation detail: a small helper type for “shared *X* transform + primary *Y* + secondary *Y*” avoids duplicating full `ChartTransform` logic).

3. **Layer plot commands**  
   - For each layer in **declared z-order** (see §6), invoke **existing internal helpers** that today build line/scatter/bar **plot** commands (extract or share the bodies of current `plan*` plot sections) with the **correct transform** (`SharedY` vs `SecondaryY`).  
   - Concatenate into **`plotDrawCommands`** in order: e.g. grid (once, from primary ticks) → layer A → layer B → selection overlay last (or immediately below crosshair, per product choice).

4. **Overlay**  
   - Build **primary** axis lines + tick labels as today.  
   - Append **secondary *Y*** tick labels (and optional secondary axis line) on the opposite side without colliding with legend/reserved regions; may require extending `ChartReservedRegions` or overlay command list conventions.

**Refactor note:** the planner **extracts** “build plot commands for one line/scatter/bar layer given `CartesianFrame`-like inputs” from `ChartPlanner` to avoid duplication, while keeping `planLineChart` etc. as thin wrappers for backward compatibility.

## 6. Z-order and drawing

**Default draw order (bottom → top):**

1. Grid lines (primary tick geometry)
2. Layers in **`spec.layers`** order (first layer bottom, last layer top)—**unless** a future `zIndex` per layer is added; v1 can fix order to list order only.
3. Selection highlight (vertical line, point highlight, bar stroke, etc.)
4. **Overlay**: axes + tick labels + legend (unchanged principle from `ChartLayoutPlan`)

**Bar vs line:** bars often sit **under** lines in product UIs; the layer list order lets callers express that explicitly.

**Groups / clips:** reuse existing `DrawCommand.Group` / clip patterns if a layer needs strict plot clipping; combined planning should emit the same sealed draw commands the adapter already understands.

## 7. Hit testing and selection

The shipped model exposes a sealed **`CombinedHitTestLayer`** (`Lines` / `Bars` / `Scatters`, each carrying a `layerId` + `yAxisBinding`) via **`CombinedCartesianChartPlan.hitTestLayers`** (with `getAllBarSegments()` for the flattened bar geometry), resolved by **`CombinedCartesianHitResolver.resolve(...)`** (and `combinedCartesianRoam` for keyboard roam).

- **Resolver composition:** after a tap, run hit resolvers **topmost-first** (mirrors `BarChartPlan.barSegmentsForHitTest`). Combined plan exposes an ordered list of **(layerId, resolver payload)** or unified geometry so compose can:
  1. Map touch to plot/domain (shared *X*; *Y* resolved against primary or secondary transform depending on layer binding)
  2. Ask the appropriate **`NearestPointResolver`**, **`BarHitResolver`**, or scatter equivalent for that layer
  3. Stop at the first hit above threshold, or define **tie-break** (e.g. prefer point over bar segment)

- **`ChartSelection`:** extend or document **`HighlightTarget`** so selection carries **`layerId`** (and existing series/point indices). Gesture code in `chart-compose` must update selection consistently with multi-layer plans.

- **Accessibility:** merged content description summarizes the active layer + series.

## 8. `ChartLayoutPlan` composition strategy

**Shipped approach:** one **`ChartLayoutPlan`** per combined chart:

- **`plotDrawCommands`**: merged list in z-order (§6)
- **`overlayDrawCommands`**: merged primary + secondary chrome
- **`xTicks` / `yTicks`**: primary ticks remain the main lists; **secondary *Y* ticks** are handled by:
  - **Option A (chosen and shipped):** store in extended fields on the **`CombinedCartesianChartPlan`** wrapper (`yTicksSecondary`) only, with **`ComposeCanvasAdapter`** / combined composable drawing secondary ticks from the wrapper. This keeps `ChartLayoutPlan` stable for single-family charts, versus
  - **Option B (not taken):** widening `ChartLayoutPlan` with a nullable `yTicksSecondary` field, which would reduce special-casing in the adapter at the cost of touching every single-family plan.

Secondary *Y* tick labels are appended to **`overlayDrawCommands`** via `buildRightAxisOverlay`. **`DrawCommand.Text.isSecondaryYAxisTick`** selects the **`yAxisSecondary`** typography/color slot in the Compose adapter (`ChartLabelTextStyles.yAxisSecondaryTick` from `yAxisSecondary.labelStyle`), distinct from primary left *Y* tick labels.

## 9. Compose / state

- **`CombinedCartesianChart`** composable: mirrors `LineChart` measurement pass, then calls `planCombinedCartesianChart`, then reuses the same gesture/selection/marker pipeline with the combined plan. It ships the same parameter surface as `LineChart` / `BarChart` (viewport state, selection state, gesture config, markers, no-data UI, animation policy, chrome theme), plus `CombinedCartesianChartSpec.defaultViewport`.
- **`ChartViewportState`:** single viewport for *X* + primary *Y* in shared mode. In dual mode the secondary range is **derived-only** from data (the simpler option was chosen); independent user pan/zoom of the secondary *Y* axis is not implemented (see §3.1).

## 10. Migration and compatibility

- Existing **`LineChart`**, **`BarChart`**, **`ScatterChart`** remain **unchanged** public entry points.
- New **combined** spec + planner + composable are **additive**.
- Tests: `chart-core` gains **`CombinedCartesianPlannerTest`** (or similar) for tick ordering, command counts, z-order invariants, dual-axis tick sanity, and hit-order fixtures—mirrors existing `ChartPlannerTest` style.

## 11. Explicit non-goals (v1)

- **Pie/donut + cartesian** on one canvas (see [`AGENTS.md`](../AGENTS.md) / [`roadmap.md`](roadmap.md)).
- **More than two *Y* scales** or independent *X* scales per surface.
- **Stacked** bars + lines as a special magic mode beyond what explicit layer specs can express (stacked bar semantics stay in bar planning).
- **Partial layer viewports** (each layer scrolling independently).

## 12. Design decisions and remaining questions

**Resolved:**

1. Secondary *Y*: **data-driven range only** (no user zoom/pan on the secondary axis in v1 — see §3.1 / §9).
4. **`ChartLayoutPlan`** stays minimal; secondary-axis fields live on the **`CombinedCartesianChartPlan`** wrapper (Option A — see §8).

**Still open:**

2. **Legend** collision when many series across layers—reuse current legend layout only, or allow column wrap per layer group?
3. **Grid lines:** primary ticks only, or also faint secondary *Y* grid (usually avoided in product)?

---

*Public API: [`api-and-state.md`](api-and-state.md) → **Combined cartesian**.*
