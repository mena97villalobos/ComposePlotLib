package com.mena97villalobos.composeplotlib.chart

/**
 * Tap-to-select (or hit-test) behavior. Independent of pan/zoom ([ChartTransformGesturePolicy]).
 *
 * Hardware note: validate single-tap selection on iOS devices; consumer gestures may differ from Android.
 */
data class ChartTapGesturePolicy(
    val selectionEnabled: Boolean = true,
)

/**
 * Drag/pinch viewport manipulation and double-tap zoom. Does not emit selection taps; see [ChartTapGesturePolicy].
 *
 * [doubleTapZoomInEnabled]: zooms **in** by [doubleTapZoomScale] (> 1) around the double-tap point, subject to
 * [com.mena97villalobos.composeplotlib.core.viewport.ViewportConstraints].
 *
 * [doubleTapZoomOutEnabled]: zooms **out** by [doubleTapZoomOutScale] (< 1) around the double-tap point.
 *
 * [doubleTapResetViewportEnabled]: double-tap inside the plot resets the viewport to the chart domain (same as
 * [com.mena97villalobos.composeplotlib.chart.state.ChartViewportState.reset] / [fitToData]).
 *
 * If more than one double-tap behavior is enabled, resolution order is **reset viewport**, then **zoom out**,
 * then **zoom in**.
 *
 * Hardware note: pinch, two-finger pan, and double-tap should be verified on physical iOS hosts (UIKit touch
 * timing and multi-touch routing can differ from Android).
 */
data class ChartTransformGesturePolicy(
    val enabled: Boolean = true,
    val panXEnabled: Boolean = true,
    val panYEnabled: Boolean = true,
    val zoomXEnabled: Boolean = true,
    val zoomYEnabled: Boolean = true,
    val doubleTapZoomInEnabled: Boolean = false,
    val doubleTapZoomScale: Float = 2f,
    val doubleTapZoomOutEnabled: Boolean = false,
    val doubleTapZoomOutScale: Float = 0.5f,
    val doubleTapResetViewportEnabled: Boolean = false,
)

/**
 * Inertial pan after lift, when [ChartTransformGesturePolicy] pan is active.
 *
 * Velocity is estimated from the last transform pan deltas after a short quiescence ([gestureEndDebounceMs]);
 * this avoids platform-specific pointer-up hooks and works across KMP targets, but is an approximation.
 *
 * Hardware note: tune [minVelocityPxPerSec] and [frictionMultiplier] after manual passes on iOS and Android.
 */
data class ChartFlingGesturePolicy(
    val panFlingEnabled: Boolean = false,
    val gestureEndDebounceMs: Long = 48L,
    val minVelocityPxPerSec: Float = 140f,
    val frictionMultiplier: Float = 0.91f,
    /**
     * Maximum domain pan applied per animation frame as a fraction of the visible span on that axis
     * (reduces jumps if velocity spikes).
     */
    val maxPanPerFrameFraction: Double = 0.12,
)

/**
 * Controls for [LineChart] / [BarChart] gestures: tap (selection), transform (pan/zoom/double-tap zoom),
 * and optional fling (inertial pan).
 *
 * Prefer the **companion object** presets (`CartesianDefault`, `PieDefault`, `readOnly()`, `exploreOnly()`, `cartesianInteractive()`, `cartesianInteractiveFullExploration()`)
 * when you want documented bundles (hosts, iOS QA); use the primary constructor when you need a one-off mix.
 *
 * [ViewportAxisPolicy] on [com.mena97villalobos.composeplotlib.chart.state.ChartViewportState] can still
 * lock an axis to the full domain; that policy disables the corresponding pan/zoom directions in practice.
 */
data class ChartGestureConfig(
    val tap: ChartTapGesturePolicy = ChartTapGesturePolicy(),
    val transform: ChartTransformGesturePolicy = ChartTransformGesturePolicy(),
    val fling: ChartFlingGesturePolicy = ChartFlingGesturePolicy(),
) {
    companion object {
        /**
         * Default for cartesian charts ([LineChart], [BarChart], [ScatterChart], [CombinedCartesianChart]):
         * tap selection on; pan/pinch on; double-tap zoom / reset and fling off.
         */
        val CartesianDefault: ChartGestureConfig = ChartGestureConfig()

        /**
         * Default for [PieChart]: tap slice selection on; transform and fling off.
         */
        val PieDefault: ChartGestureConfig =
            ChartGestureConfig(
                tap = ChartTapGesturePolicy(selectionEnabled = true),
                transform = ChartTransformGesturePolicy(enabled = false),
                fling = ChartFlingGesturePolicy(panFlingEnabled = false),
            )

        /** No tap selection and no pan/zoom/double-tap/fling (static / decorative chart). */
        fun readOnly(): ChartGestureConfig =
            ChartGestureConfig(
                tap = ChartTapGesturePolicy(selectionEnabled = false),
                transform = ChartTransformGesturePolicy(enabled = false),
                fling = ChartFlingGesturePolicy(panFlingEnabled = false),
            )

        /**
         * Pan/pinch (and optional fling) without tap-to-select.
         * Use [transform] / [fling] to tune double-tap or inertial pan.
         */
        fun exploreOnly(
            transform: ChartTransformGesturePolicy = ChartTransformGesturePolicy(),
            fling: ChartFlingGesturePolicy = ChartFlingGesturePolicy(),
        ): ChartGestureConfig =
            ChartGestureConfig(
                tap = ChartTapGesturePolicy(selectionEnabled = false),
                transform = transform,
                fling = fling,
            )

        /**
         * Interactive cartesian preset used by shared hosts: double-tap zoom-in inside the plot
         * (see [ChartTransformGesturePolicy]) and optional inertial pan.
         *
         * Pair with the same [ViewportAxisPolicy] / constraints you ship in production when validating
         * iOS touch parity (`docs/ios-gesture-validation.md`).
         */
        fun cartesianInteractive(
            doubleTapZoomInEnabled: Boolean = true,
            doubleTapZoomOutEnabled: Boolean = false,
            doubleTapResetViewportEnabled: Boolean = false,
            panFlingEnabled: Boolean = false,
        ): ChartGestureConfig =
            ChartGestureConfig(
                tap = ChartTapGesturePolicy(),
                transform =
                    ChartTransformGesturePolicy(
                        doubleTapZoomInEnabled = doubleTapZoomInEnabled,
                        doubleTapZoomOutEnabled = doubleTapZoomOutEnabled,
                        doubleTapResetViewportEnabled = doubleTapResetViewportEnabled,
                    ),
                fling = ChartFlingGesturePolicy(panFlingEnabled = panFlingEnabled),
            )

        /**
         * Cartesian preset with **double-tap zoom in**, **double-tap zoom out**, **double-tap reset**, and **pan fling**
         * enabled — convenient for full manual QA on device (`docs/ios-gesture-validation.md`).
         * Resolution order when multiple double-tap modes are on: reset, then zoom out, then zoom in.
         */
        fun cartesianInteractiveFullExploration(): ChartGestureConfig =
            cartesianInteractive(
                doubleTapZoomInEnabled = true,
                doubleTapZoomOutEnabled = true,
                doubleTapResetViewportEnabled = true,
                panFlingEnabled = true,
            )
    }
}
