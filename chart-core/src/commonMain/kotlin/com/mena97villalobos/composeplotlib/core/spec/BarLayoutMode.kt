package com.mena97villalobos.composeplotlib.core.spec

/**
 * How multiple [com.mena97villalobos.composeplotlib.core.model.BarGroup]s share category slots.
 *
 * [Clustered] and [Stacked] require every group to have the same [com.mena97villalobos.composeplotlib.core.model.BarGroup.entries]
 * list size; otherwise layout falls back to [Independent] for robustness.
 */
enum class BarLayoutMode {
    /**
     * Each [com.mena97villalobos.composeplotlib.core.model.BarEntry] uses its own x;
     * groups drawn in list order may overlap.
     */
    Independent,

    /**
     * Entries at the same index (category) share one band on the x axis; bars are drawn side by side.
     */
    Clustered,

    /**
     * Entries at the same index stack vertically in group list order (first group at the base).
     */
    Stacked,
}
