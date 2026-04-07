package com.mena97villalobos.composeplotlib.chart.theme

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.StackedBarChartSpec

/**
 * Ordered accent colors for cycling across series or bar groups. Use with [withMaterialSeriesColors] /
 * [withMaterialGroupColors] / [ScatterChartSpec.withMaterialScatterSeriesColors].
 */
fun MaterialChartPalette.seriesAccentColors(): List<ChartColor> = listOf(primary, secondary, tertiary, primaryFixed, secondaryFixed, error)

/**
 * Applies [palette] line and point colors per series (topology unchanged). Vertex fill uses the line
 * color when [PointStyle.color] was null.
 *
 * @param themeSelection when true, maps [LineChartSpec.selectionStyle] highlights to [MaterialChartPalette.primary].
 */
fun LineChartSpec.withMaterialSeriesColors(
    palette: MaterialChartPalette,
    themeSelection: Boolean = true,
): LineChartSpec {
    val accents = palette.seriesAccentColors()
    val newSeries =
        series.mapIndexed { index, s ->
            val c = accents[index % accents.size]
            s.copy(
                lineStyle = s.lineStyle.copy(color = c),
                pointStyle = s.pointStyle.copy(color = s.pointStyle.color ?: c),
            )
        }
    val sel =
        if (themeSelection) {
            selectionStyle.copy(
                lineColor = palette.primary,
                pointColor = palette.primary,
            )
        } else {
            selectionStyle
        }
    return copy(series = newSeries, selectionStyle = sel)
}

/**
 * Applies [palette] bar fill colors per group in order.
 */
fun BarChartSpec.withMaterialGroupColors(palette: MaterialChartPalette): BarChartSpec {
    val accents = palette.seriesAccentColors()
    val newGroups =
        groups.mapIndexed { index, g ->
            val c = accents[index % accents.size]
            g.copy(style = g.style.copy(color = c))
        }
    return copy(groups = newGroups)
}

/**
 * Applies [palette] bar fill colors per group in order (same as [BarChartSpec.withMaterialGroupColors]).
 */
fun StackedBarChartSpec.withMaterialGroupColors(palette: MaterialChartPalette): StackedBarChartSpec {
    val accents = palette.seriesAccentColors()
    val newGroups =
        groups.mapIndexed { index, g ->
            val c = accents[index % accents.size]
            g.copy(style = g.style.copy(color = c))
        }
    return copy(groups = newGroups)
}

/**
 * Applies [palette] point colors per scatter series in order.
 */
fun ScatterChartSpec.withMaterialScatterSeriesColors(
    palette: MaterialChartPalette,
    themeSelection: Boolean = true,
): ScatterChartSpec {
    val accents = palette.seriesAccentColors()
    val newSeries =
        series.mapIndexed { index, s ->
            val c = accents[index % accents.size]
            s.copy(pointStyle = s.pointStyle.copy(color = s.pointStyle.color ?: c))
        }
    val sel =
        if (themeSelection) {
            selectionStyle.copy(
                lineColor = palette.primary,
                pointColor = palette.primary,
            )
        } else {
            selectionStyle
        }
    return copy(series = newSeries, selectionStyle = sel)
}
