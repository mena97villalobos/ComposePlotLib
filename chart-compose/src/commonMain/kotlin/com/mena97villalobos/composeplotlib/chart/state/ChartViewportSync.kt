package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.abs
import kotlin.math.max

/** Which axis window [SyncChartViewports] mirrors across a linked group of charts. */
enum class ChartSyncAxis {
    /** Mirror the x window only — the common case for stacked, shared-time-axis dashboards. */
    X,

    /** Mirror the y window only — for charts whose value axes are comparable. */
    Y,

    /** Mirror both axes — for detail views over the same domain. */
    Both,
}

/**
 * Links a group of [ChartViewportState]s so that panning, zooming, or otherwise moving any one of them
 * mirrors the same [axis] window onto the others — the `syncId` / "connect" pattern used to keep a
 * dashboard of stacked charts aligned on a shared axis.
 *
 * Each follower re-centers on the leader's span and midpoint (via [ChartViewportState.zoomTo]), so the
 * shared axis stays aligned even when the charts have different data extents — every follower still
 * clamps to its own [ChartViewportState.domain]. Use [ChartSyncAxis.X] (default) for shared-time-axis
 * stacks; [ChartSyncAxis.Y] / [ChartSyncAxis.Both] when the value axes are comparable.
 *
 * Feedback loops are avoided by reacting only to the member that actually moved and then re-reading the
 * followers' post-clamp windows, so a follower that clamped to a narrower domain never fights the leader.
 *
 * Call inside a composable with two or more remembered states:
 * ```
 * val price = rememberChartViewportState(...)
 * val volume = rememberChartViewportState(...)
 * SyncChartViewports(price, volume)            // shared x window
 * LineChart(priceSpec, viewportState = price)
 * BarChart(volumeSpec, viewportState = volume)
 * ```
 *
 * To link **selection / tooltips** as well, pass the *same* [ChartSelectionState] instance to the charts
 * you want to highlight together.
 *
 * @param states the viewport states to keep aligned; fewer than two states is a no-op.
 * @param axis which axis window to mirror; defaults to [ChartSyncAxis.X].
 */
@Composable
fun SyncChartViewports(
    vararg states: ChartViewportState,
    axis: ChartSyncAxis = ChartSyncAxis.X,
) {
    val group = states.toList()
    LaunchedEffect(group, axis) {
        if (group.size < 2) return@LaunchedEffect
        var previous = group.map { it.syncRanges(axis) }
        snapshotFlow { group.map { it.syncRanges(axis) } }
            .collect { current ->
                previous = syncChartViewportsStep(group, previous, current, axis)
            }
    }
}

/**
 * Runs one coordination step: finds the member whose [axis] window changed since [previous], mirrors it
 * onto the others, and returns the group's post-clamp windows (the new baseline).
 *
 * Extracted from [SyncChartViewports] so the propagation logic is unit-testable without a composition.
 */
internal fun syncChartViewportsStep(
    states: List<ChartViewportState>,
    previous: List<List<ClosedRangeD>>,
    current: List<List<ClosedRangeD>>,
    axis: ChartSyncAxis,
): List<List<ClosedRangeD>> {
    val leader =
        current.indices.firstOrNull { index ->
            !current[index].approxEquals(previous.getOrElse(index) { emptyList() })
        } ?: return current
    val source = states[leader].viewport
    states.forEachIndexed { index, state ->
        if (index != leader) state.applySyncedAxis(source, axis)
    }
    return states.map { it.syncRanges(axis) }
}

/** The axis range(s) [SyncChartViewports] compares and mirrors for [axis]. */
private fun ChartViewportState.syncRanges(axis: ChartSyncAxis): List<ClosedRangeD> = viewport.syncRanges(axis)

private fun ChartViewport.syncRanges(axis: ChartSyncAxis): List<ClosedRangeD> =
    when (axis) {
        ChartSyncAxis.X -> listOf(xRange)
        ChartSyncAxis.Y -> listOf(yRange)
        ChartSyncAxis.Both -> listOf(xRange, yRange)
    }

/** Re-centers this state's [axis] window on [source]'s span and midpoint, keeping the untouched axis. */
private fun ChartViewportState.applySyncedAxis(
    source: ChartViewport,
    axis: ChartSyncAxis,
) {
    val target = viewport
    when (axis) {
        ChartSyncAxis.X ->
            zoomTo(
                xSpan = source.xRange.span,
                ySpan = target.yRange.span,
                centerX = source.xRange.midpoint,
                centerY = target.yRange.midpoint,
            )
        ChartSyncAxis.Y ->
            zoomTo(
                xSpan = target.xRange.span,
                ySpan = source.yRange.span,
                centerX = target.xRange.midpoint,
                centerY = source.yRange.midpoint,
            )
        ChartSyncAxis.Both ->
            zoomTo(
                xSpan = source.xRange.span,
                ySpan = source.yRange.span,
                centerX = source.xRange.midpoint,
                centerY = source.yRange.midpoint,
            )
    }
}

private fun List<ClosedRangeD>.approxEquals(other: List<ClosedRangeD>): Boolean {
    if (size != other.size) return false
    for (i in indices) if (!this[i].approxEquals(other[i])) return false
    return true
}

private fun ClosedRangeD.approxEquals(other: ClosedRangeD): Boolean = closeEnough(start, other.start) && closeEnough(endInclusive, other.endInclusive)

/** Relative/absolute tolerance so post-clamp rounding does not read as a fresh user gesture. */
private fun closeEnough(
    a: Double,
    b: Double,
): Boolean {
    val diff = abs(a - b)
    val scale = max(1.0, max(abs(a), abs(b)))
    return diff <= 1e-9 * scale
}
