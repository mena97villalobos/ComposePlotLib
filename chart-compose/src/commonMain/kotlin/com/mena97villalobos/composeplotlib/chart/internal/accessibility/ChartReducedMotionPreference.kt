package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable

/**
 * System “reduce motion” / “remove animations” preference. Android uses Compose
 * [androidx.compose.foundation.LocalReducedMotion]; iOS uses
 * [platform.UIKit.UIAccessibility.isReduceMotionEnabled].
 */
@Composable
expect fun chartReducedMotionPreferred(): Boolean
