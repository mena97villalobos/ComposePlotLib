package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/** A laid-out sunburst node: its ring [depth], angular span (degrees), resolved fill [color], and [label]. */
data class SunburstSlice(
    val depth: Int,
    val startAngleDegrees: Double,
    val sweepAngleDegrees: Double,
    val color: ChartColor,
    val label: String,
)
