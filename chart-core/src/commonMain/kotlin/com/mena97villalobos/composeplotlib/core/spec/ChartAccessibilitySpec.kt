package com.mena97villalobos.composeplotlib.core.spec

/**
 * Immutable accessibility hints for chart surfaces. Values are plain strings so they stay
 * platform-agnostic in [chart-core]; [chart-compose] maps them to Compose semantics.
 */
data class ChartAccessibilitySpec(
    /**
     * When non-null, replaces the default auto-generated chart summary for [contentDescription].
     */
    val contentDescription: String? = null,
    /**
     * When false, selection changes do not update semantics `stateDescription` on the chart.
     */
    val exposeSelectionState: Boolean = true,
    /**
     * When true, the chart uses a richer semantic **Image** role (via Compose) and participates in focus
     * traversal for keyboard/switch users.
     */
    val requestFocusForAccessibility: Boolean = false,
    /**
     * Hint for host UI: request keyboard-accessible viewport control (arrow pan, + / − zoom, Escape to clear
     * tap selection when applicable). Implementations attach shortcuts on **cartesian** surfaces with pan/zoom;
     * radial charts typically ignore viewport keys.
     */
    val keyboardViewportNudgeEnabled: Boolean = false,
    /**
     * When true, **Shift+arrow** keys move the tap selection (point/slice/category roam) instead of only
     * panning/zooming/clearing. **Unmodified arrows** still nudge the viewport when [keyboardViewportNudgeEnabled]
     * is on. Implementations use **Shift+Left/Right** for domain steps and **Shift+Up/Down** to switch series on
     * line/scatter multi-series charts; bar charts use Left/Right along visual X order; combined charts walk a
     * flat layer order (see accessibility docs).
     */
    val keyboardSelectionRoamEnabled: Boolean = false,
    /**
     * When true (and a `marker` composable is provided), the selection marker is a **separate focus target**
     * from the chart: descendant semantics are not merged into the chart node, and focus moves to the marker
     * container when the selection changes so screen-reader and keyboard users can read tooltip content.
     */
    val requestFocusOnMarker: Boolean = false,
    /**
     * Live-region behavior when [exposeSelectionState] is true. Use [ChartLiveRegionMode.Off] to avoid
     * announcements for dynamic `stateDescription` updates.
     */
    val selectionLiveRegionMode: ChartLiveRegionMode = ChartLiveRegionMode.Polite,
    /**
     * When true and [contentDescription] is null, the chart's auto-generated summary is prefixed with a
     * data narrative (per-series point count, min/max range, and first→last trend) produced by
     * `chart-core` (see `lineChartNarrative` / `scatterChartNarrative` / `barChartNarrative`). Currently
     * applied to line, scatter, and bar charts. Off by default to keep the default summary terse.
     */
    val includeDataNarrative: Boolean = false,
)
