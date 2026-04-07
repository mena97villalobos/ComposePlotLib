package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

@Composable
internal fun rememberChartMarkerFocusRequester(): FocusRequester = remember { FocusRequester() }

/**
 * Moves focus to the marker container after selection changes when [enabled] is true.
 */
@Composable
internal fun RequestFocusWhenChartMarkerShown(
    enabled: Boolean,
    selection: ChartSelection?,
    focusRequester: FocusRequester,
) {
    LaunchedEffect(enabled, selection) {
        if (enabled && selection != null) {
            focusRequester.requestFocus()
        }
    }
}

internal fun Modifier.chartMarkerFocusContainer(
    enabled: Boolean,
    focusRequester: FocusRequester,
    contentDescription: String,
): Modifier =
    if (!enabled) {
        this
    } else {
        this.focusRequester(focusRequester)
            .focusable()
            .semantics(mergeDescendants = false) {
                this.contentDescription = contentDescription
            }
    }
