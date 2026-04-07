package com.mena97villalobos.composeplotlib.core.spec

/** How the planner resolves overlapping bottom-axis tick labels after measurement. */
enum class XTickLabelOverlapStrategy {
    /**
     * Greedy sweep in increasing X: hide a label when its box would overlap the previous **visible**
     * label on the same row (current default).
     */
    HideOverlapping,

    /**
     * Two-row layout below the plot: when a label would collide on the first row, place it on a second row.
     * If it still collides on the second row, it is hidden. Reserves extra bottom inset for the second row.
     */
    StaggerAdjacent,
}
