package com.mena97villalobos.composeplotlib.core.spec

/** Legend visibility and style; entries are derived from series or bar groups in the layout planner. */
data class LegendSpec(
    val enabled: Boolean = true,
    val style: LegendStyle = LegendStyle(),
)
