package com.mena97villalobos.composeplotlib.core.viewport

/**
 * **Stable public API** — bounds on each axis **visible span** after pinch-zoom in [ViewportState.zoom].
 *
 * Configure via `rememberChartViewportState` (in `chart-compose`) or by
 * building [ViewportState] in chart-core tests/tools. Values apply on every zoom and after programmatic
 * viewport updates that go through [ViewportState].
 *
 * UI layers mirror the same values into `ChartViewportState`
 * so apps can cap maximum zoom-out or minimum zoom-in without touching gesture math.
 *
 * See **docs/viewport-configuration.md** in the repo for end-to-end usage (constraints + axis lock + gestures).
 *
 * On each clamp pass, requested min/max spans are intersected with what the **domain** allows on that axis:
 * the effective minimum span is at most the domain span, and the effective maximum span is at most the
 * domain span (so zoom-out cannot target a window wider than the data extent).
 *
 * @property minXSpan Lower cap on the visible x span; must be positive, very small values are raised to a safe
 * epsilon. Defaults to `1e-6`.
 * @property maxXSpan Upper cap on the visible x span; use [Double.POSITIVE_INFINITY] (the default) for no upper limit.
 * @property minYSpan Lower cap on the visible y span; must be positive, very small values are raised to a safe
 * epsilon. Defaults to `1e-6`.
 * @property maxYSpan Upper cap on the visible y span; use [Double.POSITIVE_INFINITY] (the default) for no upper limit.
 */
data class ViewportConstraints(
    val minXSpan: Double = 1e-6,
    val maxXSpan: Double = Double.POSITIVE_INFINITY,
    val minYSpan: Double = 1e-6,
    val maxYSpan: Double = Double.POSITIVE_INFINITY,
)
