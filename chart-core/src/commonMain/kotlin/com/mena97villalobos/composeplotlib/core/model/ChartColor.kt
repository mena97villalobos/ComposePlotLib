package com.mena97villalobos.composeplotlib.core.model

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ARGB color as `0xAARRGGBB`. The Compose layer maps this to platform colors when drawing.
 *
 * Construct from packed ARGB directly, or via the ergonomic factories: [rgb] / [argb] (0..255 channels),
 * [hex] (`"#RRGGBB"` / `"#AARRGGBB"` / `"#RGB"`), and [hsl] (hue 0..360, saturation/lightness 0..1). Derive a
 * translucent variant with [withAlpha]. Individual channels are exposed via [alpha] / [red] / [green] / [blue].
 *
 * @property argb Packed ARGB value where the high byte is alpha and the low three bytes are red, green, blue.
 */
data class ChartColor(
    val argb: Long,
) {
    /** Alpha channel as an integer in `0..255`. */
    val alpha: Int get() = ((argb shr 24) and 0xFF).toInt()

    /** Red channel as an integer in `0..255`. */
    val red: Int get() = ((argb shr 16) and 0xFF).toInt()

    /** Green channel as an integer in `0..255`. */
    val green: Int get() = ((argb shr 8) and 0xFF).toInt()

    /** Blue channel as an integer in `0..255`. */
    val blue: Int get() = (argb and 0xFF).toInt()

    /**
     * Returns a copy of this color with its alpha replaced by [alpha] (a fraction in `0..1`, clamped); the RGB
     * channels are preserved. `withAlpha(0.0)` is fully transparent, `withAlpha(1.0)` fully opaque.
     */
    fun withAlpha(alpha: Double): ChartColor {
        val a = (alpha.coerceIn(0.0, 1.0) * 255.0).roundToInt().toLong()
        return ChartColor((a shl 24) or (argb and 0x00FFFFFFL))
    }

    companion object {
        /** Opaque black (`0xFF000000`). */
        val Black = ChartColor(0xFF000000)

        /** Opaque white (`0xFFFFFFFF`). */
        val White = ChartColor(0xFFFFFFFF)

        /** Opaque medium gray (`0xFF9E9E9E`); default for axis/legend text. */
        val Gray = ChartColor(0xFF9E9E9E)

        /** Opaque light gray (`0xFFE0E0E0`); default for grid and axis lines. */
        val LightGray = ChartColor(0xFFE0E0E0)

        /** Opaque blue (`0xFF1565C0`); default line/point color. */
        val Blue = ChartColor(0xFF1565C0)

        /** Opaque green (`0xFF2E7D32`); default bar/bullish color. */
        val Green = ChartColor(0xFF2E7D32)

        /** Opaque red (`0xFFC62828`); default selection/bearish color. */
        val Red = ChartColor(0xFFC62828)

        /** Fully transparent (`0x00000000`); useful as a "no fill" sentinel. */
        val Transparent = ChartColor(0x00000000)

        /**
         * Opaque color from 0..255 [red] / [green] / [blue] channels (each clamped to `0..255`).
         * Equivalent to `argb(255, red, green, blue)`.
         */
        fun rgb(
            red: Int,
            green: Int,
            blue: Int,
        ): ChartColor = argb(255, red, green, blue)

        /** Color from 0..255 [alpha] / [red] / [green] / [blue] channels (each clamped to `0..255`). */
        fun argb(
            alpha: Int,
            red: Int,
            green: Int,
            blue: Int,
        ): ChartColor {
            val a = alpha.coerceIn(0, 255).toLong()
            val r = red.coerceIn(0, 255).toLong()
            val g = green.coerceIn(0, 255).toLong()
            val b = blue.coerceIn(0, 255).toLong()
            return ChartColor((a shl 24) or (r shl 16) or (g shl 8) or b)
        }

        /**
         * Parses a hex color string. Accepts an optional leading `#` and one of:
         * `RGB` (3-digit shorthand, opaque), `RRGGBB` (6-digit, opaque), or `AARRGGBB` (8-digit, with alpha).
         * The 8-digit form is alpha-first (`AARRGGBB`) to match [argb]. Case-insensitive.
         *
         * @throws IllegalArgumentException if the string is not 3/6/8 hex digits.
         */
        fun hex(hex: String): ChartColor {
            val s = hex.trim().removePrefix("#")
            val expanded =
                when (s.length) {
                    3 ->
                        buildString(6) {
                            for (c in s) {
                                append(c)
                                append(c)
                            }
                        }
                    6, 8 -> s
                    else -> throw IllegalArgumentException("Invalid hex color \"$hex\": expected 3, 6, or 8 hex digits")
                }
            // Validate every character is a hex digit first: `toLong(radix)` would otherwise accept a leading
            // '+'/'-' sign (e.g. "-fffff"), silently producing a malformed / negative ARGB value.
            if (expanded.any { it.digitToIntOrNull(16) == null }) {
                throw IllegalArgumentException("Invalid hex color \"$hex\": not a hexadecimal value")
            }
            val value = expanded.toLong(16)
            val argb = if (expanded.length == 8) value else (0xFF000000L or value)
            return ChartColor(argb)
        }

        /**
         * Color from HSL: [hue] in degrees (wrapped to `0..360`), [saturation] and [lightness] as fractions in
         * `0..1` (clamped), and optional [alpha] fraction in `0..1` (default opaque). Uses the standard HSL→RGB
         * conversion.
         */
        fun hsl(
            hue: Double,
            saturation: Double,
            lightness: Double,
            alpha: Double = 1.0,
        ): ChartColor {
            val h = ((hue % 360.0) + 360.0) % 360.0
            val s = saturation.coerceIn(0.0, 1.0)
            val l = lightness.coerceIn(0.0, 1.0)
            val c = (1.0 - abs(2.0 * l - 1.0)) * s
            val hp = h / 60.0
            val x = c * (1.0 - abs(hp % 2.0 - 1.0))
            val (r1, g1, b1) =
                when {
                    hp < 1.0 -> Triple(c, x, 0.0)
                    hp < 2.0 -> Triple(x, c, 0.0)
                    hp < 3.0 -> Triple(0.0, c, x)
                    hp < 4.0 -> Triple(0.0, x, c)
                    hp < 5.0 -> Triple(x, 0.0, c)
                    else -> Triple(c, 0.0, x)
                }
            val m = l - c / 2.0
            return argb(
                alpha = (alpha.coerceIn(0.0, 1.0) * 255.0).roundToInt(),
                red = ((r1 + m) * 255.0).roundToInt(),
                green = ((g1 + m) * 255.0).roundToInt(),
                blue = ((b1 + m) * 255.0).roundToInt(),
            )
        }
    }
}
