package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

/**
 * Payload for a custom marker composable: core [selection], series metadata, and preformatted labels.
 *
 * All label strings are already formatted for display; the marker composable renders them verbatim.
 *
 * @property selection The resolved [ChartSelection] (logical target plus pixel geometry) the marker describes.
 * @property seriesId Stable identifier of the selected series.
 * @property seriesLabel Human-readable series name shown in the marker.
 * @property xLabel Preformatted x-coordinate label of the selected point.
 * @property yLabel Preformatted y-coordinate label of the selected point.
 * @property valueLabel Preformatted primary value label emphasized in the marker.
 * @property accessibilitySummary Optional TalkBack / VoiceOver sentence; when `null` (default) a summary is
 * derived by [accessibilitySummaryText].
 */
data class ChartMarkerData(
    val selection: ChartSelection,
    val seriesId: String,
    val seriesLabel: String,
    val xLabel: String,
    val yLabel: String,
    val valueLabel: String,
    val accessibilitySummary: String? = null,
)

/** Text for semantics when [ChartMarkerData.accessibilitySummary] is null. */
internal fun ChartMarkerData.accessibilitySummaryText(): String = accessibilitySummary ?: "Selected $seriesLabel. $valueLabel. X $xLabel, Y $yLabel."

/**
 * Material-styled tooltip marker used by the Android showcase samples.
 *
 * Renders [ChartMarkerData.seriesLabel], [ChartMarkerData.valueLabel] and a coordinate row on a dark rounded card.
 *
 * @param data Marker payload with the selection metadata and preformatted labels.
 * @param modifier [Modifier] applied to the marker card; defaults to [Modifier].
 * @param showSeriesLabel When false, hides the first (series name) line; defaults to `true`.
 * @param showCoordinateLine When false, hides the compact `x=, y=` detail row; defaults to `true`.
 * @param showOutline When true, draws a one-dp translucent outline for contrast on busy backgrounds; defaults to `false`.
 */
@Composable
fun DefaultChartMarker(
    data: ChartMarkerData,
    modifier: Modifier = Modifier,
    showSeriesLabel: Boolean = true,
    showCoordinateLine: Boolean = true,
    showOutline: Boolean = false,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier =
            modifier
                .then(
                    if (showOutline) {
                        Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                    } else {
                        Modifier
                    },
                ).background(
                    color = Color(0xCC1C1C1C),
                    shape = shape,
                ).padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (showSeriesLabel) {
            Text(
                text = data.seriesLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
        Text(
            text = data.valueLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        if (showCoordinateLine) {
            Text(
                text = "x=${data.xLabel}, y=${data.yLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}
