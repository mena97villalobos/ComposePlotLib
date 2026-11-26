package com.mena97villalobos.composeplotlib.chart.internal.accessibility

import androidx.compose.runtime.Composable

/**
 * Maps the browser's `prefers-reduced-motion: reduce` media query to reduced motion for charts. Read each
 * composition, like the other platforms, so a change is picked up on recomposition.
 */
@Composable
internal actual fun chartReducedMotionPreferred(): Boolean = prefersReducedMotion()

@OptIn(ExperimentalWasmJsInterop::class)
private fun prefersReducedMotion(): Boolean = js("typeof window.matchMedia === 'function' && window.matchMedia('(prefers-reduced-motion: reduce)').matches")
