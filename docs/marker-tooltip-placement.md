# Marker and tooltip placement

Selection markers are optional composables (`marker` parameter) on `LineChart`, `BarChart`, `ScatterChart`, `CombinedCartesianChart`, and `PieChart`. The library positions them near the selection anchor in **chart (canvas) pixels**, using the measured size of the marker after layout.

## Public API

- **`ChartMarkerPlacement`** — `verticalGap` (`Dp`), `containment` (`ChartMarkerContainment`), `preferAbove`, and optional **`callout`** (`ChartMarkerCalloutLeader`). Default: 12 dp gap, plot bounds, prefer above, no leader (`ChartMarkerPlacement.Default`).
- **`ChartMarkerCalloutLeader`** — When `enabled`, draws a leader polyline **under** the marker (same fade alpha as the marker when selection highlight animation is on). Default built-in geometry is an **elbow** from the anchor to the nearest horizontal edge midpoint of the tooltip box (`ChartMarkerCalloutLeader.DefaultElbow`). Alternatively, set **`customCallout`** to a **`ChartMarkerCustomCallout`** or use **`ChartMarkerCalloutLeader.custom { … }`** to supply your own list of chart-pixel points (straight line, bent path, multiple segments). Configure `strokeWidth` and `color` on the leader data class.
- **`ChartMarkerCustomCallout`** — Functional interface: `polyline(anchorX, anchorY, markerLeft, markerTop, markerWidth, markerHeight)` → list of points in chart pixels; return at least two points to draw a stroke, or empty to skip.
- **`ChartMarkerContainment`** — `PlotBounds` (keep inside `ChartLayoutPlan.plotBounds`), `CanvasBounds` (full canvas), or `None` (legacy: clamp x/y at origin only).
- **`ChartMarkerData`** — Payload for custom markers (selection, labels, optional `accessibilitySummary`).
- **`DefaultChartMarker`** — Optional `showSeriesLabel`, `showCoordinateLine`, `showOutline` for tooltip chrome.
- **Multi-overlay layout helpers** (`com.mena97villalobos.composeplotlib.chart` — `nudgeAxisAlignedRectToAvoidOverlap`, `stackAxisAlignedRectBelow`) for apps building **multiple** floating boxes in chart pixel space; they operate on `chart-core` **`RectD`** axis-aligned rectangles. The stock selection path still uses **one** tooltip; these helpers help custom dashboards that draw several annotations.

## Placement rules (`PlotBounds` / `CanvasBounds`)

1. Try the **preferred** side of the anchor (above if `preferAbove`, else below). A candidate is kept only if the **entire** marker box fits in the containment rectangle.
2. If that fails, try the **other** side.
3. **Clamp** the top-left so the box stays inside containment (if the marker is larger than the region, it is centered on overflow).

Horizontal position is anchor-centered when possible, then clamped.

## Accessibility

When `ChartAccessibilitySpec.exposeSelectionState` is true, charts use **`LiveRegionMode.Polite`** together with `stateDescription` derived from marker copy so selection changes are announced.

When **`ChartAccessibilitySpec.requestFocusOnMarker`** is true and you pass a `marker` composable, the tooltip is a **separate focus / semantics target** from the chart (`mergeDescendants` is off on the chart node), and focus is **requested** on the marker when the selection changes. The marker container uses **unmerged** semantics (`mergeDescendants = false`) so **buttons, links, and other focusable children** inside your custom `marker` content participate in focus traversal and accessibility—Tab / TalkBack can move into the tooltip body after the container receives focus.

## Multiple markers / dense dashboards

The library does not render multiple simultaneous selection tooltips by default. For app-built overlays (e.g. several annotation cards), use **`nudgeAxisAlignedRectToAvoidOverlap`** / **`stackAxisAlignedRectBelow`** with bounding boxes in the same chart-pixel space as `ChartLayoutPlan.plotBounds`, or lay out overlays manually.

## Showcase

The Android **Line chart** showcase includes:

- **High-Y / tooltip edge** demo: tap near the top of the plot so the tooltip flips below when there is no room above. Uses `ChartMarkerPlacement(callout = ChartMarkerCalloutLeader.DefaultElbow)` and `DefaultChartMarker(showOutline = true)`.
- **Custom callout + focusable marker:** `ChartMarkerCalloutLeader.custom` draws a straight segment from the anchor to the box center; the marker includes a **Button** with `requestFocusOnMarker` enabled for keyboard / a11y QA.

## Tests

`MarkerPlacementMathTest` in `chart-compose` (`commonTest`) covers placement math, **elbow** callout points, **custom** callout paths, and **rect nudge / stack** helpers.
