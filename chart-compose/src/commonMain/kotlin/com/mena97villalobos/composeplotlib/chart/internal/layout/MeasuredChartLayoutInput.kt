package com.mena97villalobos.composeplotlib.chart.internal.layout

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.mena97villalobos.composeplotlib.chart.internal.canvas.ChartRenderStyles
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import com.mena97villalobos.composeplotlib.core.layout.ChartTextMeasurementInput
import com.mena97villalobos.composeplotlib.core.layout.TextLayoutMetrics
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.formatSliceLabel
import com.mena97villalobos.composeplotlib.core.spec.hasDrawableSlices
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Base [ChartLayoutInput] with [ChartLayoutInput.labelLayoutScale] set from the host font scale (e.g. `LocalDensity.current.fontScale`).
 */
internal fun chartLayoutBaseInput(
    canvasSize: SizeD,
    viewport: ChartViewport,
    fontScale: Float,
): ChartLayoutInput =
    ChartLayoutInput(
        canvasSize = canvasSize,
        viewport = viewport,
        labelLayoutScale = fontScale.toDouble().coerceIn(0.5, 4.0),
    )

/**
 * Re-runs layout with [TextMeasurer]-derived sizes from a preliminary [ChartLayoutPlan] (two-pass layout).
 */
@OptIn(ExperimentalTextApi::class)
internal fun measuredLayoutInput(
    base: ChartLayoutInput,
    preliminaryLayout: ChartLayoutPlan,
    textMeasurer: TextMeasurer,
    renderStyles: ChartRenderStyles,
    pieChartSpec: PieChartSpec? = null,
    /** Extra axis strings to measure (e.g. combined secondary Y tick labels not in [preliminaryLayout]). */
    extraTickLabels: List<String> = emptyList(),
    /** When true, measures [extraTickLabels] with `ChartLabelTextStyles.yAxisSecondaryTick`. */
    extraTickLabelsUseSecondaryStyle: Boolean = false,
): ChartLayoutInput {
    val labelStyles = renderStyles.labels

    fun measure(
        text: String,
        style: TextStyle,
    ): TextLayoutMetrics {
        val result =
            textMeasurer.measure(
                text = AnnotatedString(text),
                style = style,
                overflow = TextOverflow.Clip,
                softWrap = false,
                maxLines = 1,
                constraints = Constraints(maxWidth = Int.MAX_VALUE),
            )
        val height =
            result.size.height
                .toDouble()
                .coerceAtLeast(0.0)
        val firstBase =
            if (result.lineCount > 0) {
                result.getLineBaseline(0).toDouble().coerceIn(0.0, height)
            } else {
                height
            }
        val lastBase =
            if (result.lineCount > 0) {
                result.getLineBaseline(result.lineCount - 1).toDouble().coerceIn(0.0, height)
            } else {
                firstBase
            }
        return TextLayoutMetrics(
            width =
                result.size.width
                    .toDouble()
                    .coerceAtLeast(0.0),
            height = height,
            firstBaselineFromTop = firstBase,
            lastBaselineFromTop = lastBase,
        )
    }

    val tickMetrics = mutableMapOf<String, TextLayoutMetrics>()

    fun putTick(
        label: String,
        style: TextStyle,
    ) {
        val m = measure(label, style)
        tickMetrics[label] = tickMetrics[label]?.let { TextLayoutMetrics.merge(it, m) } ?: m
    }
    preliminaryLayout.xTicks.forEach { putTick(it.label, labelStyles.xAxis) }
    preliminaryLayout.yTicks.forEach { putTick(it.label, labelStyles.yAxis) }
    val extraStyle =
        if (extraTickLabelsUseSecondaryStyle) {
            labelStyles.yAxisSecondaryTick
        } else {
            labelStyles.yAxis
        }
    extraTickLabels.forEach { putTick(it, extraStyle) }

    val legendMetrics =
        preliminaryLayout.legendEntries
            .map { it.label }
            .distinct()
            .associateWith { measure(it, labelStyles.legend) }

    val pieLabelMetrics = mutableMapOf<String, TextLayoutMetrics>()
    val pieSpec = pieChartSpec
    if (pieSpec != null && pieSpec.sliceLabels.enabled && pieSpec.hasDrawableSlices()) {
        val labelsSpec = pieSpec.sliceLabels
        val total =
            pieSpec.slices
                .asSequence()
                .filter { it.value > 0.0 }
                .sumOf { it.value }
        val labelStyle = labelStyles.pieSliceLabel
        pieSpec.slices.forEachIndexed { index, slice ->
            if (slice.value <= 0.0) return@forEachIndexed
            val sweep = 360.0 * (slice.value / total)
            if (sweep < labelsSpec.minSweepDegrees) return@forEachIndexed
            val pct = 100.0 * slice.value / total
            val text = labelsSpec.formatSliceLabel(slice, pct)
            pieLabelMetrics[index.toString()] = measure(text, labelStyle)
        }
    }

    val prev = base.textMeasurements
    return base.copy(
        textMeasurements =
            ChartTextMeasurementInput(
                tickLabelMetrics = tickMetrics,
                legendLabelMetrics = legendMetrics,
                pieSliceLabelMeasurements = pieLabelMetrics.ifEmpty { prev.pieSliceLabelMeasurements },
                fallbackTickLabelMetrics = TextLayoutMetrics.uniformBox(SizeD.Zero),
                fallbackLegendLabelMetrics = TextLayoutMetrics.uniformBox(SizeD.Zero),
                fallbackPieSliceLabelMetrics = prev.fallbackPieSliceLabelMetrics,
            ),
    )
}
