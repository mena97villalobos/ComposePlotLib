package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.mena97villalobos.composeplotlib.chart.ChartCustomAccessibilityAction
import com.mena97villalobos.composeplotlib.chart.accessibilitySummaryText
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildBarMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildCombinedMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildLineMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildPieMarkerData
import com.mena97villalobos.composeplotlib.chart.internal.marker.buildScatterMarkerData
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ChartAccessibilitySpec
import com.mena97villalobos.composeplotlib.core.spec.ChartLiveRegionMode
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import kotlin.math.roundToInt

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for line charts. */
internal fun Modifier.lineChartAccessibility(
    spec: LineChartSpec,
    selectionState: ChartSelectionState,
    emptyStateDescription: String? = null,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.keyboardSelectionRoamEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            val summary =
                spec.accessibility.contentDescription
                    ?: defaultLineChartSemanticSummary(spec)
            val noData = spec.series.all { it.points.isEmpty() }
            contentDescription =
                if (noData) {
                    emptyStateDescription ?: "$summary $ACCESSIBILITY_NO_DATA_SUFFIX"
                } else {
                    summary
                }
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> {
                        liveRegion = LiveRegionMode.Polite
                    }
                    ChartLiveRegionMode.Assertive -> {
                        liveRegion = LiveRegionMode.Assertive
                    }
                }
                val selection = selectionState.selection
                if (selection != null) {
                    val data = buildLineMarkerData(spec, selection)
                    if (data != null) {
                        stateDescription = data.accessibilitySummaryText()
                    }
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for pie / donut charts. */
internal fun Modifier.pieChartAccessibility(
    spec: PieChartSpec,
    selectionState: ChartSelectionState,
    emptyStateDescription: String? = null,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.keyboardSelectionRoamEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            val summary =
                spec.accessibility.contentDescription
                    ?: defaultPieChartSemanticSummary(spec)
            val noData = !spec.slices.any { it.value > 0.0 }
            contentDescription =
                if (noData) {
                    emptyStateDescription ?: "$summary $ACCESSIBILITY_NO_DATA_SUFFIX"
                } else {
                    summary
                }
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> {
                        liveRegion = LiveRegionMode.Polite
                    }
                    ChartLiveRegionMode.Assertive -> {
                        liveRegion = LiveRegionMode.Assertive
                    }
                }
                val selection = selectionState.selection
                if (selection != null) {
                    val data = buildPieMarkerData(spec, selection)
                    if (data != null) {
                        stateDescription = data.accessibilitySummaryText()
                    }
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for sankey charts. */
internal fun Modifier.sankeyChartAccessibility(
    spec: SankeyChartSpec,
    selectionState: ChartSelectionState,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            contentDescription = spec.accessibility.contentDescription ?: "Sankey chart."
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> liveRegion = LiveRegionMode.Polite
                    ChartLiveRegionMode.Assertive -> liveRegion = LiveRegionMode.Assertive
                }
                val sel = selectionState.selection
                if (sel != null) {
                    // Requires the composable to provide a plan; if marker data can't be built, fall back to default.
                    // (SankeyChart currently builds the marker payload itself.)
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for scatter charts. */
internal fun Modifier.scatterChartAccessibility(
    spec: ScatterChartSpec,
    selectionState: ChartSelectionState,
    emptyStateDescription: String? = null,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.keyboardSelectionRoamEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            val summary =
                spec.accessibility.contentDescription
                    ?: defaultScatterChartSemanticSummary(spec)
            val noData = spec.series.all { it.points.isEmpty() }
            contentDescription =
                if (noData) {
                    emptyStateDescription ?: "$summary $ACCESSIBILITY_NO_DATA_SUFFIX"
                } else {
                    summary
                }
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> {
                        liveRegion = LiveRegionMode.Polite
                    }
                    ChartLiveRegionMode.Assertive -> {
                        liveRegion = LiveRegionMode.Assertive
                    }
                }
                val selection = selectionState.selection
                if (selection != null) {
                    val data = buildScatterMarkerData(spec, selection)
                    if (data != null) {
                        stateDescription = data.accessibilitySummaryText()
                    }
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for combined cartesian charts. */
internal fun Modifier.combinedCartesianChartAccessibility(
    spec: CombinedCartesianChartSpec,
    selectionState: ChartSelectionState,
    emptyStateDescription: String? = null,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.keyboardSelectionRoamEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            val summary =
                spec.accessibility.contentDescription
                    ?: defaultCombinedCartesianSemanticSummary(spec)
            val noData =
                spec.layers.all { layer ->
                    when (layer) {
                        is CombinedCartesianLayer.Line -> layer.series.all { it.points.isEmpty() }
                        is CombinedCartesianLayer.Bar -> layer.groups.all { it.entries.isEmpty() }
                        is CombinedCartesianLayer.Scatter -> layer.series.all { it.points.isEmpty() }
                    }
                }
            contentDescription =
                if (noData) {
                    emptyStateDescription ?: "$summary $ACCESSIBILITY_NO_DATA_SUFFIX"
                } else {
                    summary
                }
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> {
                        liveRegion = LiveRegionMode.Polite
                    }
                    ChartLiveRegionMode.Assertive -> {
                        liveRegion = LiveRegionMode.Assertive
                    }
                }
                val selection = selectionState.selection
                if (selection != null) {
                    val data = buildCombinedMarkerData(spec, selection)
                    if (data != null) {
                        stateDescription = data.accessibilitySummaryText()
                    }
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

/** Adds TalkBack / VoiceOver content, optional custom actions, focus, and an Image role for bar charts. */
internal fun Modifier.barChartAccessibility(
    spec: BarChartSpec,
    selectionState: ChartSelectionState,
    emptyStateDescription: String? = null,
    customAccessibilityActions: List<ChartCustomAccessibilityAction> = emptyList(),
): Modifier {
    val wantsFocus =
        spec.accessibility.requestFocusForAccessibility ||
            spec.accessibility.keyboardViewportNudgeEnabled ||
            spec.accessibility.keyboardSelectionRoamEnabled ||
            spec.accessibility.requestFocusOnMarker
    val mergeDescendants = !spec.accessibility.requestFocusOnMarker
    val semanticsModifier =
        Modifier.semantics(mergeDescendants = mergeDescendants) {
            role = Role.Image
            val summary =
                spec.accessibility.contentDescription
                    ?: defaultBarChartSemanticSummary(spec)
            val noData = spec.groups.all { it.entries.isEmpty() }
            contentDescription =
                if (noData) {
                    emptyStateDescription ?: "$summary $ACCESSIBILITY_NO_DATA_SUFFIX"
                } else {
                    summary
                }
            if (spec.accessibility.exposeSelectionState) {
                when (spec.accessibility.selectionLiveRegionMode) {
                    ChartLiveRegionMode.Off -> Unit
                    ChartLiveRegionMode.Polite -> {
                        liveRegion = LiveRegionMode.Polite
                    }
                    ChartLiveRegionMode.Assertive -> {
                        liveRegion = LiveRegionMode.Assertive
                    }
                }
                val selection = selectionState.selection
                if (selection != null) {
                    val data = buildBarMarkerData(spec, selection)
                    if (data != null) {
                        stateDescription = data.accessibilitySummaryText()
                    }
                }
            }
            if (customAccessibilityActions.isNotEmpty()) {
                customActions =
                    customAccessibilityActions.map { a ->
                        CustomAccessibilityAction(a.label) {
                            a.onClick()
                            true
                        }
                    }
            }
        }
    return this
        .focusable(enabled = wantsFocus)
        .then(semanticsModifier)
}

private const val ACCESSIBILITY_NO_DATA_SUFFIX = "No data to display."

private fun defaultLineChartSemanticSummary(spec: LineChartSpec): String =
    when {
        spec.series.isEmpty() -> "Line chart with no series."
        else -> {
            val names = spec.series.joinToString { it.label }
            val keys = keyboardHint(spec.accessibility)
            val markerFocus = markerFocusHint(spec.accessibility.requestFocusOnMarker)
            "Line chart. Series: $names. Use pinch to zoom and drag to pan. Tap a point to select.$keys$markerFocus"
        }
    }

private fun defaultPieChartSemanticSummary(spec: PieChartSpec): String =
    when {
        spec.slices.isEmpty() -> "Pie chart with no slices."
        else -> {
            val names = spec.slices.joinToString { it.label }
            val positive = spec.slices.filter { it.value > 0.0 }
            val total = positive.sumOf { it.value }
            if (spec.sliceLabels.enabled && total > 0.0) {
                val breakdown =
                    positive.joinToString { s ->
                        val pct = (100.0 * s.value / total).roundToInt()
                        "${s.label} $pct percent"
                    }
                val mf = markerFocusHint(spec.accessibility.requestFocusOnMarker)
                val rk = selectionRoamHint(spec.accessibility)
                "Pie chart. $breakdown. Tap a slice to select.$mf$rk"
            } else {
                val mf = markerFocusHint(spec.accessibility.requestFocusOnMarker)
                val rk = selectionRoamHint(spec.accessibility)
                "Pie chart. Slices: $names. Tap a slice to select.$mf$rk"
            }
        }
    }

private fun defaultScatterChartSemanticSummary(spec: ScatterChartSpec): String =
    when {
        spec.series.isEmpty() -> "Scatter chart with no series."
        else -> {
            val names = spec.series.joinToString { it.label }
            val keys = keyboardHint(spec.accessibility)
            val mf = markerFocusHint(spec.accessibility.requestFocusOnMarker)
            "Scatter chart. Series: $names. Use pinch to zoom and drag to pan. Tap a point to select.$keys$mf"
        }
    }

private fun defaultCombinedCartesianSemanticSummary(spec: CombinedCartesianChartSpec): String =
    when {
        spec.layers.isEmpty() -> "Combined chart with no layers."
        else -> {
            val desc =
                spec.layers.joinToString { layer ->
                    when (layer) {
                        is CombinedCartesianLayer.Line ->
                            "line layer ${layer.id}: ${layer.series.joinToString { it.label }}"
                        is CombinedCartesianLayer.Bar ->
                            "bar layer ${layer.id}: ${layer.groups.joinToString { it.label }}"
                        is CombinedCartesianLayer.Scatter ->
                            "scatter layer ${layer.id}: ${layer.series.joinToString { it.label }}"
                    }
                }
            val keys = keyboardHint(spec.accessibility)
            val mf = markerFocusHint(spec.accessibility.requestFocusOnMarker)
            "Combined cartesian chart. $desc. Use pinch to zoom and drag to pan. Tap to select.$keys$mf"
        }
    }

private fun defaultBarChartSemanticSummary(spec: BarChartSpec): String =
    when {
        spec.groups.isEmpty() -> "Bar chart with no groups."
        else -> {
            val names = spec.groups.joinToString { it.label }
            val keys = keyboardHint(spec.accessibility)
            val mf = markerFocusHint(spec.accessibility.requestFocusOnMarker)
            val orient =
                when (spec.orientation) {
                    BarChartOrientation.Vertical -> "vertical"
                    BarChartOrientation.Horizontal -> "horizontal"
                }
            "Bar chart ($orient). Groups: $names. Use pinch to zoom and drag to pan. Tap a bar to select.$keys$mf"
        }
    }

private fun markerFocusHint(requestFocusOnMarker: Boolean): String =
    if (requestFocusOnMarker) {
        " Selected value marker is a separate focus target."
    } else {
        ""
    }

private fun keyboardHint(accessibility: ChartAccessibilitySpec): String {
    val viewport =
        if (accessibility.keyboardViewportNudgeEnabled) {
            " When focused, arrow keys pan; plus and minus zoom; Escape clears selection."
        } else {
            ""
        }
    return viewport + selectionRoamHint(accessibility)
}

private fun selectionRoamHint(accessibility: ChartAccessibilitySpec): String =
    if (accessibility.keyboardSelectionRoamEnabled) {
        if (accessibility.keyboardViewportNudgeEnabled) {
            " Shift+arrow keys move selection (Shift+Up/Down switch series on line or scatter); unmodified arrows pan."
        } else {
            " Shift+arrow keys move selection when focused."
        }
    } else {
        ""
    }
