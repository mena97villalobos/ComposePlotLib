# ComposePlotLib Docs

Documentation for using and extending the library. If you are **using** ComposePlotLib, start with
[Using the library](#using-the-library). If you are **contributing**, start with
[`../CONTRIBUTING.md`](../CONTRIBUTING.md).

## Using the library

Read in this order:

1. **[`chart-families-overview.md`](chart-families-overview.md)** — a short orienter for each of the
   26 shipped families, with links to detail. Start here to find the chart you want.
2. **[`api-and-state.md`](api-and-state.md)** — the public API: charts, specs, viewport, selection,
   gestures. The main reference.
3. **[`style-and-theme.md`](style-and-theme.md)** — `ChartColor`, spec styles, `ChartChromeTheme`,
   Material defaults.
4. **[`axis-category-time-cookbook.md`](axis-category-time-cookbook.md)** — `AxisScale.Category`,
   `TimeEpochMillis`, formatters, tick density.
5. **[`viewport-configuration.md`](viewport-configuration.md)** — `ViewportConstraints`, axis lock,
   gesture interaction.
6. **[`marker-tooltip-placement.md`](marker-tooltip-placement.md)** — selection marker placement,
   containment, accessibility.
7. **[`accessibility-custom-actions.md`](accessibility-custom-actions.md)** —
   `chartViewportAccessibilityActions` plus your app's own custom actions.

Combined-chart semantics (shared vs dual *Y*, z-order, hit testing) live in
[`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md).

## Platform runbooks

| File | Purpose |
|------|---------|
| [`ios-library-usage.md`](ios-library-usage.md) | Consuming the library from a KMP + iOS app (framework + SwiftUI bridge) |
| [`ios-gesture-validation.md`](ios-gesture-validation.md) | Gesture checklist on iOS hosts |

## Releases and upgrading

| File | Purpose |
|------|---------|
| [`release-notes-1.2.0.md`](release-notes-1.2.0.md) | 1.2.0 feature list grouped by theme, with entry points |
| [`migration-1.1-to-1.2.md`](migration-1.1-to-1.2.md) | Upgrading 1.1.0 → 1.2.0 (recompile notes, `ComposePlotApp()` removal, Sankey changes) |
| [`../CHANGELOG.md`](../CHANGELOG.md) | Full per-version Added / Changed / Fixed / Removed log |

## Contributing and internals

| File | Purpose |
|------|---------|
| [`../CONTRIBUTING.md`](../CONTRIBUTING.md) | How to build, test, and get a PR merged |
| [`architecture.md`](architecture.md) | Module graph, planner → draw pipeline, constraints |
| [`module-guide.md`](module-guide.md) | What belongs in `chart-core` vs `chart-compose` vs the sample app |
| [`showcase-guide.md`](showcase-guide.md) | Android showcase layout, Roborazzi, extending demos |
| [`api-checklist.md`](api-checklist.md) | Public API audit / release gate (`chart-core` vs `chart-compose`, leakage checks) |
| [`roadmap.md`](roadmap.md) | Open backlog, production-hardening workstreams, showcase and docs gaps |
| [`performance-benchmarks.md`](performance-benchmarks.md) | JMH module (`chart-core-benchmarks`), local run instructions, variance / CI expectations |
| [`github-ci-packages-and-pages.md`](github-ci-packages-and-pages.md) | CI workflows, publishing coordinates, Pages setup for Dokka |
| [`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md) | Combined cartesian design reference; update when combined semantics change |

## For AI agents

Read [`../AGENTS.md`](../AGENTS.md) first — especially **Obligations** (add a showcase screen and
keep `docs/*` in sync when adding a chart family) — then `architecture.md` and `api-and-state.md`.
Open work lives in [`roadmap.md`](roadmap.md).
