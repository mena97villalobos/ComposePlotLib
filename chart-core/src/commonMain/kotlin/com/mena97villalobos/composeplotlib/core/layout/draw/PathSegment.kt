package com.mena97villalobos.composeplotlib.core.layout.draw

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD

/**
 * Minimal SVG-style path segment list for [com.mena97villalobos.composeplotlib.core.layout.DrawCommand.Path].
 */
sealed interface PathSegment {
    data class MoveTo(val point: OffsetD) : PathSegment

    data class LineTo(val point: OffsetD) : PathSegment

    data class QuadraticTo(val control: OffsetD, val end: OffsetD) : PathSegment

    data class CubicTo(
        val control1: OffsetD,
        val control2: OffsetD,
        val end: OffsetD,
    ) : PathSegment

    data object Close : PathSegment
}
