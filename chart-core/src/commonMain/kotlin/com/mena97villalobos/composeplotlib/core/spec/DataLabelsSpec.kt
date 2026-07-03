package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * Optional value labels drawn at each data point / bar. Off by default. When enabled the planner emits a text label
 * per mark, formatted by [valueFormatter] and positioned per [placement]; when [avoidOverlap] is on, labels whose
 * boxes would collide with an already-placed label (or fall outside the plot) are dropped so the rest stay legible.
 *
 * @property enabled Master switch; `false` (default) draws no labels.
 * @property valueFormatter Formats a mark's value (a point's `y`, a bar's height) into label text. Defaults to a
 * compact numeric format (`9.0` → `"9"`, `9.25` → `"9.3"`).
 * @property placement Where the label sits relative to its mark; defaults to [DataLabelPlacement.Above].
 * @property color Label text color; defaults to [ChartColor.Black].
 * @property offsetPx Gap in pixels between the mark and the label box; defaults to `6.0`.
 * @property avoidOverlap When `true` (default), drop labels that would overlap an already-placed label or leave the plot.
 */
data class DataLabelsSpec(
    val enabled: Boolean = false,
    val valueFormatter: (Double) -> String = ::formatChartValue,
    val placement: DataLabelPlacement = DataLabelPlacement.Above,
    val color: ChartColor = ChartColor.Black,
    val offsetPx: Double = 6.0,
    val avoidOverlap: Boolean = true,
)

/** Where a [DataLabelsSpec] label is placed relative to its mark (point center or bar top-center). */
enum class DataLabelPlacement {
    /** Above the mark (default), horizontally centered. */
    Above,

    /** Below the mark, horizontally centered. */
    Below,

    /** Left of the mark, vertically centered. */
    Left,

    /** Right of the mark, vertically centered. */
    Right,

    /** Centered on the mark. */
    Center,
}
