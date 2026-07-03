package com.mena97villalobos.composeplotlib.chart.state

/**
 * How the chart viewport target last changed, so compose can snap vs animate the rendered window.
 *
 * Gesture-driven updates (`ChartViewportState.pan` / `zoom`, including fling) snap the rendered
 * viewport immediately. API-driven updates (`panTo`, `zoomTo`, `reset`, `fitToData`,
 * `updateConstraints`, `updateAxisPolicy`) may smooth when chart
 * [com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy.viewportChangesEnabled] is on.
 */
enum class ViewportChangeOrigin {
    /** Direct-manipulation change (pan/zoom/fling); the rendered viewport snaps immediately. */
    Gesture,

    /** API-driven change; the rendered viewport may animate when viewport-change animations are enabled. */
    Programmatic,
}
