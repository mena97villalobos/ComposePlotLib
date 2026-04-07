# ComposePlotLib Feature Inventory

## Purpose
This document lists the major capability areas from MPAndroidChart that should be considered for `ComposePlotLib`. It is intended to guide scoping, not to force one-to-one API parity.

**Implementation order in this repo:** **scatter** and **pie/donut** *(implemented)* → **combined** (multi-layer planner). See **`production-readiness-next-steps.md` → New Chart Families**.

## Chart Families

### Priority chart families
- Line chart *(implemented)*
- Bar chart *(implemented)*
- Scatter chart *(implemented)*
- Pie or donut chart *(radial; shipped — polish in production-readiness backlog)*

### Secondary chart families
- Horizontal bar chart
- Bubble chart
- Radar chart
- Candlestick chart
- Combined cartesian chart *(shipped — planner/rendering polish in production-readiness backlog)*

## Shared Data Model
Implement reusable data structures for:

- Numeric cartesian points
- Stacked bar values
- Bubble size values
- Candle open/high/low/close values
- Pie slices
- Radar values
- Series metadata
- Optional user payloads
- Aggregate min or max calculations
- Efficient x-domain lookup for highlighting and viewport calculations

## Components
Implement reusable component systems for:

- X axis
- Left Y axis
- Right Y axis
- Grid lines
- Axis lines
- Tick labels
- Limit or reference lines
- Legend
- Description or subtitle text
- Empty state
- Error state
- Loading state
- Marker or tooltip overlays

## Rendering and Layout
Implement shared rendering responsibilities for:

- Chart content bounds
- Axis and legend layout
- Label placement
- Domain-to-pixel transforms
- Clipping
- Highlight overlays
- Layered draw order
- Text measurement abstraction
- Reusable geometry buffers

## Interaction
Implement interaction features for:

- Tap selection *(line, scatter, and bar — line/scatter: nearest point in pixel space; bar: rect hit-testing)*
- Highlighting selected values *(line/scatter: point highlight + crosshair; bar: segment/top-edge stroke)*
- Drag or pan
- Pinch zoom
- Double tap zoom *(optional via `ChartGestureConfig`)*
- Programmatic scroll and zoom *(viewport state APIs)*
- Visible range constraints *(`ViewportConstraints` + `ChartViewportState.updateConstraints`)*
- Optional fling or deceleration *(`ChartGestureConfig` fling policy)*
- Radial rotation if pie or radar rotation is included in scope

## Formatting and Styling
Implement customization surfaces for:

- Axis label formatting
- Value label formatting
- Tooltip content formatting
- Series colors
- Line stroke, point markers, and fills
- Bar fills and stack colors
- Pie slice colors and label style
- Grid and axis style
- Legend appearance
- Highlight style
- Animation timing and easing

## Performance
Plan for the following from the start:

- Visible-range clipping
- Large-series handling
- Stable recomposition boundaries
- Streaming updates
- Optional sampling or decimation
- Deterministic benchmark datasets

## Showcase and Documentation
The library should ship with:

- A separate Android showcase app *(shipped: `samples:android-showcase`)*
- Curated demo datasets
- Reference screens for each chart family *(line, bar, scatter, and pie shipped in showcase; combined still future)*
- Performance demo screens
- Docs linked to showcase examples
- Generated screenshots for the docs

## Recommended v1 Scope
- Line chart *(implemented)*
- Bar chart *(implemented)*
- Scatter chart *(implemented)*
- Pie or donut chart *(shipped; radial polish backlog)*
- Shared axis and legend system
- Selection, highlighting, pan, and zoom
- Android showcase app
- Android and iOS Compose Multiplatform support

## Recommended Post-v1 Scope
- Bubble chart
- Radar chart
- Candlestick chart
- Full combined chart support
- Advanced radial gestures
- Export APIs
- Advanced performance optimizations
