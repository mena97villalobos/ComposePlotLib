package com.mena97villalobos.composeplotlib.chart.internal.animation

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mena97villalobos.composeplotlib.chart.ChartMarkerData

private const val SelectionHighlightAlphaEpsilon = 1e-4f

internal data class SelectionMarkerAppearance(
    val alpha: Float,
    val contentData: ChartMarkerData?,
)

/**
 * Marker appearance: [alpha] tracks selection (fade in/out), and [contentData] keeps the last marker payload
 * during fade-out when [enabled] and [durationMillis] > 0 so the overlay stays composed until alpha reaches zero.
 */
@Composable
internal fun rememberSelectionMarkerAppearance(
    markerData: ChartMarkerData?,
    selectionPresent: Boolean,
    enabled: Boolean,
    durationMillis: Int,
): SelectionMarkerAppearance {
    val alpha =
        rememberSelectionHighlightAlpha(
            selectionPresent = selectionPresent,
            enabled = enabled,
            durationMillis = durationMillis,
        )
    val fadeOutActive = enabled && durationMillis > 0
    var retainedMarker by remember { mutableStateOf<ChartMarkerData?>(null) }

    LaunchedEffect(markerData) {
        if (markerData != null) {
            retainedMarker = markerData
        }
    }
    LaunchedEffect(alpha, markerData, fadeOutActive) {
        if (fadeOutActive && markerData == null && alpha <= SelectionHighlightAlphaEpsilon) {
            retainedMarker = null
        }
    }

    val contentData =
        when {
            !fadeOutActive -> markerData
            markerData != null -> markerData
            alpha > SelectionHighlightAlphaEpsilon -> retainedMarker
            else -> null
        }

    return SelectionMarkerAppearance(alpha = alpha, contentData = contentData)
}

/** Alpha for marker / selection overlay; fades in and out when [enabled] and duration is positive. */
@Composable
internal fun rememberSelectionHighlightAlpha(
    selectionPresent: Boolean,
    enabled: Boolean,
    durationMillis: Int,
): Float {
    val target = if (selectionPresent) 1f else 0f
    val spec: AnimationSpec<Float> =
        remember(enabled, durationMillis) {
            if (!enabled || durationMillis <= 0) {
                tween(durationMillis = 0)
            } else {
                tween(durationMillis = durationMillis.coerceIn(0, 60_000))
            }
        }
    val alpha by animateFloatAsState(
        targetValue = target,
        animationSpec = spec,
        label = "chartSelectionHighlight",
    )
    return alpha
}
