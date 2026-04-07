package com.mena97villalobos.composeplotlib.chart.internal.marker

import com.mena97villalobos.composeplotlib.chart.ChartMarkerData
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import kotlin.math.round

/** Builds [ChartMarkerData] for the selected line point using spec formatters. */
internal fun buildLineMarkerData(
    spec: LineChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    val series = spec.series.firstOrNull { it.id == selection.target.seriesId } ?: return null
    val point =
        series.points.getOrNull(selection.target.pointIndex)
            ?: ChartPoint(selection.target.point.x, selection.target.point.y)
    return ChartMarkerData(
        selection = selection,
        seriesId = series.id,
        seriesLabel = series.label,
        xLabel = spec.xAxis.labelFormatter(point.x),
        yLabel = spec.yAxis.labelFormatter(point.y),
        valueLabel = spec.valueFormatter(point),
    )
}

/** Builds [ChartMarkerData] for the selected scatter point using spec formatters. */
internal fun buildScatterMarkerData(
    spec: ScatterChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    val series = spec.series.firstOrNull { it.id == selection.target.seriesId } ?: return null
    val point =
        series.points.getOrNull(selection.target.pointIndex)
            ?: ChartPoint(selection.target.point.x, selection.target.point.y)
    return ChartMarkerData(
        selection = selection,
        seriesId = series.id,
        seriesLabel = series.label,
        xLabel = spec.xAxis.labelFormatter(point.x),
        yLabel = spec.yAxis.labelFormatter(point.y),
        valueLabel = spec.valueFormatter(point),
    )
}

/** Builds [ChartMarkerData] for the selected pie slice. */
internal fun buildPieMarkerData(
    spec: PieChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    if (selection.target.seriesId != spec.id) return null
    val slice = spec.slices.getOrNull(selection.target.pointIndex) ?: return null
    val totalPositive = spec.slices.filter { it.value > 0.0 }.sumOf { it.value }
    val pct = if (totalPositive > 0.0) 100.0 * slice.value / totalPositive else 0.0
    val pctRounded = round(pct * 10.0) / 10.0
    val pctLabel = "$pctRounded%"
    val valueLabel = spec.sliceValueFormatter(slice)
    return ChartMarkerData(
        selection = selection,
        seriesId = spec.id,
        seriesLabel = slice.label,
        xLabel = pctLabel,
        yLabel = "value=${slice.value}",
        valueLabel = valueLabel,
        accessibilitySummary = "Selected ${slice.label}. $valueLabel. $pctLabel of chart.",
    )
}

/** Builds [ChartMarkerData] for the selected radar vertex. */
internal fun buildRadarMarkerData(
    spec: RadarChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    val series = spec.series.firstOrNull { it.id == selection.target.seriesId } ?: return null
    val axisIndex = selection.target.pointIndex
    val axisLabel = spec.axes.getOrNull(axisIndex) ?: return null
    val value = selection.target.point.y
    val valueLabel = spec.axisValueFormatter(axisLabel, value)
    return ChartMarkerData(
        selection = selection,
        seriesId = series.id,
        seriesLabel = series.label,
        xLabel = axisLabel,
        yLabel = "",
        valueLabel = valueLabel,
        accessibilitySummary = "Selected ${series.label}, $axisLabel. $valueLabel.",
    )
}

/** Builds [ChartMarkerData] for combined cartesian selection using per-layer formatters. */
internal fun buildCombinedMarkerData(
    spec: CombinedCartesianChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    val layer =
        spec.layers.firstOrNull { l ->
            when {
                selection.target.layerId != null -> l.id == selection.target.layerId
                else ->
                    when (l) {
                        is CombinedCartesianLayer.Line -> l.series.any { it.id == selection.target.seriesId }
                        is CombinedCartesianLayer.Bar -> l.groups.any { it.id == selection.target.seriesId }
                        is CombinedCartesianLayer.Scatter -> l.series.any { it.id == selection.target.seriesId }
                    }
            }
        } ?: return null
    val yAxisForLayer =
        when (layer.yAxisBinding) {
            CombinedCartesianYAxisBinding.SharedY -> spec.yAxis
            CombinedCartesianYAxisBinding.SecondaryY -> spec.yAxisSecondary ?: spec.yAxis
        }
    return when (layer) {
        is CombinedCartesianLayer.Line -> {
            val series = layer.series.firstOrNull { it.id == selection.target.seriesId } ?: return null
            val point =
                series.points.getOrNull(selection.target.pointIndex)
                    ?: ChartPoint(selection.target.point.x, selection.target.point.y)
            ChartMarkerData(
                selection = selection,
                seriesId = series.id,
                seriesLabel = series.label,
                xLabel = spec.xAxis.labelFormatter(point.x),
                yLabel = yAxisForLayer.labelFormatter(point.y),
                valueLabel = spec.valueFormatter(point),
            )
        }
        is CombinedCartesianLayer.Bar -> {
            val group = layer.groups.firstOrNull { it.id == selection.target.seriesId } ?: return null
            val entry =
                group.entries.getOrNull(selection.target.pointIndex)
                    ?: BarEntry(selection.target.point.x, selection.target.point.y)
            val categoryOrdinal = selection.target.pointIndex + 1
            val layout =
                if (layer.barLayout == BarLayoutMode.Clustered || layer.barLayout == BarLayoutMode.Stacked) {
                    if (layer.groups.isEmpty()) {
                        BarLayoutMode.Independent
                    } else {
                        val n = layer.groups.first().entries.size
                        if (layer.groups.all { it.entries.size == n }) layer.barLayout else BarLayoutMode.Independent
                    }
                } else {
                    layer.barLayout
                }
            val groupIndex = layer.groups.indexOfFirst { it.id == group.id }.let { if (it < 0) 0 else it }
            val (xLab, yLab) =
                when (layer.orientation) {
                    BarChartOrientation.Vertical ->
                        spec.xAxis.labelFormatter(entry.x) to yAxisForLayer.labelFormatter(entry.y)
                    BarChartOrientation.Horizontal ->
                        spec.xAxis.labelFormatter(entry.y) to yAxisForLayer.labelFormatter(entry.x)
                }
            val accessibilitySummary =
                when (layout) {
                    BarLayoutMode.Stacked -> {
                        val stackDepth = layer.groups.size
                        "Selected stacked segment ${groupIndex + 1} of $stackDepth, ${group.label}. " +
                            "${spec.barValueFormatter(entry)}. Category $categoryOrdinal, X $xLab, segment value Y $yLab."
                    }
                    BarLayoutMode.Clustered ->
                        "Selected ${group.label}, category $categoryOrdinal. ${spec.barValueFormatter(entry)}. X $xLab, Y $yLab."
                    BarLayoutMode.Independent -> null
                }
            ChartMarkerData(
                selection = selection,
                seriesId = group.id,
                seriesLabel = group.label,
                xLabel = xLab,
                yLabel = yLab,
                valueLabel = spec.barValueFormatter(entry),
                accessibilitySummary = accessibilitySummary,
            )
        }
        is CombinedCartesianLayer.Scatter -> {
            val series = layer.series.firstOrNull { it.id == selection.target.seriesId } ?: return null
            val point =
                series.points.getOrNull(selection.target.pointIndex)
                    ?: ChartPoint(selection.target.point.x, selection.target.point.y)
            ChartMarkerData(
                selection = selection,
                seriesId = series.id,
                seriesLabel = series.label,
                xLabel = spec.xAxis.labelFormatter(point.x),
                yLabel = yAxisForLayer.labelFormatter(point.y),
                valueLabel = spec.valueFormatter(point),
            )
        }
    }
}

/** Builds [ChartMarkerData] for the selected bar entry using spec formatters. */
internal fun buildBarMarkerData(
    spec: BarChartSpec,
    selection: ChartSelection,
): ChartMarkerData? {
    val group = spec.groups.firstOrNull { it.id == selection.target.seriesId } ?: return null
    val entry =
        group.entries.getOrNull(selection.target.pointIndex)
            ?: BarEntry(selection.target.point.x, selection.target.point.y)
    val categoryOrdinal = selection.target.pointIndex + 1
    val layout = spec.resolvedBarLayout()
    val groupIndex = spec.groups.indexOfFirst { it.id == group.id }.let { if (it < 0) 0 else it }
    val (xLab, yLab) =
        when (spec.orientation) {
            BarChartOrientation.Vertical ->
                spec.xAxis.labelFormatter(entry.x) to spec.yAxis.labelFormatter(entry.y)
            BarChartOrientation.Horizontal ->
                spec.xAxis.labelFormatter(entry.y) to spec.yAxis.labelFormatter(entry.x)
        }
    val accessibilitySummary =
        when (layout) {
            BarLayoutMode.Stacked -> {
                val stackDepth = spec.groups.size
                "Selected stacked segment ${groupIndex + 1} of $stackDepth, ${group.label}. " +
                    "${spec.valueFormatter(entry)}. Category $categoryOrdinal, X $xLab, segment value Y $yLab."
            }
            BarLayoutMode.Clustered ->
                "Selected ${group.label}, category $categoryOrdinal. ${spec.valueFormatter(entry)}. X $xLab, Y $yLab."
            BarLayoutMode.Independent -> null
        }
    return ChartMarkerData(
        selection = selection,
        seriesId = group.id,
        seriesLabel = group.label,
        xLabel = xLab,
        yLabel = yLab,
        valueLabel = spec.valueFormatter(entry),
        accessibilitySummary = accessibilitySummary,
    )
}
