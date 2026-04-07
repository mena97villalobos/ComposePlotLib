package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendStyle
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec

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

private fun tickStyleFrom(
    typography: Typography,
    labelStyle: AxisLabelStyle,
): TextStyle = typography.labelSmall.copy(color = labelStyle.color.toComposeColor())

private fun legendStyleFrom(
    typography: Typography,
    legendStyle: LegendStyle,
): TextStyle = typography.labelSmall.copy(color = legendStyle.textColor.toComposeColor())
