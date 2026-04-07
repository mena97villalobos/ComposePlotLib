package com.mena97villalobos.composeplotlib.core.geometry

/**
 * 2D offset vector in pixel or layout space.
 *
 * @property x Horizontal component.
 * @property y Vertical component.
 */
data class OffsetD(
    val x: Double,
    val y: Double,
) {
    /** Vector addition in the same coordinate space/units. */
    operator fun plus(other: OffsetD): OffsetD = OffsetD(x + other.x, y + other.y)

    /** Vector subtraction in the same coordinate space/units. */
    operator fun minus(other: OffsetD): OffsetD = OffsetD(x - other.x, y - other.y)
}
