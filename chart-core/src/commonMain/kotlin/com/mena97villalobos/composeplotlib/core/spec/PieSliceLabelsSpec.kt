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
    /** When true, the planner emits label text and leader polylines for slices; defaults to `false`. */
    val enabled: Boolean = false,
    /** Content shown per slice; see [PieSliceLabelFormat]. Defaults to [PieSliceLabelFormat.LabelAndPercent]. */
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
    /** Color of the label text; defaults to [ChartColor.Gray]. */
    val labelColor: ChartColor = ChartColor.Gray,
    /** Color of the leader polyline connecting the slice rim to the label; defaults to [ChartColor.Gray]. */
    val leaderColor: ChartColor = ChartColor.Gray,
    /** Stroke width of the leader polyline in pixels; defaults to `1.0`. */
    val leaderStrokeWidthPx: Double = 1.0,
)

/** Which fields a pie/donut slice label combines; see [PieSliceLabelsSpec.format] and [formatSliceLabel]. */
enum class PieSliceLabelFormat {
    /** Only the slice's [PieChartSlice.label]. */
    LabelOnly,

    /** Only the slice's share of the positive total, rendered as a whole percent (e.g. `42%`). */
    PercentOnly,

    /** The [PieChartSlice.label] followed by the percent (e.g. `Rent 42%`). Default. */
    LabelAndPercent,

    /** Only the slice's numeric value, formatted via [formatChartValue]. */
    ValueOnly,

    /** The [PieChartSlice.label] followed by the formatted value (e.g. `Rent 1,200`). */
    LabelAndValue,
}

/**
 * Builds display text for a slice according to [PieSliceLabelsSpec.format].
 *
 * @param slice The slice whose [PieChartSlice.label] and [PieChartSlice.value] supply the text.
 * @param percent The slice's share of the chart total, in `0…100` (whole chart total of positive slices);
 * clamped to that range and rounded to a whole percent.
 * @return The formatted label string for the configured [PieSliceLabelFormat].
 */
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
