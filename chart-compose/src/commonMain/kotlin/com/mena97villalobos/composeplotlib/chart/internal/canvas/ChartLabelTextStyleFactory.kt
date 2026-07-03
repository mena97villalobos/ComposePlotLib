package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BulletChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.WaterfallChartSpec

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: LineChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(
    @Suppress("UNUSED_PARAMETER") spec: GaugeChartSpec,
): ChartLabelTextStyles {
    // A gauge draws no text, so any valid style set works; use the default typography slot.
    val y = MaterialTheme.typography.labelSmall
    return ChartLabelTextStyles(xAxis = y, yAxis = y, legend = y, pieSliceLabel = y, yAxisSecondaryTick = y)
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: BarChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: CandlestickChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(
    @Suppress("UNUSED_PARAMETER") spec: BulletChartSpec,
): ChartLabelTextStyles {
    // Bullet draws only min/max bound labels via the x-axis text slot.
    val y = MaterialTheme.typography.labelSmall
    return ChartLabelTextStyles(xAxis = y, yAxis = y, legend = y, pieSliceLabel = y, yAxisSecondaryTick = y)
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: BoxPlotChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: WaterfallChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    val legend = legendStyleFrom(typo, LegendStyle())
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: ScatterChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = tickStyleFrom(typo, spec.yAxis.labelStyle)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = y,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: PieChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val y = typo.labelSmall
    return ChartLabelTextStyles(
        xAxis = y,
        yAxis = y,
        legend = legend,
        pieSliceLabel = typo.labelSmall.copy(color = spec.sliceLabels.labelColor.toComposeColor()),
        yAxisSecondaryTick = y,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: CombinedCartesianChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val yPrimary = tickStyleFrom(typo, spec.yAxis.labelStyle)
    val ySec = spec.yAxisSecondary
    val ySecondary = if (ySec != null) tickStyleFrom(typo, ySec.labelStyle) else yPrimary
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = yPrimary,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = ySecondary,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: SankeyChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val label = legend
    return ChartLabelTextStyles(
        xAxis = label,
        yAxis = label,
        legend = legend,
        pieSliceLabel = label,
        yAxisSecondaryTick = label,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: RadarChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    val axis = tickStyleFrom(typo, spec.axisLabelStyle)
    return ChartLabelTextStyles(
        xAxis = axis,
        yAxis = axis,
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = axis,
    )
}

@Composable
@ReadOnlyComposable
internal fun chartLabelTextStyles(spec: HeatmapChartSpec): ChartLabelTextStyles {
    val typo = MaterialTheme.typography
    val legend = legendStyleFrom(typo, spec.legend.style)
    return ChartLabelTextStyles(
        xAxis = tickStyleFrom(typo, spec.xAxis.labelStyle),
        yAxis = tickStyleFrom(typo, spec.yAxis.labelStyle),
        legend = legend,
        pieSliceLabel = legend,
        yAxisSecondaryTick = tickStyleFrom(typo, spec.xAxis.labelStyle),
    )
}

private fun tickStyleFrom(
    typography: Typography,
    labelStyle: AxisLabelStyle,
): TextStyle = typography.labelSmall.copy(color = labelStyle.color.toComposeColor())

private fun legendStyleFrom(
    typography: Typography,
    legendStyle: LegendStyle,
): TextStyle = typography.labelSmall.copy(color = legendStyle.textColor.toComposeColor())
