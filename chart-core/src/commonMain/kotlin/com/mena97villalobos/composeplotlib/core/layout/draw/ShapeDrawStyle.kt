package com.mena97villalobos.composeplotlib.core.layout.draw

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** Fill or stroke for [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.RoundRect] and [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Arc] (including annulus sectors when `Arc.innerRadius` is set). */
sealed interface ShapeDrawStyle {
    data class Fill(
        val color: ChartColor,
    ) : ShapeDrawStyle

    data class Stroke(
        val color: ChartColor,
        val strokeWidth: Double,
        val strokeStyle: StrokeStyle? = null,
    ) : ShapeDrawStyle
}
