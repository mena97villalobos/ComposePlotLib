package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import kotlin.math.roundToInt

/**
 * Optional **canvas** labels and leader polylines for pie / donut slices (planned in [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planPieChart]).
 *
 * When [enabled] is false, no label commands are emitted (default).
 */
data class PieSliceLabelsSpec(
    val enabled: Boolean = false,
    val format: PieSliceLabelFormat = PieSliceLabelFormat.LabelAndPercent,
    /**
     * Slices with a smaller **central** angle (degrees) skip labels to reduce clutter.
     */
    val minSweepDegrees: Double = 8.0,
    /** Distance from the outer rim along the slice bisector before the elbow (pixels). */
    val leaderStubPx: Double = 12.0,
    /** Horizontal span from the stub toward the plot edge before attaching the label (pixels). */
    val horizontalElbowPx: Double = 32.0,
    /** Minimum inset from [com.mena97villalobos.composeplotlib.core.geometry.RectD] edges when positioning text. */
    val edgePaddingPx: Double = 6.0,
    /** Gap between the elbow anchor and the label box (pixels). */
    val labelGapPx: Double = 4.0,
    val labelColor: ChartColor = ChartColor.Gray,
    val leaderColor: ChartColor = ChartColor.Gray,
    val leaderStrokeWidthPx: Double = 1.0,
)

enum class PieSliceLabelFormat {
    LabelOnly,
    PercentOnly,
    LabelAndPercent,
    ValueOnly,
    LabelAndValue,
}

/** Builds display text for a slice; [percent] is in `0…100` (whole chart total of positive slices). */
fun PieSliceLabelsSpec.formatSliceLabel(
    slice: PieChartSlice,
    percent: Double,
): String {
    val pct = percent.coerceIn(0.0, 100.0).roundToInt()
    val pctStr = "$pct%"
    val valStr = formatChartValue(slice.value)
    return when (format) {
        PieSliceLabelFormat.LabelOnly -> slice.label
        PieSliceLabelFormat.PercentOnly -> pctStr
        PieSliceLabelFormat.LabelAndPercent -> "${slice.label} $pctStr"
        PieSliceLabelFormat.ValueOnly -> valStr
        PieSliceLabelFormat.LabelAndValue -> "${slice.label} $valStr"
    }
}
