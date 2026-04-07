package com.mena97villalobos.composeplotlib.chart.internal.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.chart.state.ViewportChangeOrigin
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Viewport actually shown in layout/render/hit-testing: interpolates toward [ChartViewportState.viewport] for
 * programmatic updates when [ChartAnimationPolicy.viewportChangesEnabled] and duration are non-zero; optional
 * exponential chase when the last change is gesture-driven and [ChartAnimationPolicy.gestureViewportEasingEnabled].
 */
@Composable
internal fun rememberAnimatedChartViewport(
    viewportState: ChartViewportState,
    animation: ChartAnimationPolicy,
): ChartViewport {
    val target = viewportState.viewport
    val origin = viewportState.lastViewportChangeOrigin
    val latestTarget = rememberUpdatedState(viewportState.viewport)
    val latestOrigin = rememberUpdatedState(viewportState.lastViewportChangeOrigin)

    var display by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    var warmedUp by remember { mutableStateOf(false) }

    LaunchedEffect(
        target,
        origin,
        animation.viewportChangesEnabled,
        animation.viewportDurationMillis,
        animation.gestureViewportEasingEnabled,
    ) {
        if (!warmedUp) {
            progress.snapTo(1f)
            display = target
            warmedUp = true
            return@LaunchedEffect
        }

        if (origin == ViewportChangeOrigin.Gesture) {
            if (!animation.gestureViewportEasingEnabled) {
                display = target
            }
            return@LaunchedEffect
        }

        val snap =
            !animation.viewportChangesEnabled ||
                animation.viewportDurationMillis <= 0

        if (snap) {
            progress.snapTo(1f)
            display = target
            return@LaunchedEffect
        }

        val from = display
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animation.viewportDurationMillis.coerceIn(0, 60_000)),
        ) {
            display = lerpViewport(from, target, value)
        }
    }

    LaunchedEffect(animation.gestureViewportEasingEnabled, animation.gestureViewportChaseStiffness) {
        if (!animation.gestureViewportEasingEnabled) return@LaunchedEffect
        val stiffness = animation.gestureViewportChaseStiffness.coerceIn(0.05f, 0.95f)
        while (isActive) {
            delay(16L)
            if (latestOrigin.value != ViewportChangeOrigin.Gesture) continue
            val t = latestTarget.value
            display = lerpViewport(display, t, stiffness)
        }
    }

    return display
}

private fun lerpViewport(
    from: ChartViewport,
    to: ChartViewport,
    t: Float,
): ChartViewport =
    ChartViewport(
        xRange =
            ClosedRangeD(
                lerp(from.xRange.start, to.xRange.start, t),
                lerp(from.xRange.endInclusive, to.xRange.endInclusive, t),
            ),
        yRange =
            ClosedRangeD(
                lerp(from.yRange.start, to.yRange.start, t),
                lerp(from.yRange.endInclusive, to.yRange.endInclusive, t),
            ),
    )

private fun lerp(
    a: Double,
    b: Double,
    t: Float,
): Double = a + (b - a) * t.toDouble()
