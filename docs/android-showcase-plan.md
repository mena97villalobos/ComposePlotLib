# Android Showcase App Plan

**Status:** The **`samples:android-showcase`** module exists with **line, bar, scatter, and pie/donut** showcase screens, sample data, **Roborazzi** goldens (including `pieChartShowcaseScreen` and cartesian screens), and manual QA workflows. Root **`ShowcaseApp`** opens a **home** screen with buttons that navigate to each demo (line, bar, scatter, pie, domain axes), with a top **Back** control on detail screens; use this document as a **target information architecture** when expanding toward the fuller sectioned structure below.

## Goal
Create a separate Android application for `ComposePlotLib` that demonstrates the library with polished examples, reproducible datasets, and documentation-friendly screens.

This app should not be a dumping ground for temporary experiments. It should be a curated product surface that helps users discover the library and helps maintainers verify features visually.

## Why A Separate App
MPAndroidChart includes many useful demos in `MPChartExample`, but it is organized as a large list of sample activities with many small variations. For `ComposePlotLib`, a dedicated showcase app should be easier to navigate and more intentional.

Benefits:

- Better onboarding for library users
- Stable examples for the documentation
- Visual regression targets
- Easier performance validation
- A place to demonstrate theming and accessibility

## Recommended Module
Create a dedicated Android-only module:

- `:samples:android-showcase`

This module should depend on the public chart modules, not internal implementation packages.

## Recommended Navigation Structure

### Home
Provide grouped navigation by use case rather than by implementation detail.

Suggested sections:

- Basics
- Styling
- Interaction
- Realtime and performance
- Advanced compositions
- Accessibility and theming

### Basics
Screens:

- Basic line chart
- Basic bar chart
- Basic pie or donut chart
- Basic scatter chart

### Styling
Screens:

- Multi-series line chart
- Filled or gradient line chart
- Grouped bars
- Stacked bars
- Legend and axis customization
- Tooltip and selection customization

### Interaction
Screens:

- Pan and zoom
- Visible range controls
- Programmatic viewport changes
- Selection and highlighting

### Realtime and Performance
Screens:

- Realtime append points
- Large dataset line chart
- Large dataset bar chart
- Optional sampled vs unsampled comparison

### Advanced Compositions
Screens:

- Combined chart
- Chart embedded in scroll content
- Chart inside tabs or pager
- Dashboard-like screen with multiple charts

### Accessibility and Theming
Screens:

- Light and dark theme comparison
- Dynamic color example
- Reduced motion example if animations are optional
- Accessibility labels and contrast example
- Empty, loading, and error state examples

## Dataset Strategy
Use committed datasets instead of only random values.

Recommended dataset types:

- Time series
- Categorical revenue or sales data
- Financial candle data
- Sensor-like streaming data
- Comparison datasets for multi-series examples

Benefits:

- Deterministic screenshots
- Easier docs maintenance
- Better regression testing
- More realistic examples

## Documentation Integration
Every showcase screen should map directly to docs.

Each screen should include:

- A short purpose statement
- The main API concepts being demonstrated
- A link target or identifier that docs can reference

The docs should include:

- A screen index
- Matching screenshots
- Short code snippets sourced from the showcase app

## Testing Strategy
The showcase app should support:

- Screenshot generation for key screens
- Basic smoke tests for navigation
- Performance checks for large datasets
- Theming verification

## Implementation Order

1. Create the Android showcase module. *(Done.)*
2. Build the home screen and navigation groups. *(Partial: thin `ShowcaseApp` root instead of grouped home.)*
3. Add basic line, bar, and **scatter** chart screens. *(Done: `LineChartShowcaseScreen`, `BarChartShowcaseScreen`, `ScatterChartShowcaseScreen`.)*
4. Add styling and interaction examples.
5. Add performance and realtime examples.
6. Add advanced composition screens.
7. Add screenshots and docs links. *(Partial: Roborazzi for current screens.)*

## Minimum Launch Set
Before the first public release, the showcase app should have at least:

- Home screen *(or equivalent entry; today: `ShowcaseApp` with chart switcher)*
- Basic line chart *(shipped)*
- Basic bar chart *(shipped)*
- Basic scatter chart *(shipped)*
- Pie or donut chart *(shipped in `ShowcaseApp`; deeper radial demos still optional)*
- Realtime line chart
- Performance screen
- Styling screen
- Accessibility or theming screen

## Long-Term Enhancements
Possible later additions:

- In-app code snippet viewer
- Device form factor adaptations
- Tablet layouts
- Benchmark mode
- Internal debug overlays for bounds and ticks
