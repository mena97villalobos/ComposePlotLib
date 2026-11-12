package com.mena97villalobos.composeplotlib.core.model

import kotlin.math.pow

/**
 * WCAG 2.x relative luminance of this color's RGB channels (`0.0` black … `1.0` white). Alpha is ignored: the
 * result describes the color as if drawn opaque.
 */
fun ChartColor.relativeLuminance(): Double {
    fun channel(value: Int): Double {
        val c = value / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
}

/** WCAG 2.x contrast ratio between this color and [other] (`1.0` … `21.0`), both treated as opaque. */
fun ChartColor.contrastRatio(other: ChartColor): Double {
    val a = relativeLuminance()
    val b = other.relativeLuminance()
    val lighter = maxOf(a, b)
    val darker = minOf(a, b)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * [ChartColor.Black] or [ChartColor.White], whichever contrasts more with this color (treated as opaque). The winner
 * always reaches at least 4.58:1, so text in the returned color meets WCAG AA for normal text on any opaque fill.
 */
fun ChartColor.readableContentColor(): ChartColor = if (contrastRatio(ChartColor.Black) >= contrastRatio(ChartColor.White)) ChartColor.Black else ChartColor.White
