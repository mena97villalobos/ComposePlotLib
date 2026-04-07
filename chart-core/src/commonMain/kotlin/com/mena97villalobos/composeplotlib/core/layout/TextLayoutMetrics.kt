package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import kotlin.math.max

/**
 * Measured label geometry in pixels: [size] box plus baseline distances from the **top** of that box.
 *
 * Used so axis and legend placement can align to typographic baselines instead of raw bounding boxes.
 */
data class TextLayoutMetrics(
    val width: Double,
    val height: Double,
    /** Distance from the top of the layout box to the first line’s baseline. */
    val firstBaselineFromTop: Double,
    /** Distance from the top to the last line’s baseline (multi-line); equals [firstBaselineFromTop] for a single line. */
    val lastBaselineFromTop: Double,
) {
    init {
        require(width >= 0.0 && height >= 0.0) { "Text layout width and height must be non-negative." }
    }

    val size: SizeD get() = SizeD(width, height)

    companion object {
        /** Single-line guess when real measurement is unavailable (tests, first layout pass). */
        fun uniformBox(
            size: SizeD,
            baselineFromTop: Double = (size.height * 0.77).coerceIn(0.0, size.height),
        ): TextLayoutMetrics =
            TextLayoutMetrics(
                width = size.width,
                height = size.height,
                firstBaselineFromTop = baselineFromTop,
                lastBaselineFromTop = baselineFromTop,
            )

        /** Conservative merge when the same label string is measured with different axis styles. */
        fun merge(
            a: TextLayoutMetrics,
            b: TextLayoutMetrics,
        ): TextLayoutMetrics =
            TextLayoutMetrics(
                width = max(a.width, b.width),
                height = max(a.height, b.height),
                firstBaselineFromTop = max(a.firstBaselineFromTop, b.firstBaselineFromTop),
                lastBaselineFromTop = max(a.lastBaselineFromTop, b.lastBaselineFromTop),
            )
    }
}
