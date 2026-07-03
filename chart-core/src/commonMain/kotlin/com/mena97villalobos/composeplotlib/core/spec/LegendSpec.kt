package com.mena97villalobos.composeplotlib.core.spec

/**
 * Legend visibility and style; entries are derived from series or bar groups in the layout planner.
 *
 * @property enabled When true (default), the planner emits legend entries and reserves layout space for them.
 * @property style Text and color-swatch styling for legend entries; see [LegendStyle].
 */
data class LegendSpec(
    val enabled: Boolean = true,
    val style: LegendStyle = LegendStyle(),
)
