package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.lighten
import kotlin.math.floor

/**
 * A multi-stop color gradient over `t` in `0..1`, interpolated linearly (in sRGB) between evenly-spaced [stops].
 * Used for **sequential** (viridis, turbo, plasma) and **diverging** (RdBu, BrBG) encodings — pass a normalized
 * value to [colorAt]. Named companions ship the standard tables. KMP-safe.
 *
 * @property stops Ordered gradient colors; the first maps to `t = 0`, the last to `t = 1`.
 */
class ColorGradient(
    val stops: List<ChartColor>,
) {
    init {
        require(stops.isNotEmpty()) { "ColorGradient requires at least one stop" }
    }

    /** Interpolated color at [t] (clamped to `0..1`). */
    fun colorAt(t: Double): ChartColor {
        if (stops.size == 1) return stops[0]
        val scaled = t.coerceIn(0.0, 1.0) * (stops.size - 1)
        val lo = floor(scaled).toInt().coerceIn(0, stops.size - 1)
        val hi = (lo + 1).coerceAtMost(stops.size - 1)
        return lerpChartColor(stops[lo], stops[hi], scaled - lo)
    }

    /** Samples the gradient into [count] evenly-spaced colors (e.g. to build a discrete legend). */
    fun sample(count: Int): List<ChartColor> {
        if (count <= 0) return emptyList()
        if (count == 1) return listOf(colorAt(0.0))
        return (0 until count).map { colorAt(it.toDouble() / (count - 1)) }
    }

    companion object {
        /** Perceptually-uniform sequential (matplotlib **viridis**). */
        val Viridis: ColorGradient =
            gradientOf(0xFF440154, 0xFF472D7B, 0xFF3B528B, 0xFF2C728E, 0xFF21918C, 0xFF28AE80, 0xFF5EC962, 0xFFADDC30, 0xFFFDE725)

        /** Perceptually-uniform sequential (matplotlib **plasma**). */
        val Plasma: ColorGradient =
            gradientOf(0xFF0D0887, 0xFF5302A3, 0xFF8B0AA5, 0xFFB83289, 0xFFDB5C68, 0xFFF48849, 0xFFFEBD2A, 0xFFF0F921)

        /** High-contrast sequential rainbow (Google **turbo**). */
        val Turbo: ColorGradient =
            gradientOf(0xFF30123B, 0xFF4145AB, 0xFF4675ED, 0xFF39A2FC, 0xFF1BCFD4, 0xFF24ECA6, 0xFF61FC6C, 0xFFA4FC3B, 0xFFD1E834, 0xFFFE9B2D, 0xFFEA5F18, 0xFF7A0403)

        /** Diverging red–white–blue (ColorBrewer **RdBu**), for signed / centered data. */
        val RdBu: ColorGradient =
            gradientOf(0xFFB2182B, 0xFFEF8A62, 0xFFFDDBC7, 0xFFF7F7F7, 0xFFD1E5F0, 0xFF67A9CF, 0xFF2166AC)

        /** Diverging brown–white–teal (ColorBrewer **BrBG**). */
        val BrBG: ColorGradient =
            gradientOf(0xFF8C510A, 0xFFD8B365, 0xFFF6E8C3, 0xFFF5F5F5, 0xFFC7EAE5, 0xFF5AB4AC, 0xFF01665E)

        private fun gradientOf(vararg argb: Long): ColorGradient = ColorGradient(argb.map { ChartColor(it) })
    }
}

/**
 * Named **categorical** color palettes (qualitative — assign one color per series / category by position). Use
 * [cyclic] to index with wrap-around when there are more series than colors.
 */
object ChartPalettes {
    /** Tableau 10 — professional, colorblind-considerate qualitative defaults (matches the Sankey node palette). */
    val Tableau10: List<ChartColor> =
        paletteOf(0xFF4E79A7, 0xFFF28E2B, 0xFFE15759, 0xFF76B7B2, 0xFF59A14F, 0xFFEDC948, 0xFFB07AA1, 0xFFFF9DA7, 0xFF9C755F, 0xFFBAB0AC)

    /** D3 / matplotlib **category10** qualitative palette. */
    val Category10: List<ChartColor> =
        paletteOf(0xFF1F77B4, 0xFFFF7F0E, 0xFF2CA02C, 0xFFD62728, 0xFF9467BD, 0xFF8C564B, 0xFFE377C2, 0xFF7F7F7F, 0xFFBCBD22, 0xFF17BECF)

    /** Okabe–Ito 8-color qualitative palette, designed to be distinguishable for color-vision deficiencies. */
    val ColorblindSafe: List<ChartColor> =
        paletteOf(0xFFE69F00, 0xFF56B4E9, 0xFF009E73, 0xFFF0E442, 0xFF0072B2, 0xFFD55E00, 0xFFCC79A7, 0xFF000000)

    /** Returns `palette[index mod palette.size]`, cycling through the palette for large [index]. Empty → black. */
    fun cyclic(
        palette: List<ChartColor>,
        index: Int,
    ): ChartColor {
        if (palette.isEmpty()) return ChartColor.Black
        val i = ((index % palette.size) + palette.size) % palette.size
        return palette[i]
    }

    /**
     * Adapts a categorical [palette] for a **dark background** by tinting each color toward white by [amount]
     * (`0..1`), so the mid-tone qualitative defaults keep enough contrast to read. The planner does not shift
     * series colors on its own; pass the result into your specs when rendering on a dark surface.
     */
    fun forDarkBackground(
        palette: List<ChartColor>,
        amount: Double = 0.2,
    ): List<ChartColor> = palette.map { it.lighten(amount) }

    private fun paletteOf(vararg argb: Long): List<ChartColor> = argb.map { ChartColor(it) }
}

/** Linear sRGB interpolation between [a] and [b] at [t] in `0..1`. Shared by gradients and color scales. */
internal fun lerpChartColor(
    a: ChartColor,
    b: ChartColor,
    t: Double,
): ChartColor {
    val clamped = t.coerceIn(0.0, 1.0)

    fun channel(shift: Int): Long {
        val ca = (a.argb shr shift) and 0xFFL
        val cb = (b.argb shr shift) and 0xFFL
        return (ca + ((cb - ca) * clamped)).toLong().coerceIn(0L, 0xFFL)
    }
    return ChartColor(
        (channel(24) shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0),
    )
}
