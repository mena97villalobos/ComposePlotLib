package com.mena97villalobos.composeplotlib.chart.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/** Holds optional [ChartSelection] for chart tap feedback and marker overlays. */
class ChartSelectionState {
    var selection by mutableStateOf<ChartSelection?>(null)
        private set

    /** Replaces the current selection (or clears when `null`). */
    fun select(selection: ChartSelection?) {
        this.selection = selection
    }

    /** Clears [selection]. */
    fun clear() {
        selection = null
    }
}
