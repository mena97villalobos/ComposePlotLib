package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD

/**
 * A freeform annotation drawn over a cartesian plot, anchored in **domain (data) space**. Generalizes [ReferenceLine]
 * (which stays the dedicated helper for full-width/height threshold lines) into marked points, shaded regions, free
 * text, and arbitrary segments. Attach via `annotations` on `LineChartSpec` / `ScatterChartSpec` / `BarChartSpec`.
 * Annotations render above the series (and above reference lines) and are clipped to the plot.
 */
sealed interface ChartAnnotation {
    /**
     * A marked data point with an optional label (like ECharts `markPoint`).
     *
     * @property x Domain x of the point.
     * @property y Domain y of the point.
     * @property color Marker fill color; defaults to [ChartColor.Red].
     * @property radius Marker radius in pixels; defaults to `5.0`.
     * @property label Optional text drawn near the marker.
     * @property labelColor Label color; defaults to [ChartColor.Black].
     * @property labelPlacement Where [label] sits relative to the marker; defaults to [DataLabelPlacement.Above].
     */
    data class Point(
        val x: Double,
        val y: Double,
        val color: ChartColor = ChartColor.Red,
        val radius: Double = 5.0,
        val label: String? = null,
        val labelColor: ChartColor = ChartColor.Black,
        val labelPlacement: DataLabelPlacement = DataLabelPlacement.Above,
    ) : ChartAnnotation

    /**
     * Freeform text anchored at a data point.
     *
     * @property x Domain x anchor.
     * @property y Domain y anchor.
     * @property text Text to draw.
     * @property color Text color; defaults to [ChartColor.Black].
     * @property placement Position of the text box relative to the anchor; defaults to [DataLabelPlacement.Center].
     */
    data class Text(
        val x: Double,
        val y: Double,
        val text: String,
        val color: ChartColor = ChartColor.Black,
        val placement: DataLabelPlacement = DataLabelPlacement.Center,
    ) : ChartAnnotation

    /**
     * A shaded rectangular region (like ECharts `markArea`). A `null` range spans the full plot on that axis, so a
     * `yRange`-only box is a horizontal band and an `xRange`-only box is a vertical band.
     *
     * @property xRange Domain x span, or `null` for the full plot width.
     * @property yRange Domain y span, or `null` for the full plot height.
     * @property fillColor Optional fill (usually translucent); `null` = no fill.
     * @property borderColor Optional border color; `null` = no border.
     * @property borderWidth Border stroke width in pixels; defaults to `1.0`.
     * @property label Optional text drawn at the region's top-left.
     * @property labelColor Label color; defaults to [ChartColor.Black].
     */
    data class Box(
        val xRange: ClosedRangeD? = null,
        val yRange: ClosedRangeD? = null,
        val fillColor: ChartColor? = null,
        val borderColor: ChartColor? = null,
        val borderWidth: Double = 1.0,
        val label: String? = null,
        val labelColor: ChartColor = ChartColor.Black,
    ) : ChartAnnotation

    /**
     * An arbitrary line segment between two data points (optionally dashed), e.g. a trend guide or connector.
     *
     * @property x1 Start domain x.
     * @property y1 Start domain y.
     * @property x2 End domain x.
     * @property y2 End domain y.
     * @property color Stroke color; defaults to [ChartColor.Red].
     * @property strokeWidth Stroke width in pixels; defaults to `1.5`.
     * @property dashIntervals Optional on/off dash lengths; `null` = solid.
     */
    data class Segment(
        val x1: Double,
        val y1: Double,
        val x2: Double,
        val y2: Double,
        val color: ChartColor = ChartColor.Red,
        val strokeWidth: Double = 1.5,
        val dashIntervals: List<Double>? = null,
    ) : ChartAnnotation
}
