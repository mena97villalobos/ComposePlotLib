package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable

/**
 * System “reduce motion” / “remove animations” preference. Android uses Compose
 * `androidx.compose.foundation.LocalReducedMotion`; iOS uses
 * `UIAccessibilityIsReduceMotionEnabled`.
 */
@Composable
expect fun chartReducedMotionPreferred(): Boolean
