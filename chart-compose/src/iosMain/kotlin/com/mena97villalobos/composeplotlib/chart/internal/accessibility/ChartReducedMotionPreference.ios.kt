package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun chartReducedMotionPreferred(): Boolean = UIAccessibilityIsReduceMotionEnabled()
