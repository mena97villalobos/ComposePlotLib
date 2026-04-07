package com.mena97villalobos.composeplotlib.core.layout.draw

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** How to paint a vector [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Path]. */
sealed interface PathDrawStyle {
    data class Stroke(
        val color: ChartColor,
        val strokeWidth: Double,
        val strokeStyle: StrokeStyle? = null,
    ) : PathDrawStyle

    data class Fill(
        val color: ChartColor,
        val fillRule: FillRule = FillRule.NonZero,
    ) : PathDrawStyle
}
