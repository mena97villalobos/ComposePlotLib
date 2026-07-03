package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BubbleSeries
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries

/**
 * Immutable configuration for a bubble chart: [BubbleSeries] over X/Y axes with a per-bubble size ([z]) mapped to a
 * radius by [sizeScale]. Rendering reuses the scatter pipeline — [toScatterSpec] projects each bubble to a
 * [ScatterSeries] point with a per-point `markerRadius`, so bubble charts inherit scatter's planning, hit-testing,
 * pan/zoom, and markers. The size legend is drawn by the `chart-compose` `BubbleChart` from [sizeScale].
 *
 * @property series Bubble series to plot; ids must be unique (checked in `init`).
 * @property sizeScale Maps [z][com.mena97villalobos.composeplotlib.core.model.BubblePoint.z] to a radius; see
 *   [BubbleSizeScale.auto] to derive from the data.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property xAxis X axis appearance and tick behavior; see [AxisSpec].
 * @property yAxis Y axis appearance and tick behavior; see [AxisSpec].
 * @property grid Background grid line configuration; see [GridSpec].
 * @property legend Legend visibility and styling; see [LegendSpec].
 * @property selectionStyle Highlight styling for the selected bubble; see [SelectionStyle].
 * @property contentPadding Minimum padding around chart content in **pixels**.
 */
data class BubbleChartSpec(
    val series: List<BubbleSeries>,
    val sizeScale: BubbleSizeScale = BubbleSizeScale(0.0, 1.0),
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val grid: GridSpec = GridSpec(),
    val legend: LegendSpec = LegendSpec(),
    val selectionStyle: SelectionStyle = SelectionStyle(),
    val contentPadding: InsetsD = InsetsD(left = 56.0, top = 48.0, right = 24.0, bottom = 40.0),
) {
    init {
        requireUniqueIds(series.map { it.id }, "BubbleChartSpec.series ids")
    }

    /** Projects this bubble spec into an equivalent [ScatterChartSpec] with per-point bubble radii and colors. */
    fun toScatterSpec(): ScatterChartSpec =
        ScatterChartSpec(
            series =
                series.map { s ->
                    ScatterSeries(
                        id = s.id,
                        label = s.label,
                        points =
                            s.points.map { p ->
                                ChartPoint(
                                    x = p.x,
                                    y = p.y,
                                    markerRadius = sizeScale.radiusFor(p.z),
                                    markerColor = p.color ?: s.color,
                                )
                            },
                        pointStyle = PointStyle(color = s.color),
                    )
                },
            accessibility = accessibility,
            xAxis = xAxis,
            yAxis = yAxis,
            grid = grid,
            legend = legend,
            selectionStyle = selectionStyle,
            contentPadding = contentPadding,
        )
}
