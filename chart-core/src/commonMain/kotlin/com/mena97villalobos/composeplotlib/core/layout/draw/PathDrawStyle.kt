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

    /**
     * Vertical linear-gradient fill: [topColor] at [topY] fading to [bottomColor] at [bottomY] (pixel space).
     * Used for area fills with an alpha-fade look; renderers build the platform gradient brush.
     */
    data class VerticalGradientFill(
        val topColor: ChartColor,
        val bottomColor: ChartColor,
        val topY: Double,
        val bottomY: Double,
        val fillRule: FillRule = FillRule.NonZero,
    ) : PathDrawStyle

    /**
     * General linear-gradient fill between two pixel-space points: [startColor] at ([startX], [startY])
     * fading to [endColor] at ([endX], [endY]). Orientation-agnostic (sankey ribbons use source→target
     * attach-point midpoints); renderers build the platform gradient brush.
     */
    data class LinearGradientFill(
        val startColor: ChartColor,
        val endColor: ChartColor,
        val startX: Double,
        val startY: Double,
        val endX: Double,
        val endY: Double,
        val fillRule: FillRule = FillRule.NonZero,
    ) : PathDrawStyle

    /**
     * **Radial** multi-stop gradient fill: [stops] run from the center `([centerX], [centerY])` (offset `0`) out to
     * [radius] (offset `1`), all in pixel space. Supports any number of color stops. Renderers build the platform
     * radial-gradient brush.
     */
    data class RadialGradientFill(
        val stops: List<GradientColorStop>,
        val centerX: Double,
        val centerY: Double,
        val radius: Double,
        val fillRule: FillRule = FillRule.NonZero,
    ) : PathDrawStyle

    /**
     * **Pattern / decal** fill: a repeating [pattern] in [color] over an optional [backgroundColor], clipped to the
     * path. Print- and colorblind-friendly (encodes categories by texture, not just hue).
     *
     * @property spacingPx tile pitch in pixels (line spacing / dot grid step).
     * @property strokeWidthPx line/dot weight in pixels.
     */
    data class PatternFill(
        val pattern: FillPattern,
        val color: ChartColor,
        val backgroundColor: ChartColor? = null,
        val spacingPx: Double = 6.0,
        val strokeWidthPx: Double = 1.5,
        val fillRule: FillRule = FillRule.NonZero,
    ) : PathDrawStyle
}
