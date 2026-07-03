# ComposePlotLib Docs

Implementation-oriented documentation for the library. Start with **`production-readiness-next-steps.md`** if your goal is shipping quality or planning the next milestone.

## Recommended reading order
1. **[`production-readiness-next-steps.md`](production-readiness-next-steps.md)** — Production gaps, testing, concrete next tasks, chart-family order  
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

**Chart families today:** **[`chart-families-overview.md`](chart-families-overview.md)** and **`api-and-state.md`** (line, bar, stacked bar, scatter, combined, pie/donut, radar, sankey, heatmap, gauge, candlestick, plus a sparkline wrapper; polish in `production-readiness-next-steps.md` → New Chart Families). Combined semantics: **[`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md)**.

## Reference and long-range scope
| File | Purpose |
|------|---------|
| [`performance-benchmarks.md`](performance-benchmarks.md) | JMH module (`chart-core-benchmarks`), local run instructions, variance / CI expectations |
| [`implementation-roadmap.md`](implementation-roadmap.md) | Phased plan; MPAndroidChart-derived scope; chart rollout order |
| [`feature-inventory.md`](feature-inventory.md) | Capability areas to consider vs classic chart libs |
| [`android-showcase-plan.md`](android-showcase-plan.md) | Target showcase IA; module exists with **line / bar / scatter / pie / radar / combined / sankey / heatmap / gauge / candlestick / domain axes** root demos + Roborazzi (full sectioned nav still future work) |
| [`design-combined-cartesian-layers.md`](design-combined-cartesian-layers.md) | Combined cartesian design reference (shared vs dual *Y*, z-order, hit testing, `ChartLayoutPlan` composition); update when combined semantics change |
| [`api-checklist.md`](api-checklist.md) | Release public-API audit checklist (**M16**) |
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
Read first: **[`../AGENTS.md`](../AGENTS.md)** (including **Obligations**: add a **showcase screen** and keep **`docs/*`** in sync when adding chart families), then **`production-readiness-next-steps.md`**, **`architecture.md`**, and **`api-and-state.md`**.

Full automation prompt (**residual mission index** + verification): **[`ai-agent-full-backlog-prompt.md`](ai-agent-full-backlog-prompt.md)**.  
Copy-paste **residual** agent tasks (open work; see file for ID set): **[`ai-agent-milestone-prompts.md`](ai-agent-milestone-prompts.md)**.
