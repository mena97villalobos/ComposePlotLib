package com.mena97villalobos.composeplotlib.core.geometry

/** 2D offset vector in pixel or layout space. */
data class OffsetD(
    val x: Double,
    val y: Double,
) {
    operator fun plus(other: OffsetD): OffsetD = OffsetD(x + other.x, y + other.y)

    operator fun minus(other: OffsetD): OffsetD = OffsetD(x - other.x, y - other.y)
}
