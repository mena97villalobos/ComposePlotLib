package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/**
 * A **drill-down** back-stack over levels of type [T]: start at a root, [drillDown] into a mark the user selects,
 * and [drillUp] to go back. Build each level's chart spec from [current]; show a back affordance while [canDrillUp].
 *
 * The library provides the navigation state; you decide what a level is (a category id, a filtered dataset, …) and
 * wire selection → [drillDown] via the chart's `selectionState` / `onLegendItemClick`. Pairs with programmatic
 * viewport transitions (`ChartViewportState.zoomTo`) and `ChartAnimationPolicy` for animated drills.
 */
@Stable
class ChartDrillState<T> internal constructor(
    root: T,
) {
    private val stack = mutableStateListOf(root)

    /** The full path from the root to [current] (inclusive). */
    val path: List<T> get() = stack.toList()

    /** The level currently in view (the top of the stack). */
    val current: T get() = stack.last()

    /** How many levels deep the current view is (0 at the root). */
    val depth: Int get() = stack.size - 1

    /** True when there is a level to go back to. */
    val canDrillUp: Boolean get() = stack.size > 1

    /** Pushes [level] as the new current view. */
    fun drillDown(level: T) {
        stack.add(level)
    }

    /** Pops back to the previous level, if any. */
    fun drillUp() {
        if (stack.size > 1) stack.removeAt(stack.size - 1)
    }

    /** Returns to the root, discarding the drill path. */
    fun reset() {
        while (stack.size > 1) stack.removeAt(stack.size - 1)
    }
}

/** Remembers a [ChartDrillState] starting at [root]. */
@Composable
fun <T> rememberChartDrillState(root: T): ChartDrillState<T> = remember { ChartDrillState(root) }
