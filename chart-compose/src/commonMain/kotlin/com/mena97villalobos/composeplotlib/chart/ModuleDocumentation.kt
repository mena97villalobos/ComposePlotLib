/**
 * **chart-compose** — Compose Multiplatform chart widgets and interaction state.
 *
 * Public entry points:
 * - [LineChart], [BarChart], [ScatterChart], [PieChart], and [CombinedCartesianChart] composables
 * - [ChartViewportState], [ChartSelectionState], and [rememberChartViewportState] / [rememberChartSelectionState]
 * - [defaultViewport], [ChartGestureConfig] (companion presets + [ChartTapGesturePolicy], [ChartTransformGesturePolicy], [ChartFlingGesturePolicy]),
 *   markers ([ChartMarkerData], [DefaultChartMarker], [ChartMarkerPlacement], [ChartMarkerCalloutLeader], [ChartMarkerCustomCallout], [ChartMarkerContainment]; multi-overlay helpers [nudgeAxisAlignedRectToAvoidOverlap], [stackAxisAlignedRectBelow]), empty states ([ChartNoDataUi], [ChartNoDataDefaults], [DefaultNoDataOverlay]),
 *   [ChartAnimationPolicy] (opt-in programmatic viewport easing, optional gesture chase, marker fade-in/out, optional [LineChart] data morph; reduced-motion integration), theme ([MaterialChartPalette], [materialChartPalette], [Color.toChartColor], [seriesAccentColors], [LineChartSpec.withMaterialSeriesColors], [BarChartSpec.withMaterialGroupColors], [ScatterChartSpec.withMaterialScatterSeriesColors]),
 *   [ChartCustomAccessibilityAction], [chartViewportAccessibilityActions], [chartAccessibilityActionsMergeViewport]
 * - [ComposePlotApp] — shared Android/iOS milestone host; must include a preview for each shipped chart family (see root **AGENTS.md** → Obligations)
 *
 * Chart configuration and planning types live in **chart-core** (re-exported via `api`).
 */
package com.mena97villalobos.composeplotlib.chart

@Suppress("unused")
private object ChartComposeModuleDocumentationAnchor
