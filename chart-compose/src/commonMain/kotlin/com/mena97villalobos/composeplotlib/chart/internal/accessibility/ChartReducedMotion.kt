package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy

/**
 * Applies system reduced-motion when [ChartAnimationPolicy.respectSystemReducedMotion] is true.
 * Call from chart composables before running viewport/selection animations.
 */
@Composable
internal fun resolveChartAnimationPolicy(policy: ChartAnimationPolicy): ChartAnimationPolicy {
    if (!policy.respectSystemReducedMotion) return policy
    if (!chartReducedMotionPreferred()) return policy
    return policy.copy(
        viewportChangesEnabled = false,
        selectionHighlightEnabled = false,
        viewportDurationMillis = 0,
        selectionDurationMillis = 0,
        gestureViewportEasingEnabled = false,
        dataTransitionEnabled = false,
        dataTransitionDurationMillis = 0,
    )
}
