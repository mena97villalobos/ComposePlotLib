# ComposePlotLib Docs

Implementation-oriented documentation for the library. Start with **`roadmap.md`** (plus **`architecture.md`** and **`api-and-state.md`**) if your goal is shipping quality or understanding open work.

## Recommended reading order
1. **[`roadmap.md`](roadmap.md)** — Current state, near-term priorities, feature backlog, production-hardening workstreams  
2. **[`architecture.md`](architecture.md)** — Module graph, planner → draw pipeline, constraints  
3. **[`api-and-state.md`](api-and-state.md)** — Public API: charts, specs, viewport, selection, gestures  
4. **[`viewport-configuration.md`](viewport-configuration.md)** — `ViewportConstraints`, axis lock, gesture interaction  
5. **[`accessibility-custom-actions.md`](accessibility-custom-actions.md)** — `chartViewportAccessibilityActions` + app custom actions  
6. **[`axis-category-time-cookbook.md`](axis-category-time-cookbook.md)** — `AxisScale.Category`, `TimeEpochMillis`, formatters, tick density  
7. **[`style-and-theme.md`](style-and-theme.md)** — `ChartColor`, spec styles, `ChartChromeTheme`, Material defaults  
8. **[`marker-tooltip-placement.md`](marker-tooltip-placement.md)** — Selection marker placement, containment, accessibility  
9. **[`chart-families-overview.md`](chart-families-overview.md)** — Short orienter per shipped family + links to detail  
10. **[`module-guide.md`](module-guide.md)** — What belongs in `chart-core` vs `chart-compose` vs sample apps  
11. **[`showcase-guide.md`](showcase-guide.md)** — Android showcase layout, Roborazzi, extending demos  
12. **[`api-checklist.md`](api-checklist.md)** — Public API audit / release gate (`chart-core` vs `chart-compose`, leakage checks)

**Chart families today:** **[`chart-families-overview.md`](chart-families-overview.md)** and **`api-and-state.md`** (line, bar, stacked bar, scatter, combined, pie/donut, radar, sankey, heatmap, gauge, candlestick, plus a sparkline wrapper; remaining polish in `roadmap.md`). Combined semantics: **[`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md)**.

## Releases
| File | Purpose |
|------|---------|
| [`release-notes-1.2.0.md`](release-notes-1.2.0.md) | 1.2.0 feature list grouped by theme, with entry points |
| [`migration-1.1-to-1.2.md`](migration-1.1-to-1.2.md) | Upgrading 1.1.0 → 1.2.0 (recompile notes, `ComposePlotApp()` removal, Sankey changes) |
| [`../CHANGELOG.md`](../CHANGELOG.md) | Full per-version Added / Changed / Fixed / Removed log |

## Reference and long-range scope
| File | Purpose |
|------|---------|
| [`roadmap.md`](roadmap.md) | Current state, near-term priorities, feature backlog by theme, production-hardening workstreams, showcase and docs gaps |
| [`performance-benchmarks.md`](performance-benchmarks.md) | JMH module (`chart-core-benchmarks`), local run instructions, variance / CI expectations |
| [`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md) | Combined cartesian design reference (shared vs dual *Y*, z-order, hit testing, `ChartLayoutPlan` composition); update when combined semantics change |
| [`api-checklist.md`](api-checklist.md) | Release public-API audit checklist |
| [`accessibility-custom-actions.md`](accessibility-custom-actions.md) | Merging viewport TalkBack actions with app custom actions |

## CI and publishing
| File | Purpose |
|------|---------|
| [`github-ci-packages-and-pages.md`](github-ci-packages-and-pages.md) | GitHub Actions PR/release workflows, GitHub Packages coordinates, Pages setup for Dokka |

## Platform runbooks
| File | Purpose |
|------|---------|
| [`ios-library-usage.md`](ios-library-usage.md) | Consuming the library from a KMP+iOS app (framework + SwiftUI bridge) |
| [`ios-gesture-validation.md`](ios-gesture-validation.md) | Gesture checklist on iOS hosts |

## For AI agents
Read first: **[`../AGENTS.md`](../AGENTS.md)** (including **Obligations**: add a **showcase screen** and keep **`docs/*`** in sync when adding chart families), then **[`roadmap.md`](roadmap.md)**, **`architecture.md`**, and **`api-and-state.md`**.

Open work and priorities live in **[`roadmap.md`](roadmap.md)**.
