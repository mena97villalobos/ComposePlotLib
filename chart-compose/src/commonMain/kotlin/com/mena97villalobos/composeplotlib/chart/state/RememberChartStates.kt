package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.ViewportAxisPolicy
import com.mena97villalobos.composeplotlib.core.viewport.ViewportConstraints
import com.mena97villalobos.composeplotlib.core.viewport.ViewportState
import com.mena97villalobos.composeplotlib.core.viewport.normalizedNonDegenerate

/**
 * Remembers [ChartViewportState].
 *
 * @param initialViewport starting visible window (often equals [domain] or a zoomed subset).
 * @param domain maximum range pan/zoom is clamped to (full data extent).
 * @param constraints optional min/max axis spans for pinch-zoom.
 * @param axisPolicy optional per-axis locks pinned to [domain] after each gesture.
 */
@Composable
fun rememberChartViewportState(
    initialViewport: ChartViewport,
    domain: ChartViewport = initialViewport,
    constraints: ViewportConstraints = ViewportConstraints(),
    axisPolicy: ViewportAxisPolicy = ViewportAxisPolicy(),
): ChartViewportState =
    remember(initialViewport, domain, constraints, axisPolicy) {
        val dom = domain.normalizedNonDegenerate()
        val init = initialViewport.normalizedNonDegenerate()
        val start = ViewportState(viewport = init, constraints = constraints).withViewport(init, dom)
        ChartViewportState(
            initialViewportState = start,
            domain = dom,
            initialAxisPolicy = axisPolicy,
        )
    }

/** Remembers empty [ChartSelectionState] across composition. */
@Composable
fun rememberChartSelectionState(): ChartSelectionState =
    remember {
        ChartSelectionState()
    }
