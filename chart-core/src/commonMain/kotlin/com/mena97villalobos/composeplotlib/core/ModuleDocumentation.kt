/**
 * **chart-core** — platform-agnostic layer for ComposePlotLib.
 *
 * Responsibilities:
 * - **Specs** ([com.mena97villalobos.composeplotlib.core.spec]): immutable chart configuration (line/bar/scatter,
 *   radial pie via [com.mena97villalobos.composeplotlib.core.spec.PieChartSpec], axes, grid, legend, formatters,
 *   styles, [com.mena97villalobos.composeplotlib.core.spec.ChartAccessibilitySpec]).
 * - **Models** ([com.mena97villalobos.composeplotlib.core.model]): series data, pie slices, and colors.
 * - **Layout** ([com.mena97villalobos.composeplotlib.core.layout]): multi-pass [ChartPlanner] (bounds → ticks →
 *   measure → layout) via [com.mena97villalobos.composeplotlib.core.layout.planning]; emits [ChartLayoutPlan],
 *   [com.mena97villalobos.composeplotlib.core.layout.PieRadialLayout] via [com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath] / [com.mena97villalobos.composeplotlib.core.layout.PieSweep], and
 *   [com.mena97villalobos.composeplotlib.core.layout.DrawCommand] lists.
 * - **Viewport** ([com.mena97villalobos.composeplotlib.core.viewport]): domain ranges, [ViewportConstraints],
 *   [ViewportAxisPolicy], and [ViewportState] math (stable configuration surface for Compose via
 *   `rememberChartViewportState`; see `docs/viewport-configuration.md`).
 * - **Selection** ([com.mena97villalobos.composeplotlib.core.selection]): hit-testing helpers, [ChartSelection], and [com.mena97villalobos.composeplotlib.core.selection.PieHitResolver].
 * - **Geometry / domain** ([com.mena97villalobos.composeplotlib.core.geometry],
 *   [com.mena97villalobos.composeplotlib.core.domain],
 *   [com.mena97villalobos.composeplotlib.core.range]): double-precision primitives — no Compose types.
 *
 * The `chart-compose` module depends on this artifact as `api` and hosts Compose UI.
 */
package com.mena97villalobos.composeplotlib.core

@Suppress("unused")
private object ChartCoreModuleDocumentationAnchor
