package com.mena97villalobos.composeplotlib.chart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * How the selection marker/tooltip box is kept on screen relative to chart chrome.
 *
 * Placement uses the measured marker size after the first composition/measure pass.
 */
enum class ChartMarkerContainment {
    /**
     * Keep the marker box inside the **plot** rectangle (data area inside axes).
     * Tooltip may flip above/below the anchor and slide horizontally to avoid clipping.
     */
    PlotBounds,

    /**
     * Keep inside the full **canvas** (plot plus axes, legend gutters, and labels).
     * Useful when overlays should be allowed in axis regions but still avoid going fully off-screen.
     */
    CanvasBounds,

    /**
     * Legacy behavior: pin the tooltip above the anchor with only a non-negative **x** and **y**
     * clamp at the canvas origin (may draw over axes or leave the plot).
     */
    None,
}

/**
 * Supplies a **custom** callout polyline in chart pixels when [ChartMarkerCalloutLeader.customCallout] is set.
 *
 * Return at least two points for a visible stroke, or an empty list to skip drawing.
 * Points are in the same coordinate space as `ChartMarkerOverlay` (chart/canvas pixels).
 */
fun interface ChartMarkerCustomCallout {
    fun polyline(
        anchorX: Double,
        anchorY: Double,
        markerLeft: Double,
        markerTop: Double,
        markerWidth: Double,
        markerHeight: Double,
    ): List<Pair<Double, Double>>
}

/**
 * Optional leader from the data anchor to the marker box (drawn under the marker content).
 *
 * When [enabled] is false, no leader is drawn. When [enabled] is true and [customCallout] is non-null,
 * its [ChartMarkerCustomCallout.polyline] is used; otherwise the built-in **elbow** geometry is used
 * (same as historical behavior).
 *
 * Geometry uses the same chart-pixel space as `ChartMarkerOverlay` after flip/slide placement.
 */
data class ChartMarkerCalloutLeader(
    val enabled: Boolean = false,
    val strokeWidth: Dp = 1.dp,
    val color: Color = Color.White.copy(alpha = 0.5f),
    val customCallout: ChartMarkerCustomCallout? = null,
) {
    companion object {
        val None = ChartMarkerCalloutLeader(enabled = false)

        /** Elbow leader on with default stroke and color. */
        val DefaultElbow = ChartMarkerCalloutLeader(enabled = true)

        /**
         * Enabled callout using a custom polyline (e.g. straight segment, multi-segment, or app-computed path).
         */
        fun custom(
            strokeWidth: Dp = 1.dp,
            color: Color = Color.White.copy(alpha = 0.5f),
            path: ChartMarkerCustomCallout,
        ): ChartMarkerCalloutLeader =
            ChartMarkerCalloutLeader(
                enabled = true,
                strokeWidth = strokeWidth,
                color = color,
                customCallout = path,
            )
    }
}

/**
 * Policy for composable selection markers ([LineChart] / [BarChart] / [ScatterChart] / [CombinedCartesianChart] / [PieChart] `marker` slot).
 *
 * @property verticalGap Space between the anchor point and the edge of the marker box.
 * @property containment Region used for flip/slide placement.
 * @property preferAbove When true, prefer placing the marker **above** the anchor when both sides fit;
 *   otherwise prefer below first.
 * @property callout Optional anchor-to-box leader (elbow or [ChartMarkerCalloutLeader.customCallout]); see [ChartMarkerCalloutLeader].
 */
data class ChartMarkerPlacement(
    val verticalGap: Dp = 12.dp,
    val containment: ChartMarkerContainment = ChartMarkerContainment.PlotBounds,
    val preferAbove: Boolean = true,
    val callout: ChartMarkerCalloutLeader = ChartMarkerCalloutLeader.None,
) {
    companion object {
        val Default = ChartMarkerPlacement()
    }
}
