package com.mena97villalobos.composeplotlib.core.model

/**
 * One bubble: position ([x], [y]) plus a size value [z] mapped to a radius by
 * [BubbleSizeScale][com.mena97villalobos.composeplotlib.core.spec.BubbleSizeScale].
 *
 * @property x Position along the X axis, in domain space.
 * @property y Position along the Y axis, in domain space.
 * @property z Size value; larger values render as larger bubbles (area-proportional).
 * @property color Per-bubble fill override, or null to use the series color.
 */
data class BubblePoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val color: ChartColor? = null,
)

/**
 * A bubble series (one row in the legend, one default [color]).
 *
 * @property id Stable identifier used by selection.
 * @property label Human-readable name shown in the legend for this series.
 * @property points Bubbles in domain space; see [BubblePoint].
 * @property color Default bubble fill when a [BubblePoint.color] is null; also the legend swatch color.
 */
data class BubbleSeries(
    val id: String,
    val label: String,
    val points: List<BubblePoint>,
    val color: ChartColor = ChartColor.Blue,
)
