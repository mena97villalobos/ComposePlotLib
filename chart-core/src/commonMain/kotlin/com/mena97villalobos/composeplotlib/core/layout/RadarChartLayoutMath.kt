package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object RadarChartLayoutMath {
    fun spokeAnglesDegrees(
        axisCount: Int,
        startAngleDegrees: Double,
    ): DoubleArray {
        if (axisCount <= 0) return DoubleArray(0)
        val step = 360.0 / axisCount.toDouble()
        return DoubleArray(axisCount) { i -> startAngleDegrees + (i.toDouble() * step) }
    }

    fun polarToPixel(
        center: OffsetD,
        radiusPx: Double,
        angleDegrees: Double,
    ): OffsetD {
        val rad = angleDegrees * (PI / 180.0)
        return OffsetD(
            x = center.x + radiusPx * cos(rad),
            y = center.y + radiusPx * sin(rad),
        )
    }
}

