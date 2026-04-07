package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun chartReducedMotionPreferred(): Boolean = UIAccessibilityIsReduceMotionEnabled()
