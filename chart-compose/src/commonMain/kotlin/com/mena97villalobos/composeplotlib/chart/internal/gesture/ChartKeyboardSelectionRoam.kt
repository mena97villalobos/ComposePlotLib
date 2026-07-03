package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.mena97villalobos.composeplotlib.chart.state.ChartSelectionState
import com.mena97villalobos.composeplotlib.core.layout.BarChartPlan
import com.mena97villalobos.composeplotlib.core.layout.BoxPlotChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CandlestickChartPlan
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedCartesianChartPlan
import com.mena97villalobos.composeplotlib.core.layout.WaterfallChartPlan
import com.mena97villalobos.composeplotlib.core.selection.KeyboardSelectionRoam
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

internal fun Modifier.chartLineKeyboardSelectionRoam(
    enabled: Boolean,
    spec: LineChartSpec,
    renderViewport: ChartViewport,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    layoutPlan: () -> ChartLayoutPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val layout = layoutPlan() ?: return@onKeyEvent false
        val (deltaPoint, deltaSeries) = shiftArrowDeltas(event.key) ?: return@onKeyEvent false
        val next =
            KeyboardSelectionRoam.lineChartRoam(
                spec = spec,
                viewport = renderViewport,
                plotBounds = layout.plotBounds,
                current = selectionState.selection,
                deltaPoint = deltaPoint,
                deltaSeries = deltaSeries,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartScatterKeyboardSelectionRoam(
    enabled: Boolean,
    spec: ScatterChartSpec,
    renderViewport: ChartViewport,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    layoutPlan: () -> ChartLayoutPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val layout = layoutPlan() ?: return@onKeyEvent false
        val (deltaPoint, deltaSeries) = shiftArrowDeltas(event.key) ?: return@onKeyEvent false
        val next =
            KeyboardSelectionRoam.scatterChartRoam(
                spec = spec,
                viewport = renderViewport,
                plotBounds = layout.plotBounds,
                current = selectionState.selection,
                deltaPoint = deltaPoint,
                deltaSeries = deltaSeries,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartBarKeyboardSelectionRoam(
    enabled: Boolean,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    barPlan: () -> BarChartPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val plan = barPlan() ?: return@onKeyEvent false
        val delta =
            when (plan.orientation) {
                BarChartOrientation.Vertical ->
                    when (event.key) {
                        Key.DirectionLeft -> -1
                        Key.DirectionRight -> 1
                        else -> return@onKeyEvent false
                    }
                BarChartOrientation.Horizontal ->
                    when (event.key) {
                        Key.DirectionUp -> -1
                        Key.DirectionDown -> 1
                        else -> return@onKeyEvent false
                    }
            }
        val next =
            KeyboardSelectionRoam.barChartRoam(
                plan = plan,
                current = selectionState.selection,
                delta = delta,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartCandlestickKeyboardSelectionRoam(
    enabled: Boolean,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    candlestickPlan: () -> CandlestickChartPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val plan = candlestickPlan() ?: return@onKeyEvent false
        val (deltaPoint, deltaSeries) = shiftArrowDeltas(event.key) ?: return@onKeyEvent false
        val next =
            KeyboardSelectionRoam.candlestickChartRoam(
                plan = plan,
                current = selectionState.selection,
                deltaPoint = deltaPoint,
                deltaSeries = deltaSeries,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartBoxPlotKeyboardSelectionRoam(
    enabled: Boolean,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    boxPlotPlan: () -> BoxPlotChartPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val plan = boxPlotPlan() ?: return@onKeyEvent false
        val (deltaPoint, deltaSeries) = shiftArrowDeltas(event.key) ?: return@onKeyEvent false
        val next =
            KeyboardSelectionRoam.boxPlotChartRoam(
                plan = plan,
                current = selectionState.selection,
                deltaPoint = deltaPoint,
                deltaSeries = deltaSeries,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartWaterfallKeyboardSelectionRoam(
    enabled: Boolean,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    waterfallPlan: () -> WaterfallChartPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val plan = waterfallPlan() ?: return@onKeyEvent false
        val delta =
            when (event.key) {
                Key.DirectionLeft -> -1
                Key.DirectionRight -> 1
                else -> return@onKeyEvent false
            }
        val next =
            KeyboardSelectionRoam.waterfallChartRoam(
                plan = plan,
                current = selectionState.selection,
                delta = delta,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartCombinedCartesianKeyboardSelectionRoam(
    enabled: Boolean,
    spec: CombinedCartesianChartSpec,
    renderViewport: ChartViewport,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    combinedPlan: () -> CombinedCartesianChartPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val plan = combinedPlan() ?: return@onKeyEvent false
        val layout = plan.layout
        val delta =
            when (event.key) {
                Key.DirectionLeft -> -1
                Key.DirectionRight -> 1
                else -> return@onKeyEvent false
            }
        val next =
            KeyboardSelectionRoam.combinedCartesianRoam(
                spec = spec,
                plan = plan,
                primaryViewport = renderViewport,
                plotBounds = layout.plotBounds,
                current = selectionState.selection,
                delta = delta,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

internal fun Modifier.chartPieKeyboardSelectionRoam(
    enabled: Boolean,
    spec: PieChartSpec,
    selectionState: ChartSelectionState,
    tapSelectionEnabled: Boolean,
    layoutPlan: () -> ChartLayoutPlan?,
): Modifier {
    if (!enabled || !tapSelectionEnabled) return this
    return onKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
        if (!event.isShiftPressed) return@onKeyEvent false
        val layout = layoutPlan() ?: return@onKeyEvent false
        val delta =
            when (event.key) {
                Key.DirectionLeft -> -1
                Key.DirectionRight -> 1
                else -> return@onKeyEvent false
            }
        val next =
            KeyboardSelectionRoam.pieChartRoam(
                spec = spec,
                plotBounds = layout.plotBounds,
                current = selectionState.selection,
                delta = delta,
            ) ?: return@onKeyEvent false
        selectionState.select(next)
        true
    }
}

private fun shiftArrowDeltas(key: Key): Pair<Int, Int>? =
    when (key) {
        Key.DirectionLeft -> -1 to 0
        Key.DirectionRight -> 1 to 0
        Key.DirectionUp -> 0 to -1
        Key.DirectionDown -> 0 to 1
        else -> null
    }
