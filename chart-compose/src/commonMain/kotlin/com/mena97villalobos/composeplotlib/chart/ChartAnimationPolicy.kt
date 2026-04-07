package com.mena97villalobos.composeplotlib.chart

/**
 * Animated transitions for programmatic viewport changes, optional gesture chase, selection markers,
 * and optional [LineChart] data morph.
 *
 * Gesture-driven pan/zoom snaps each frame unless [gestureViewportEasingEnabled] (exponential chase).
 * Selection marker motion ([selectionHighlightEnabled]): fades **in** when a point appears and **out** when
 * the selection is cleared, provided [selectionDurationMillis] is positive; with zero duration the marker
 * shows/hides immediately. **[respectSystemReducedMotion]**: when system reduced-motion is on, toggles are
 * forced off and durations to zero. Use [ChartAnimationPolicy.Default] for stable defaults (motion off unless opted in).
 */
data class ChartAnimationPolicy(
    /**
     * When true (default), system reduced-motion / remove-animations settings disable chart motion and
     * zero transition durations inside chart composables.
     */
    val respectSystemReducedMotion: Boolean = true,
    /**
     * When true, programmatic [com.mena97villalobos.composeplotlib.chart.state.ChartViewportState] changes
     * (`panTo`, `zoomTo`, `fitToData`, `reset`, constraint / axis policy updates) ease the **rendered**
     * viewport toward the target. Gesture-driven updates snap each frame unless [gestureViewportEasingEnabled].
     */
    val viewportChangesEnabled: Boolean = false,
    /** Duration for programmatic viewport transitions (milliseconds). */
    val viewportDurationMillis: Int = 280,
    /**
     * When true, the marker slot fades **in** when a selection appears and **out** when it is cleared
     * ([selectionDurationMillis] must be > 0 for a visible fade; otherwise behavior is immediate).
     */
    val selectionHighlightEnabled: Boolean = false,
    val selectionDurationMillis: Int = 200,
    /**
     * When true, pan/pinch/fling updates (gesture origin) **chase** the target viewport each frame instead of
     * snapping; higher [gestureViewportChaseStiffness] reaches the target faster. Off by default (snap).
     */
    val gestureViewportEasingEnabled: Boolean = false,
    /**
     * Per-frame lerp factor toward the gesture target (typical 0.15–0.35 at ~60 Hz). Coerced into `(0, 1)`.
     */
    val gestureViewportChaseStiffness: Float = 0.22f,
    /**
     * When true, [LineChart] interpolates **series point coordinates** between the previous and new
     * [com.mena97villalobos.composeplotlib.core.spec.LineChartSpec] when structure matches
     * ([com.mena97villalobos.composeplotlib.core.spec.canMorphLineSpecs]).
     */
    val dataTransitionEnabled: Boolean = false,
    /** Duration for line data coordinate morph (milliseconds). */
    val dataTransitionDurationMillis: Int = 320,
) {
    companion object {
        /** Stable default instance for composable default parameters. */
        val Default: ChartAnimationPolicy = ChartAnimationPolicy()
    }
}
