package com.mena97villalobos.composeplotlib.core.domain

/**
 * A single sample in chart **domain** space (not pixels).
 *
 * @property x X coordinate in domain units.
 * @property y Y coordinate in domain units.
 */
data class XyPoint(
    val x: Double,
    val y: Double,
)
