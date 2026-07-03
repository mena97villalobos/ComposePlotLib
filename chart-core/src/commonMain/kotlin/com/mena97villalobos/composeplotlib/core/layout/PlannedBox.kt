package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.BoxPlotEntry
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotStyle

/**
 * One drawable box-and-whisker from [ChartPlanner] (pixel geometry) plus logical identity for selection.
 * [index] is the **global** entry index within the owning
 * [com.mena97villalobos.composeplotlib.core.model.BoxPlotSeries].
 *
 * @property boxBounds Q1–Q3 box rectangle in plot pixels.
 * @property hitBounds Tappable region spanning min..max and at least the box width.
 * @property medianY Pixel y of the median line.
 * @property whiskerTop Pixel point at the box's upper edge value ([BoxPlotEntry.max]).
 * @property whiskerBottom Pixel point at the box's lower whisker value ([BoxPlotEntry.min]).
 * @property capHalfWidth Half-width (pixels) of the whisker end caps.
 * @property outlierCenters Pixel centers of outlier points.
 */
data class PlannedBox(
    val seriesId: String,
    val index: Int,
    val entry: BoxPlotEntry,
    val boxBounds: RectD,
    val hitBounds: RectD,
    val medianY: Double,
    val whiskerTop: OffsetD,
    val whiskerBottom: OffsetD,
    val capHalfWidth: Double,
    val outlierCenters: List<OffsetD>,
    val style: BoxPlotStyle,
)
