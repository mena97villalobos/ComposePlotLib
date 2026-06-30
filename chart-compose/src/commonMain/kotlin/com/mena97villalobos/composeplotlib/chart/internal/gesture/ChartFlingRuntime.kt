package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.ui.geometry.Offset
import com.mena97villalobos.composeplotlib.chart.ChartFlingGesturePolicy
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.math.hypot

internal class ChartFlingRuntime(
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun cancel() {
        job?.cancel()
        job = null
    }

    fun startFling(
        velocityPxPerSec: Offset,
        policy: ChartFlingGesturePolicy,
        flags: EffectiveTransformFlags,
        viewportState: ChartViewportState,
        layoutPlan: ChartLayoutPlan,
    ) {
        cancel()
        job =
            scope.launch {
                runFlingLoop(velocityPxPerSec, policy, flags, viewportState, layoutPlan)
            }
    }

    private suspend fun runFlingLoop(
        initialVelocity: Offset,
        policy: ChartFlingGesturePolicy,
        flags: EffectiveTransformFlags,
        viewportState: ChartViewportState,
        layoutPlan: ChartLayoutPlan,
    ) {
        var vx = initialVelocity.x
        var vy = initialVelocity.y
        val minV = 28f
        val plotBounds = layoutPlan.plotBounds
        val plotW = plotBounds.width.coerceAtLeast(1.0)
        val plotH = plotBounds.height.coerceAtLeast(1.0)
        val xSpan = viewportState.viewport.xRange.span
        val ySpan = viewportState.viewport.yRange.span

        while (coroutineContext.isActive && hypot(vx.toDouble(), vy.toDouble()) >= minV) {
            val dt = 1f / 60f
            val rawDx =
                if (flags.panX) {
                    -(vx * dt / plotW.toFloat()) * xSpan
                } else {
                    0.0
                }
            val rawDy =
                if (flags.panY) {
                    (vy * dt / plotH.toFloat()) * ySpan
                } else {
                    0.0
                }
            val capX = xSpan * policy.maxPanPerFrameFraction
            val capY = ySpan * policy.maxPanPerFrameFraction
            val dx = rawDx.coerceIn(-capX, capX)
            val dy = rawDy.coerceIn(-capY, capY)
            if (dx != 0.0 || dy != 0.0) {
                viewportState.pan(dx, dy)
            }
            vx *= policy.frictionMultiplier
            vy *= policy.frictionMultiplier
            delay(16L)
        }
    }
}
