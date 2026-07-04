package com.mena97villalobos.composeplotlib.core.layout.draw

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** A single stop in a multi-stop gradient: a [color] at a normalized [offset] (`0..1`) along the gradient. */
data class GradientColorStop(
    val offset: Double,
    val color: ChartColor,
)
