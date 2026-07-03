package com.mena97villalobos.composeplotlib.core.spec

/**
 * How stacked bars ([BarLayoutMode.Stacked]) accumulate within a category.
 *
 * Only applies when the resolved layout is [BarLayoutMode.Stacked]; ignored for independent / clustered bars.
 */
enum class BarStackMode {
    /** Segments stack at their raw values; the tallest category total sets the axis span (default). */
    Absolute,

    /**
     * **100% stacking**: each category's segments are normalized to fractions of that category's total, so every
     * stack fills the same height and encodes composition (share of total). Use a `[0, 1]` value viewport (the
     * `BarChart` / `StackedBarChart` default viewport switches to this automatically) and format the axis as a
     * percentage. Non-positive segment values contribute 0.
     */
    PercentOfTotal,
}
