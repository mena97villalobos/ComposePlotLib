package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.ViewportAxisPolicy
import com.mena97villalobos.composeplotlib.core.viewport.ViewportConstraints
import com.mena97villalobos.composeplotlib.core.viewport.ViewportState
import com.mena97villalobos.composeplotlib.core.viewport.withAxisPolicy
import kotlin.math.abs

/**
 * Compose-friendly holder for [ViewportState] with a fixed [domain] used for clamping pan/zoom.
 *
 * [axisPolicy] can pin an axis to the full domain after each gesture (e.g. horizontal-only exploration).
 * Update it with [updateAxisPolicy]; zoom limits with [updateConstraints].
 *
 * Obtain via [rememberChartViewportState]. You may pass a wider [domain] than `initialViewport` to allow
 * zooming out to a known data extent while starting zoomed in.
 */
@Stable
class ChartViewportState(
    initialViewportState: ViewportState,
    val domain: ChartViewport,
    initialAxisPolicy: ViewportAxisPolicy = ViewportAxisPolicy(),
) {
    var viewportState by mutableStateOf(initialViewportState)
        private set

    var axisPolicy by mutableStateOf(initialAxisPolicy)
        private set

    /**
     * Last kind of viewport mutation; used by chart composables to decide whether to smooth the
     * rendered viewport toward [viewport].
     */
    var lastViewportChangeOrigin by mutableStateOf(ViewportChangeOrigin.Programmatic)
        private set

    val viewport: ChartViewport get() = viewportState.viewport

    val constraints: ViewportConstraints get() = viewportState.constraints

    /** Re-clamps after changing zoom span limits. */
    fun updateConstraints(constraints: ViewportConstraints) {
        lastViewportChangeOrigin = ViewportChangeOrigin.Programmatic
        viewportState = viewportState.copy(constraints = constraints).finish(domain, axisPolicy)
    }

    /** Changes axis locking and re-clamps. */
    fun updateAxisPolicy(policy: ViewportAxisPolicy) {
        lastViewportChangeOrigin = ViewportChangeOrigin.Programmatic
        axisPolicy = policy
        viewportState = viewportState.finish(domain, axisPolicy)
    }

    fun reset() {
        lastViewportChangeOrigin = ViewportChangeOrigin.Programmatic
        viewportState =
            ViewportState(viewport = domain, constraints = viewportState.constraints)
                .finish(domain, axisPolicy)
    }

    /** Pans in domain units (x right positive, y up positive per core viewport conventions). */
    fun pan(
        deltaX: Double,
        deltaY: Double,
        origin: ViewportChangeOrigin = ViewportChangeOrigin.Gesture,
    ) {
        lastViewportChangeOrigin = origin
        viewportState = viewportState.pan(deltaX, deltaY, domain).finish(domain, axisPolicy)
    }

    /** Zooms around domain pivot `(pivotX, pivotY)`; scale factors greater than `1` zoom in (smaller visible span). */
    fun zoom(
        scaleX: Double,
        scaleY: Double,
        pivotX: Double,
        pivotY: Double,
        origin: ViewportChangeOrigin = ViewportChangeOrigin.Gesture,
    ) {
        lastViewportChangeOrigin = origin
        viewportState = viewportState.zoom(scaleX, scaleY, pivotX, pivotY, domain).finish(domain, axisPolicy)
    }

    /** Recenters the current x/y spans on `(centerX, centerY)` without changing span. */
    fun panTo(
        centerX: Double,
        centerY: Double,
    ) {
        lastViewportChangeOrigin = ViewportChangeOrigin.Programmatic
        val nextViewport =
            ChartViewport(
                xRange = viewport.xRange.centeredOn(sanitizeCenter(centerX, viewport.xRange.midpoint)),
                yRange = viewport.yRange.centeredOn(sanitizeCenter(centerY, viewport.yRange.midpoint)),
            )
        viewportState = viewportState.withViewport(nextViewport, domain).finish(domain, axisPolicy)
    }

    /** Alias for [panTo]. */
    fun centerOn(
        x: Double,
        y: Double,
    ) {
        panTo(centerX = x, centerY = y)
    }

    /**
     * Sets exact axis spans centered on `(centerX, centerY)`, clamped within [domain].
     *
     * Non-finite or non-positive spans fall back to the current span, and non-finite centers fall back to the
     * current midpoint, so bad inputs (e.g. a `NaN` from `dataMax - dataMin` on empty data) cannot crash.
     */
    fun zoomTo(
        xSpan: Double,
        ySpan: Double,
        centerX: Double,
        centerY: Double,
    ) {
        lastViewportChangeOrigin = ViewportChangeOrigin.Programmatic
        val safeXSpan = sanitizeSpan(xSpan, viewport.xRange.span)
        val safeYSpan = sanitizeSpan(ySpan, viewport.yRange.span)
        val safeCenterX = sanitizeCenter(centerX, viewport.xRange.midpoint)
        val safeCenterY = sanitizeCenter(centerY, viewport.yRange.midpoint)
        val nextViewport =
            ChartViewport(
                xRange = ClosedRangeD(safeCenterX - (safeXSpan / 2.0), safeCenterX + (safeXSpan / 2.0)),
                yRange = ClosedRangeD(safeCenterY - (safeYSpan / 2.0), safeCenterY + (safeYSpan / 2.0)),
            )
        viewportState = viewportState.withViewport(nextViewport, domain).finish(domain, axisPolicy)
    }

    /** Fits the visible window to [domain]. */
    fun fitToData() {
        reset()
    }
}

private fun ViewportState.finish(
    domain: ChartViewport,
    policy: ViewportAxisPolicy,
): ViewportState = withViewport(viewport.withAxisPolicy(domain, policy), domain)

private fun ClosedRangeD.centeredOn(center: Double): ClosedRangeD = ClosedRangeD(center - (span / 2.0), center + (span / 2.0))

/** Returns the magnitude of [span] when finite and positive, otherwise [fallback] (the current span). */
private fun sanitizeSpan(
    span: Double,
    fallback: Double,
): Double {
    val magnitude = abs(span)
    return if (magnitude.isFinite() && magnitude > 0.0) magnitude else fallback
}

/** Returns [center] when finite, otherwise [fallback] (the current midpoint). */
private fun sanitizeCenter(
    center: Double,
    fallback: Double,
): Double = if (center.isFinite()) center else fallback
